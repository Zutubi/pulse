package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.personal.PersonalBuildException;
import com.zutubi.pulse.core.scm.ScmLocation;
import com.zutubi.pulse.core.scm.WorkingCopyContextImpl;
import com.zutubi.pulse.core.scm.WorkingCopyFactory;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.dev.xmlrpc.PulseXmlRpcClient;
import com.zutubi.pulse.dev.xmlrpc.PulseXmlRpcException;
import com.zutubi.util.Pair;
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
public class PersonalBuildClient
{
    private PersonalBuildConfig config;
    private PersonalBuildUI ui;
    private String password;

    public PersonalBuildClient(PersonalBuildConfig config, PersonalBuildUI ui)
    {
        this.config = config;
        this.ui = ui;
    }

    public Pair<WorkingCopy, WorkingCopyContext> checkConfiguration() throws PersonalBuildException
    {
        ui.debug("Verifying configuration with pulse server...");
        checkRequiredConfig();

        try
        {
            PulseXmlRpcClient rpc = new PulseXmlRpcClient(config.getPulseUrl());

            checkVersion(rpc);

            String token = null;

            try
            {
                ui.debug("Logging in to pulse: url: " + config.getPulseUrl() + ", user: " + config.getPulseUser());
                token = rpc.login(config.getPulseUser(), getPassword());
                ui.debug("Login successful.");
                Pair<WorkingCopy, WorkingCopyContext> pair = prepare(rpc, token);
                ui.debug("Verified: personal build for project: " + config.getProject() + ".");
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
                password = ui.passwordPrompt("Pulse password");
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

        ui.debug("Checking pulse server version...");
        try
        {
            int serverBuild = rpc.getVersion();
            if (serverBuild != ourBuild)
            {
                ui.debug(String.format("Server build (%d) does not match local build (%d)", serverBuild, ourBuild));
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

                    PersonalBuildUI.Response response = ui.ynaPrompt(question, PersonalBuildUI.Response.NO);
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

            ui.debug("Version accepted.");
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
            PersonalBuildUI.Response response = ui.ynPrompt("No pulse server configured.  Configure one now?", PersonalBuildUI.Response.YES);
            if (response.isAffirmative())
            {
                new ConfigCommand().setupPulseConfig(ui, config);
            }
            else
            {
                throw new PersonalBuildException("Required property '" + PersonalBuildConfig.PROPERTY_PULSE_URL + "' not specified.");                
            }
        }

        if (config.getProject() == null)
        {
            PersonalBuildUI.Response response = ui.ynPrompt("No pulse project configured.  Configure one now?", PersonalBuildUI.Response.YES);
            if (response.isAffirmative())
            {
                new ConfigCommand().setupLocalConfig(ui, config);
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
            ui.debug("Checking configuration and obtaining project SCM details...");
            ScmLocation scmLocation = rpc.preparePersonalBuild(token, config.getProject());
            ui.debug("Configuration accepted.");
            String scmType = scmLocation.getType();
            ui.debug("SCM type: " + scmType);

            WorkingCopyContext context = new WorkingCopyContextImpl(config.getBase(), config, ui);
            WorkingCopy wc = WorkingCopyFactory.create(scmType);
            if (wc == null)
            {
                throw new PersonalBuildException("Personal builds are not supported for this SCM (" + scmType + ")");
            }

            if (config.getCheckRepository())
            {
                ui.debug("Checking working copy matches project SCM configuration");
                if (!wc.matchesLocation(context, scmLocation.getLocation()))
                {
                    PersonalBuildUI.Response response = ui.ynaPrompt("This working copy may not match project '" + config.getProject() + "'.  Continue anyway?", PersonalBuildUI.Response.NO);
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
                    ui.debug("Configuration matches.");
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

    public Revision preparePatch(WorkingCopy wc, WorkingCopyContext context, File patchFile, String... spec) throws PersonalBuildException
    {
        Revision rev;
        try
        {
            ui.status("Updating working copy...");
            ui.enterContext();
            try
            {
                rev = wc.update(context, Revision.HEAD);
            }
            finally
            {
                ui.exitContext();
            }
            ui.status("Update complete.");
        }
        catch (ScmException e)
        {
            throw new PersonalBuildException("Unable to update working copy: " + e.getMessage(), e);
        }

        try
        {
            boolean created;

            ui.status("Creating patch archive...");
            ui.enterContext();

            try
            {
                created = wc.writePatchFile(context, patchFile, spec);
            }
            finally
            {
                ui.exitContext();
            }

            if (created)
            {
                ui.status("Patch created.");
                return rev;
            }
            else
            {
                ui.status("No patch created.");
                return null;
            }
        }
        catch (ScmException e)
        {
            throw new PersonalBuildException("Unable to create patch file: " + e.getMessage(), e);
        }
    }

    public long sendRequest(Revision revision, File patchFile) throws PersonalBuildException
    {
        HttpClient client = new HttpClient();

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(config.getPulseUser(), getPassword());
        client.getState().setCredentials(new AuthScope(null, -1), credentials);
        client.getParams().setAuthenticationPreemptive(true);

        PostMethod post = new PostMethod(config.getPulseUrl() + "/personal/personalBuild.action");
        post.setDoAuthentication(true);

        try
        {
            Part[] parts = {
                    new StringPart("project", config.getProject()),
                    new StringPart("revision", revision.getRevisionString()),
                    new FilePart("patch.zip", patchFile),
            };
            post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

            ui.status("Sending patch to pulse server...");
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
                        ui.status("Patch accepted: personal build " + numberStr + ".");
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
