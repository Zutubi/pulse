package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.personal.PatchArchive;
import com.zutubi.pulse.core.personal.PersonalBuildException;
import com.zutubi.pulse.core.scm.ScmLocation;
import com.zutubi.pulse.core.scm.WorkingCopyContextImpl;
import com.zutubi.pulse.core.scm.WorkingCopyFactory;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.dev.xmlrpc.PulseXmlRpcClient;
import com.zutubi.pulse.dev.xmlrpc.PulseXmlRpcException;
import com.zutubi.util.Pair;
import com.zutubi.util.TextUtils;
import com.zutubi.util.io.IOUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * The client does the work of actually talking to the Pulse server, sending
 * personal build requests and getting back the results.
 */
public class PersonalBuildClient extends PersonalBuildUIAwareSupport
{
    private PersonalBuildConfig config;
    private String password;

    public PersonalBuildClient(PersonalBuildConfig config)
    {
        this.config = config;
    }

    public Pair<WorkingCopy, WorkingCopyContext> checkConfiguration() throws PersonalBuildException
    {
        getUI().debug("Verifying configuration with pulse server...");
        checkRequiredConfig();

        try
        {
            PulseXmlRpcClient rpc = new PulseXmlRpcClient(config.getPulseUrl(), config.getProxyHost(), config.getProxyPort());

            checkVersion(rpc);

            String token = null;

            try
            {
                getUI().debug("Logging in to pulse: url: " + config.getPulseUrl() + ", user: " + config.getPulseUser());
                token = rpc.login(config.getPulseUser(), getPassword());
                getUI().debug("Login successful.");
                Pair<WorkingCopy, WorkingCopyContext> pair = prepare(rpc, token);
                getUI().debug("Verified: personal build for project: " + config.getProject() + ".");
                return pair;
            }
            catch (PersonalBuildException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new PersonalBuildException("Unable to log in to pulse server: " + e.getMessage(), e);
            }
            finally
            {
                rpc.failSafeLogout(token);
            }
        }
        catch (MalformedURLException e)
        {
            throw new PersonalBuildException("Invalid pulse server URL '" + config.getPulseUrl() + "'", e);
        }
    }

    private String getPassword()
    {
        if (password == null)
        {
            password = config.getPulsePassword();
            if (password == null)
            {
                password = getUI().passwordPrompt("Pulse password");
                if (password == null)
                {
                    password = "";
                }
            }
        }

        return password;
    }

    private void checkVersion(PulseXmlRpcClient rpc) throws PersonalBuildException
    {
        int ourBuild = Version.getVersion().getBuildNumberAsInt();
        int confirmedBuild = config.getConfirmedVersion();

        getUI().debug("Checking pulse server version...");
        try
        {
            int serverBuild = rpc.getVersion();
            if (serverBuild != ourBuild)
            {
                getUI().debug(String.format("Server build (%d) does not match local build (%d)", serverBuild, ourBuild));
                if (serverBuild != confirmedBuild)
                {
                    String serverVersion = Version.buildNumberToVersion(serverBuild);
                    String ourVersion = Version.buildNumberToVersion(ourBuild);
                    String question;

                    if (serverVersion.equals(ourVersion))
                    {
                        question = String.format("Server build (%d) does not match tools build (%d).  Continue anyway?", serverBuild, ourBuild);
                    }
                    else
                    {
                        question = String.format("Server version (%s) does not match tools version (%s).  Continue anyway?", serverVersion, ourVersion);
                    }

                    PersonalBuildUI.Response response = getUI().ynaPrompt(question, PersonalBuildUI.Response.NO);
                    if (response == PersonalBuildUI.Response.ALWAYS)
                    {
                        config.setConfirmedVersion(serverBuild);
                    }
                    else if (!response.isAffirmative())
                    {
                        throw new UserAbortException();
                    }
                }
            }

            getUI().debug("Version accepted.");
        }
        catch (PulseXmlRpcException e)
        {
            throw new PersonalBuildException("Unable to get pulse server version: " + e.getMessage(), e);
        }
    }

    private void checkRequiredConfig() throws PersonalBuildException
    {
        if (config.getPulseUrl() == null)
        {
            PersonalBuildUI.Response response = getUI().ynPrompt("No pulse server configured.  Configure one now?", PersonalBuildUI.Response.YES);
            if (response.isAffirmative())
            {
                new ConfigCommand().setupPulseConfig(getUI(), config);
            }
            else
            {
                throw new PersonalBuildException("Required property '" + PersonalBuildConfig.PROPERTY_PULSE_URL + "' not specified.");                
            }
        }

        if (config.getProject() == null)
        {
            PersonalBuildUI.Response response = getUI().ynPrompt("No pulse project configured.  Configure one now?", PersonalBuildUI.Response.YES);
            if (response.isAffirmative())
            {
                new ConfigCommand().setupLocalConfig(getUI(), config);
            }
            else
            {
                throw new PersonalBuildException("Required property '" + PersonalBuildConfig.PROPERTY_PROJECT + "' not specified.");
            }
        }
    }

    private Pair<WorkingCopy, WorkingCopyContext> prepare(PulseXmlRpcClient rpc, String token) throws PersonalBuildException
    {
        try
        {
            getUI().debug("Checking configuration and obtaining project SCM details...");
            ScmLocation scmLocation = rpc.preparePersonalBuild(token, config.getProject());
            getUI().debug("Configuration accepted.");
            String scmType = scmLocation.getType();
            getUI().debug("SCM type: " + scmType);

            WorkingCopyContext context = new WorkingCopyContextImpl(config.getBase(), config);
            WorkingCopy wc = WorkingCopyFactory.create(scmType);
            if (wc == null)
            {
                throw new PersonalBuildException("Personal builds are not supported for this SCM (" + scmType + ")");
            }

            if (wc instanceof PersonalBuildUIAware)
            {
                ((PersonalBuildUIAware) wc).setUI(getUI());
            }

            if (config.getCheckRepository())
            {
                getUI().debug("Checking working copy matches project SCM configuration");
                if (!wc.matchesLocation(context, scmLocation.getLocation()))
                {
                    PersonalBuildUI.Response response = getUI().ynaPrompt("This working copy may not match project '" + config.getProject() + "'.  Continue anyway?", PersonalBuildUI.Response.NO);
                    if (response.isPersistent())
                    {
                        config.setCheckRepository(!response.isAffirmative());
                    }

                    if (!response.isAffirmative())
                    {
                        throw new UserAbortException();
                    }
                }
                else
                {
                    getUI().debug("Configuration matches.");
                }
            }

            return new Pair<WorkingCopy, WorkingCopyContext>(wc, context);
        }
        catch (PersonalBuildException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new PersonalBuildException("Unable to prepare personal build: " + e.getMessage(), e);
        }
    }

    public PatchArchive preparePatch(WorkingCopy wc, WorkingCopyContext context, File patchFile, String... spec) throws PersonalBuildException
    {
        Revision rev;
        try
        {
            getUI().status("Updating working copy...");
            getUI().enterContext();
            try
            {
                rev = wc.update(context, Revision.HEAD);
            }
            finally
            {
                getUI().exitContext();
            }
            getUI().status("Update complete.");
        }
        catch (ScmException e)
        {
            throw new PersonalBuildException("Unable to update working copy: " + e.getMessage(), e);
        }

        getUI().status("Getting working copy status...");
        getUI().enterContext();

        WorkingCopyStatus status;
        try
        {
            status = getStatus(wc, context, spec);
        }
        finally
        {
            getUI().exitContext();
        }
        getUI().status("Status retrieved.");

        if (status.hasStatuses())
        {
            getUI().status("Creating patch archive...");
            getUI().enterContext();

            PatchArchive patchArchive;
            try
            {
                patchArchive = new PatchArchive(rev, status, patchFile, getUI());
            }
            finally
            {
                getUI().exitContext();
            }
            getUI().status("Patch created.");

            return patchArchive;
        }
        else
        {
            return null;
        }
    }

    public WorkingCopyStatus getStatus(WorkingCopy wc, WorkingCopyContext context, String... spec) throws PersonalBuildException
    {
        WorkingCopyStatus status;

        try
        {
            status = wc.getLocalStatus(context, spec);
        }
        catch (ScmException e)
        {
            throw new PersonalBuildException("Unable to get working copy status: " + e.getMessage(), e);
        }

        if (!status.inConsistentState())
        {
            // Fatal, we can't deal with wc's in this state
            throw new PersonalBuildException("Working copy is not in a consistent state.");
        }

        return status;
    }

    public long sendRequest(PatchArchive patch) throws PersonalBuildException
    {
        HttpClient client = new HttpClient();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(config.getPulseUser(), getPassword());
        final AuthScope authScope = new AuthScope(null, -1);

        String proxyHost = config.getProxyHost();
        if (TextUtils.stringSet(proxyHost))
        {
            client.getHostConfiguration().setProxy(proxyHost, config.getProxyPort());
            client.getState().setProxyCredentials(authScope, credentials);
        }

        client.getState().setCredentials(authScope, credentials);
        client.getParams().setAuthenticationPreemptive(true);

        PostMethod post = new PostMethod(config.getPulseUrl() + "/personal/personalBuild.action");
        post.setDoAuthentication(true);

        try
        {
            Part[] parts = {
                    new StringPart("version", Version.getVersion().getBuildNumber()),
                    new StringPart("project", config.getProject()),
                    new FilePart("patch.zip", patch.getPatchFile()),
            };
            post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

            getUI().status("Sending patch to pulse server...");
            int status = client.executeMethod(post);
            if (status == HttpStatus.SC_OK)
            {
                // That's good ... now check the response
                String response = IOUtils.inputStreamToString(post.getResponseBodyAsStream());
                if (response.startsWith("OK:"))
                {
                    String numberStr = response.substring(3);
                    try
                    {
                        long number = Long.parseLong(numberStr);
                        getUI().status("Patch accepted: personal build " + numberStr + ".");
                        return number;
                    }
                    catch (NumberFormatException e)
                    {
                        throw new PersonalBuildException("Pulse server returned invalid build number '" + numberStr + "'");
                    }
                }
                else
                {
                    throw new PersonalBuildException("Pulse server responded with: " + response);
                }
            }
            else
            {
                // Not good
                throw new PersonalBuildException("Pulse server returned error code " + status + " (" + HttpStatus.getStatusText(status) + ")");
            }
        }
        catch (IOException e)
        {
            throw new PersonalBuildException("I/O error sending patch to pulse server: " + e.getMessage(), e);
        }
        finally
        {
            post.releaseConnection();
        }
    }
}
