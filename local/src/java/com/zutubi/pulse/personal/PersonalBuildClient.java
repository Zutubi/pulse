package com.zutubi.pulse.personal;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.scm.*;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.xmlrpc.PulseXmlRpcClient;
import com.zutubi.pulse.xmlrpc.PulseXmlRpcException;
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
public class PersonalBuildClient extends PersonalBuildSupport
{
    private PersonalBuildConfig config;
    private String password;

    public PersonalBuildClient(PersonalBuildConfig config)
    {
        this.config = config;
    }

    public WorkingCopy checkConfiguration() throws PersonalBuildException
    {
        debug("Verifying configuration with pulse server...");
        checkRequiredConfig();

        try
        {
            PulseXmlRpcClient rpc = new PulseXmlRpcClient(config.getPulseUrl(), config.getProxyHost(), config.getProxyPort());

            checkVersion(rpc);

            String token = null;

            try
            {
                debug("Logging in to pulse: url: " + config.getPulseUrl() + ", user: " + config.getPulseUser());
                token = rpc.login(config.getPulseUser(), getPassword());
                debug("Login successful.");
                WorkingCopy wc = prepare(rpc, token);
                debug("Verified: personal build for project: " + config.getProject() + ", specification: " + config.getSpecification() + ".");
                return wc;
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
                password = passwordPrompt("Pulse password");
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

        debug("Checking pulse server version...");
        try
        {
            int serverBuild = rpc.getVersion();
            if (serverBuild != ourBuild)
            {
                debug("Server build (%d) does not match local build (%d)", serverBuild, ourBuild);
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

                    PersonalBuildUI.Response response = ynaPrompt(question, PersonalBuildUI.Response.NO);
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

            debug("Version accepted.");
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
            PersonalBuildUI.Response response = ynPrompt("No pulse server configured.  Configure one now?", PersonalBuildUI.Response.YES);
            if (response.isAffirmative())
            {
                new ConfigCommand().setupPulseConfig(getUi(), config);
            }
            else
            {
                throw new PersonalBuildException("Required property '" + PersonalBuildConfig.PROPERTY_PULSE_URL + "' not specified.");                
            }
        }

        if (config.getProject() == null)
        {
            PersonalBuildUI.Response response = ynPrompt("No pulse project configured.  Configure one now?", PersonalBuildUI.Response.YES);
            if (response.isAffirmative())
            {
                new ConfigCommand().setupLocalConfig(getUi(), config);
            }
            else
            {
                throw new PersonalBuildException("Required property '" + PersonalBuildConfig.PROPERTY_PROJECT + "' not specified.");
            }
        }
    }

    private WorkingCopy prepare(PulseXmlRpcClient rpc, String token) throws PersonalBuildException
    {
        try
        {
            debug("Checking configuration and obtaining project SCM details...");
            SCMConfiguration scmConfiguration = rpc.preparePersonalBuild(token, config.getProject(), config.getSpecification());
            debug("Configuration accepted.");
            String scmType = scmConfiguration.getType();
            debug("SCM type: " + scmType);

            WorkingCopy wc = WorkingCopyFactory.create(scmType, config.getBase(), config);
            if (wc == null)
            {
                throw new PersonalBuildException("Personal builds are not supported for this SCM (" + scmType + ")");
            }

            wc.setUI(getUi());
            if (config.getCheckRepository())
            {
                debug("Checking working copy matches project SCM configuration");
                if (!wc.matchesRepository(scmConfiguration.getRepositoryDetails()))
                {
                    PersonalBuildUI.Response response = ynaPrompt("This working copy may not match project '" + config.getProject() + "'.  Continue anyway?", PersonalBuildUI.Response.NO);
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
                    debug("Configuration matches.");
                }
            }

            return wc;
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

    public PatchArchive preparePatch(WorkingCopy wc, File patchFile, String... spec) throws PersonalBuildException
    {
        Revision rev;
        try
        {
            status("Updating working copy...");
            enterContext();
            try
            {
                rev = wc.update();
            }
            finally
            {
                exitContext();
            }
            status("Update complete.");
        }
        catch (SCMException e)
        {
            throw new PersonalBuildException("Unable to update working copy: " + e.getMessage(), e);
        }

        status("Getting working copy status...");
        enterContext();

        WorkingCopyStatus status;
        try
        {
            status = getStatus(wc, spec);
            status.setRevision(rev);
        }
        finally
        {
            exitContext();
        }
        status("Status retrieved.");

        if (status.hasChanges())
        {
            status("Creating patch archive...");
            enterContext();

            PatchArchive patchArchive;
            try
            {
                patchArchive = new PatchArchive(status, patchFile, getUi());
            }
            finally
            {
                exitContext();
            }
            status("Patch created.");

            return patchArchive;
        }
        else
        {
            return null;
        }
    }

    public WorkingCopyStatus getStatus(WorkingCopy wc, String... spec) throws PersonalBuildException
    {
        WorkingCopyStatus status;

        try
        {
            status = wc.getLocalStatus(spec);
        }
        catch (SCMException e)
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
                    new StringPart("specification", config.getSpecification()),
                    new FilePart("patch.zip", patch.getPatchFile()),
            };
            post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

            status("Sending patch to pulse server...");
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
                        status("Patch accepted: personal build " + numberStr + ".");
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
