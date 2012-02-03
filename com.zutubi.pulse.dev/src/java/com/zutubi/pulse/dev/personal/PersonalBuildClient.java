package com.zutubi.pulse.dev.personal;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.plugins.repository.PluginInfo;
import com.zutubi.pulse.core.plugins.sync.PluginSynchroniser;
import com.zutubi.pulse.core.plugins.sync.SynchronisationActions;
import com.zutubi.pulse.core.scm.PersonalBuildInfo;
import com.zutubi.pulse.core.scm.WorkingCopyContextImpl;
import com.zutubi.pulse.core.scm.WorkingCopyFactory;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.patch.PatchFormatFactory;
import com.zutubi.pulse.core.scm.patch.api.PatchFormat;
import com.zutubi.pulse.core.ui.api.MenuChoice;
import com.zutubi.pulse.core.ui.api.MenuOption;
import com.zutubi.pulse.core.ui.api.UserInterface;
import com.zutubi.pulse.core.ui.api.YesNoResponse;
import com.zutubi.pulse.dev.client.AbstractClient;
import com.zutubi.pulse.dev.client.ClientException;
import com.zutubi.pulse.dev.client.UserAbortException;
import com.zutubi.pulse.dev.sync.SynchronisePluginsClient;
import com.zutubi.pulse.dev.xmlrpc.PulseXmlRpcClient;
import com.zutubi.util.Pair;
import com.zutubi.util.Sort;
import com.zutubi.util.StringUtils;
import nu.xom.ParsingException;
import nu.xom.XMLException;
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
import java.io.StringWriter;
import java.util.*;

/**
 * The client does the work of actually talking to the Pulse server, sending
 * personal build requests and getting back the results.
 */
public class PersonalBuildClient extends AbstractClient<PersonalBuildConfig>
{
    private static final Messages I18N = Messages.getInstance(PersonalBuildClient.class);

    static final String REVISION_OPTION_LOCAL    = "@local";
    static final String REVISION_OPTION_LATEST   = "@latest";
    static final String REVISION_OPTION_FLOATING = "@floating";
    static final String REVISION_OPTION_GOOD     = "@good";
    static final String REVISION_OPTION_CUSTOM   = "@custom";

    private static final int EXIT_REBOOT = 111;
    
    private PatchFormatFactory patchFormatFactory;
    private PluginManager pluginManager;
    private PluginSynchroniser pluginSynchroniser;

    public PersonalBuildClient(PersonalBuildConfig config, UserInterface ui)
    {
        super(config, ui);
    }

    public PersonalBuildContext checkConfiguration() throws ClientException
    {
        ensureServerConfigured();
        ensureProjectConfigured();
        
        PulseXmlRpcClient rpc = getXmlRpcClient();
        checkVersion(rpc);

        String token = null;
        try
        {
            token = login(rpc);
            PersonalBuildContext context = prepare(rpc, token, patchFormatFactory);
            ui.debug("Verified: personal build for project: " + config.getProject() + ".");
            return context;
        }
        catch (ClientException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ClientException("Unable to log in to pulse server: " + e.getMessage(), e);
        }
        finally
        {
            rpc.failSafeLogout(token);
        }
    }

    private void ensureProjectConfigured() throws ClientException
    {
        if (config.getProject() == null)
        {
            YesNoResponse response = ui.yesNoPrompt(I18N.format("prompt.pulse.project"), false, false, YesNoResponse.YES);
            if (response.isAffirmative())
            {
                new ConfigCommand().setupLocalConfig(ui, config);
            }
            else
            {
                throw new ClientException("Required property '" + PersonalBuildConfig.PROPERTY_PROJECT + "' not specified.");
            }
        }
    }

    private PersonalBuildContext prepare(PulseXmlRpcClient rpc, String token, PatchFormatFactory patchFormatFactory) throws ClientException
    {
        try
        {
            ui.debug("Checking configuration and obtaining project SCM details...");
            PersonalBuildInfo personalBuildInfo = rpc.preparePersonalBuild(token, config.getProject());
            ui.debug("Configuration accepted.");

            checkPlugins(personalBuildInfo.getPlugins());
            
            String scmType = personalBuildInfo.getScmType();
            ui.debug("SCM type: " + scmType);

            WorkingCopyContext context = new WorkingCopyContextImpl(config.getBase(), config, ui);
            WorkingCopy wc = WorkingCopyFactory.create(scmType);
            if (config.getPatchFile() == null)
            {
                checkRepositoryIfRequired(wc, context, personalBuildInfo);

                Pair<String, PatchFormat> typeAndFormat = patchFormatFactory.createByScmType(scmType);
                if (typeAndFormat == null)
                {
                    throw new ClientException("No patch format registered for this SCM (" + scmType + ")");
                }

                return new PersonalBuildContext(wc, context, typeAndFormat.first, typeAndFormat.second);
            }
            else
            {
                String patchType = guessExistingPatchType(patchFormatFactory);
                return new PersonalBuildContext(wc, context, patchType, patchFormatFactory.createByFormatType(patchType));
            }
        }
        catch (ClientException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ClientException("Unable to prepare personal build: " + e.getMessage(), e);
        }
    }

    private void checkPlugins(List<PluginInfo> plugins) throws ClientException
    {
        ui.debug("Checking if plugins are in sync with master...");
        
        SynchronisationActions requiredActions = pluginSynchroniser.determineRequiredActions(plugins);
        if (requiredActions.isSyncRequired())
        {
            ui.debug("Sync is required.");
            Boolean syncPreference = config.getSyncPlugins();
            boolean sync;
            if (syncPreference == null)
            {
                YesNoResponse response = ui.yesNoPrompt(I18N.format("prompt.sync.required"), true, true, YesNoResponse.YES);
                if (response.isPersistent())
                {
                    config.setSyncPlugins(response.isAffirmative());
                }
                
                sync = response.isAffirmative();
            }
            else
            {
                sync = syncPreference;
                if (sync)
                {
                    ui.status(I18N.format("sync.required"));
                }
            }
            
            if (sync)
            {
                synchronisePlugins();
            }
        }
        else
        {
            ui.debug("No sync required.");
        }
        
        ui.debug("Plugin check complete.");
    }

    private void synchronisePlugins() throws ClientException
    {
        SynchronisePluginsClient syncClient = new SynchronisePluginsClient(config, ui);
        syncClient.setPluginManager(pluginManager);
        syncClient.setPluginSynchroniser(pluginSynchroniser);
        if (syncClient.syncPlugins())
        {
            ui.status(I18N.format("restarting"));
            System.exit(EXIT_REBOOT);
        }
    }

    private void checkRepositoryIfRequired(WorkingCopy wc, WorkingCopyContext context, PersonalBuildInfo personalBuildInfo) throws ScmException, UserAbortException
    {
        if (config.getCheckRepository())
        {
            ui.debug("Checking working copy matches project SCM configuration");
            if (!wc.matchesLocation(context, personalBuildInfo.getScmLocation()))
            {
                YesNoResponse response = ui.yesNoPrompt(I18N.format("prompt.wc.mismatch", config.getProject()), true, false, YesNoResponse.NO);
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
    }

    private String guessExistingPatchType(PatchFormatFactory patchFormatFactory) throws ClientException
    {
        String patchFilename = config.getPatchFile();
        File patchFile = new File(patchFilename);
        if (!patchFile.exists())
        {
            throw new ClientException("Specified patch file '" + patchFilename + "' does not exist.");
        }
        
        String patchType = config.getPatchType();
        if (patchType == null)
        {
            ui.status("Guessing format of patch file...");
            patchType = patchFormatFactory.guessFormatType(patchFile);
            if (patchType == null)
            {
                List<String> supportedTypes = patchFormatFactory.getFormatTypes();
                Collections.sort(supportedTypes, new Sort.StringComparator());
                throw new ClientException("Unable to guess type of patch.  Please specify a type (supported types are " + supportedTypes + ").");
            }

            ui.status("Guessed type '" + patchType + "'.");
        }

        return patchType;
    }

    public PersonalBuildRevision chooseRevision(PersonalBuildContext context) throws ClientException
    {
        String chosenRevision = config.getRevision();
        boolean fromConfig = true;
        if (!StringUtils.stringSet(chosenRevision))
        {
            fromConfig = false;
            WorkingCopy workingCopy = context.getWorkingCopy();
            Set<WorkingCopyCapability> capabilities = workingCopy == null ? Collections.<WorkingCopyCapability>emptySet() : workingCopy.getCapabilities();

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
            return guessLocalRevision(context);
        }
        else if (chosenRevision.equals(REVISION_OPTION_LATEST))
        {
            return getLatestRemoteRevision(context);
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
            throw new ClientException("Unknown revision choice '" + chosenRevision + "'");
        }
    }

    private MenuOption<String> makeRevisionOption(String value, boolean defaultOption)
    {
        return new MenuOption<String>(value, I18N.format("revision.option." + value.substring(1)), defaultOption);
    }

    private PersonalBuildRevision guessLocalRevision(PersonalBuildContext context) throws ClientException
    {
        Revision revision;
        ui.status(I18N.format("status.guessing.revision"));
        ui.enterContext();
        try
        {
            revision = context.getRequiredWorkingCopy().guessLocalRevision(context.getWorkingCopyContext());
        }
        catch (ScmException e)
        {
            throw new ClientException("Unable to guess local revision: " + e.getMessage(), e);
        }
        finally
        {
            ui.exitContext();
        }

        ui.status(I18N.format("status.guessed.revision", revision.getRevisionString()));
        return new PersonalBuildRevision(revision, true);
    }

    private PersonalBuildRevision getLatestRemoteRevision(PersonalBuildContext context) throws ClientException
    {
        Revision revision;
        ui.status(I18N.format("status.getting.latest.revision"));
        try
        {
            revision = context.getRequiredWorkingCopy().getLatestRemoteRevision(context.getWorkingCopyContext());
        }
        catch (ScmException e)
        {
            throw new ClientException("Unable to guess local revision: " + e.getMessage(), e);
        }
        finally
        {
            ui.exitContext();
        }

        ui.status(I18N.format("status.got.latest.revision", revision.getRevisionString()));
        return new PersonalBuildRevision(revision, true);
    }

    public void updateIfDesired(PersonalBuildContext context, Revision revision) throws ClientException
    {
        WorkingCopy wc = context.getWorkingCopy();
        if (wc != null && wc.getCapabilities().contains(WorkingCopyCapability.UPDATE))
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
                        wc.update(context.getWorkingCopyContext(), revision);
                        ui.status(I18N.format("status.updated"));
                    }
                    finally
                    {
                        ui.exitContext();
                    }
                }
                catch (ScmException e)
                {
                    throw new ClientException("Unable to update working copy: " + e.getMessage(), e);
                }
            }
        }
    }

    public boolean preparePatch(PersonalBuildContext context, File patchFile, String... spec) throws ClientException
    {
        try
        {
            boolean created;
            ui.status(I18N.format("status.preparing.patch"));
            ui.enterContext();

            try
            {
                created = context.getPatchFormat().writePatchFile(context.getRequiredWorkingCopy(), context.getWorkingCopyContext(), patchFile, spec);
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
            throw new ClientException("Unable to create patch file: " + e.getMessage(), e);
        }
    }

    public long sendRequest(PersonalBuildContext context, Revision revision, File patchFile) throws ClientException
    {
        HttpClient client = new HttpClient();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(config.getPulseUser(), getPassword());
        final AuthScope authScope = new AuthScope(null, -1);

        String proxyHost = config.getProxyHost();
        if (StringUtils.stringSet(proxyHost))
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
                    new StringPart("overrides", convertOverrides(config.getOverrides())),
                    new StringPart("patchFormat", context.getPatchFormatType()),
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
                        throw new ClientException("Patch rejected.");
                    }
                }
                catch (ParsingException e)
                {
                    throw new ClientException("Unable to parse response from server: " + e.getMessage(), e);
                }
                catch (XMLException e)
                {
                    throw new ClientException("Invalid response from server: " + e.getMessage(), e);
                }
            }
            else
            {
                // Not good
                throw new ClientException("Pulse server returned error code " + status + " (" + HttpStatus.getStatusText(status) + ")");
            }
        }
        catch (IOException e)
        {
            throw new ClientException("I/O error sending patch to pulse server: " + e.getMessage(), e);
        }
        finally
        {
            post.releaseConnection();
        }
    }

    private String convertOverrides(Properties overrides)
    {
        StringWriter writer = new StringWriter();
        try
        {
            overrides.store(writer, "");
        }
        catch (IOException e)
        {
            // Not expected with a string output.
        }
        return writer.toString();
    }

    public void setPatchFormatFactory(PatchFormatFactory patchFormatFactory)
    {
        this.patchFormatFactory = patchFormatFactory;
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }

    public void setPluginSynchroniser(PluginSynchroniser pluginSynchroniser)
    {
        this.pluginSynchroniser = pluginSynchroniser;
    }
}
