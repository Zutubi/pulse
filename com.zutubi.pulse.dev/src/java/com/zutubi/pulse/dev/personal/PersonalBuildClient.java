package com.zutubi.pulse.dev.personal;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.personal.PersonalBuildException;
import com.zutubi.pulse.core.scm.ScmLocation;
import com.zutubi.pulse.core.scm.WorkingCopyContextImpl;
import com.zutubi.pulse.core.scm.WorkingCopyFactory;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.dev.xmlrpc.PulseXmlRpcClient;
import com.zutubi.pulse.dev.xmlrpc.PulseXmlRpcException;
import com.zutubi.util.Pair;
import com.zutubi.util.TextUtils;
import nu.xom.ParsingException;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The client does the work of actually talking to the Pulse server, sending
 * personal build requests and getting back the results.
 */
public class PersonalBuildClient
{
    private static final Messages I18N = Messages.getInstance(PersonalBuildClient.class);

    static final String REVISION_OPTION_LOCAL    = "@local";
    static final String REVISION_OPTION_LATEST   = "@latest";
    static final String REVISION_OPTION_FLOATING = "@floating";
    static final String REVISION_OPTION_GOOD     = "@good";
    static final String REVISION_OPTION_CUSTOM   = "@custom";

    private PersonalBuildConfig config;
    private PersonalBuildUI ui;
    private String password;

    public PersonalBuildClient(PersonalBuildConfig config, PersonalBuildUI ui)
    {
        this.config = config;
        this.ui = ui;
    }

    public PersonalBuildConfig getConfig()
    {
        return config;
    }

    public PersonalBuildUI getUI()
    {
        return ui;
    }

    public Pair<WorkingCopy, WorkingCopyContext> checkConfiguration() throws PersonalBuildException
    {
        ui.debug("Verifying configuration with pulse server...");
        checkRequiredConfig();

        try
        {
            PulseXmlRpcClient rpc = new PulseXmlRpcClient(config.getPulseUrl(), config.getProxyHost(), config.getProxyPort());

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
                        question = I18N.format("prompt.build.mismatch", serverBuild, ourBuild);
                    }
                    else
                    {
                        question = I18N.format("prompt.version.mismatch", new Object[]{serverVersion, ourVersion});
                    }

                    YesNoResponse response = ui.yesNoPrompt(question, true, false, YesNoResponse.NO);
                    if (response.isPersistent())
                    {
                        config.setConfirmedVersion(serverBuild);
                    }

                    if (!response.isAffirmative())
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
            YesNoResponse response = ui.yesNoPrompt(I18N.format("prompt.pulse.server"), false, false, YesNoResponse.YES);
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
            YesNoResponse response = ui.yesNoPrompt(I18N.format("prompt.pulse.project"), false, false, YesNoResponse.YES);
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
                    YesNoResponse response = ui.yesNoPrompt(I18N.format("prompt.wc.mismatch", new Object[]{ config.getProject() }), true, false, YesNoResponse.NO);
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

    public PersonalBuildRevision chooseRevision(WorkingCopy wc, WorkingCopyContext context) throws PersonalBuildException
    {
        String chosenRevision = config.getRevision();
        boolean fromConfig = true;
        if (!TextUtils.stringSet(chosenRevision))
        {
            fromConfig = false;
            Set<WorkingCopyCapability> capabilities = wc.getCapabilities();

            List<MenuOption<String>> options = new LinkedList<MenuOption<String>>();
            if (capabilities.contains(WorkingCopyCapability.LOCAL_REVISION))
            {
                options.add(makeRevisionOption(REVISION_OPTION_LOCAL, false));
            }
            if (capabilities.contains(WorkingCopyCapability.REMOTE_REVISION))
            {
                options.add(makeRevisionOption(REVISION_OPTION_LATEST, true));
            }
            options.add(makeRevisionOption(REVISION_OPTION_FLOATING, !capabilities.contains(WorkingCopyCapability.REMOTE_REVISION)));
            options.add(makeRevisionOption(REVISION_OPTION_GOOD, false));
            options.add(makeRevisionOption(REVISION_OPTION_CUSTOM, false));

            MenuChoice<String> choice = ui.menuPrompt(I18N.format("prompt.choose.revision"), options);
            chosenRevision = choice.getValue();
            if (choice.isPersistent())
            {
                config.setRevision(chosenRevision);
            }
        }

        if (chosenRevision.equals(REVISION_OPTION_LOCAL))
        {
            return guessLocalRevision(wc, context);
        }
        else if (chosenRevision.equals(REVISION_OPTION_LATEST))
        {
            return getLatestRemoteRevision(wc, context);
        }
        else if (chosenRevision.equals(REVISION_OPTION_FLOATING))
        {
            return new PersonalBuildRevision(WorkingCopy.REVISION_FLOATING, false);
        }
        else if (chosenRevision.equals(REVISION_OPTION_GOOD))
        {
            return new PersonalBuildRevision(WorkingCopy.REVISION_LAST_KNOWN_GOOD, false);
        }
        else if (chosenRevision.equals(REVISION_OPTION_CUSTOM))
        {
            String custom = ui.inputPrompt(I18N.format("prompt.custom.revision")).trim();
            if (custom.length() > 0)
            {
                return new PersonalBuildRevision(new Revision(custom), true);
            }
            else
            {
                return new PersonalBuildRevision(WorkingCopy.REVISION_FLOATING, false);
            }
        }
        else if (fromConfig)
        {
            return new PersonalBuildRevision(new Revision(chosenRevision), true);
        }
        else
        {
            throw new PersonalBuildException("Unknown revision choice '" + chosenRevision + "'");
        }
    }

    private MenuOption<String> makeRevisionOption(String value, boolean defaultOption)
    {
        return new MenuOption<String>(value, I18N.format("revision.option." + value.substring(1)), defaultOption);
    }

    private PersonalBuildRevision guessLocalRevision(WorkingCopy wc, WorkingCopyContext context) throws PersonalBuildException
    {
        Revision revision;
        ui.status(I18N.format("status.guessing.revision"));
        ui.enterContext();
        try
        {
            revision = wc.guessLocalRevision(context);
        }
        catch (ScmException e)
        {
            throw new PersonalBuildException("Unable to guess local revision: " + e.getMessage(), e);
        }
        finally
        {
            ui.exitContext();
        }

        ui.status(I18N.format("status.guessed.revision", new Object[]{revision.getRevisionString()}));
        return new PersonalBuildRevision(revision, true);
    }

    private PersonalBuildRevision getLatestRemoteRevision(WorkingCopy wc, WorkingCopyContext context) throws PersonalBuildException
    {
        Revision revision;
        ui.status(I18N.format("status.getting.latest.revision"));
        try
        {
            revision = wc.getLatestRemoteRevision(context);
        }
        catch (ScmException e)
        {
            throw new PersonalBuildException("Unable to guess local revision: " + e.getMessage(), e);
        }
        finally
        {
            ui.exitContext();
        }

        ui.status(I18N.format("status.got.latest.revision", new Object[]{revision.getRevisionString()}));
        return new PersonalBuildRevision(revision, true);
    }

    public void updateIfDesired(WorkingCopy wc, WorkingCopyContext context, Revision revision) throws PersonalBuildException
    {
        if (wc.getCapabilities().contains(WorkingCopyCapability.UPDATE))
        {
            Boolean update = config.getUpdate();
            if (update == null)
            {
                YesNoResponse response = ui.yesNoPrompt(I18N.format("prompt.update"), true, true, YesNoResponse.YES);
                if (response.isPersistent())
                {
                    config.setUpdate(response.isAffirmative());
                }

                update = response.isAffirmative();
            }

            if (update)
            {
                try
                {
                    ui.status(I18N.format("status.updating"));
                    ui.enterContext();
                    try
                    {
                        wc.update(context, revision);
                        ui.status(I18N.format("status.updated"));
                    }
                    finally
                    {
                        ui.exitContext();
                    }
                }
                catch (ScmException e)
                {
                    throw new PersonalBuildException("Unable to update working copy: " + e.getMessage(), e);
                }
            }
        }
    }

    public boolean preparePatch(WorkingCopy wc, WorkingCopyContext context, File patchFile, String... spec) throws PersonalBuildException
    {
        try
        {
            boolean created;
            ui.status(I18N.format("status.preparing.patch"));
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
                ui.status(I18N.format("status.prepared.patch"));
            }
            else
            {
                ui.status(I18N.format("status.no.patch"));
            }

            return created;
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
                try
                {
                    PersonalBuildResponse response = PersonalBuildResponse.parse(post.getResponseBodyAsStream());
                    for (String warning: response.getWarnings())
                    {
                        ui.warning(warning);
                    }

                    for (String error: response.getErrors())
                    {
                        ui.error(error);
                    }

                    if (response.isSuccess())
                    {
                        ui.status("Patch accepted: personal build " + response.getNumber() + ".");
                        return response.getNumber();
                    }
                    else
                    {
                        throw new PersonalBuildException("Patch rejected.");
                    }
                }
                catch (ParsingException e)
                {
                    throw new PersonalBuildException("Unable to parse response from server: " + e.getMessage(), e);
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
