package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.acceptance.rpc.RemoteApiClient;
import com.zutubi.pulse.core.patchformats.unified.UnifiedPatchFormat;
import com.zutubi.pulse.core.plugins.sync.PluginSynchroniser;
import com.zutubi.pulse.core.plugins.sync.SynchronisationActions;
import com.zutubi.pulse.core.scm.WorkingCopyFactory;
import com.zutubi.pulse.core.scm.git.GitClient;
import com.zutubi.pulse.core.scm.git.GitPatchFormat;
import com.zutubi.pulse.core.scm.git.GitWorkingCopy;
import com.zutubi.pulse.core.scm.hg.MercurialClient;
import com.zutubi.pulse.core.scm.hg.MercurialPatchFormat;
import com.zutubi.pulse.core.scm.hg.MercurialWorkingCopy;
import com.zutubi.pulse.core.scm.p4.PerforceClient;
import com.zutubi.pulse.core.scm.p4.PerforceWorkingCopy;
import com.zutubi.pulse.core.scm.patch.DefaultPatchFormatFactory;
import com.zutubi.pulse.core.scm.svn.SubversionClient;
import com.zutubi.pulse.core.scm.svn.SubversionWorkingCopy;
import com.zutubi.pulse.dev.client.ClientException;
import com.zutubi.pulse.dev.personal.PersonalBuildClient;
import com.zutubi.pulse.dev.personal.PersonalBuildCommand;
import com.zutubi.pulse.dev.personal.PersonalBuildConfig;
import com.zutubi.util.Pair;
import com.zutubi.util.StringUtils;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.io.IOUtils;
import org.mockito.Matchers;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The personal build runner is an acceptance test support class that
 * assists with the running of personal builds.
 */
public class PersonalBuildRunner
{
    private RemoteApiClient remoteApi;

    private DefaultPatchFormatFactory patchFormatFactory;

    private File base;

    static
    {
        try
        {
            WorkingCopyFactory.registerType("svn", SubversionWorkingCopy.class);
            WorkingCopyFactory.registerType("git", GitWorkingCopy.class);
            WorkingCopyFactory.registerType("p4", PerforceWorkingCopy.class);
            WorkingCopyFactory.registerType("hg", MercurialWorkingCopy.class);
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Create a new instance of the personal build runner.
     *
     * @param remoteApi  an xml rpc helper used to assist with the operations of the
     * personal build runner.  Note that the xml rpc helper should be logged in with the
     * same user as is running the build.
     */
    public PersonalBuildRunner(RemoteApiClient remoteApi)
    {
        patchFormatFactory = new DefaultPatchFormatFactory();
        patchFormatFactory.registerScm(SubversionClient.TYPE, DefaultPatchFormatFactory.FORMAT_STANDARD);
        patchFormatFactory.registerScm(PerforceClient.TYPE, DefaultPatchFormatFactory.FORMAT_STANDARD);
        patchFormatFactory.registerScm(GitClient.TYPE, "git");
        patchFormatFactory.registerScm(MercurialClient.TYPE, "hg");
        patchFormatFactory.registerFormatType("hg", MercurialPatchFormat.class);
        patchFormatFactory.registerFormatType("git", GitPatchFormat.class);
        patchFormatFactory.registerFormatType("unified", UnifiedPatchFormat.class);
        patchFormatFactory.setObjectFactory(new DefaultObjectFactory());

        this.remoteApi = remoteApi;
    }

    /**
     * Set the base for the personal builds
     *
     * @param base  base directory for personal builds.    
     */
    public void setBase(File base)
    {
        this.base = base;
    }

    /**
     * Trigger a personal build and wait for it to complete before returning.
     *
     * @return  the personal build ui instance
     *
     * @throws IOException  on error
     * @throws com.zutubi.pulse.dev.client.ClientException on error
     */
    public AcceptancePersonalBuildUI triggerAndWaitForBuild() throws IOException, ClientException
    {
        AcceptancePersonalBuildUI ui = triggerBuild();

        remoteApi.waitForBuildToComplete((int)ui.getBuildNumber());

        return ui;
    }

    /**
     * Trigger a personal build and return immediately.
     * @return the personal build ui instance.
     * @throws IOException on error
     * @throws com.zutubi.pulse.dev.client.ClientException on error
     */
    public AcceptancePersonalBuildUI triggerBuild() throws IOException, ClientException
    {
        File configFile = new File(base, PersonalBuildConfig.PROPERTIES_FILENAME);
        if (!configFile.isFile())
        {
            throw new IOException("Create config file before triggering build");
        }
        
        AcceptancePersonalBuildUI ui = requestPersonalBuild();
        if (ui.getErrorMessages().size() > 0)
        {
            String details = StringUtils.join("\n", ui.getErrorMessages());
            throw new ClientException("Errors have occured during the personal build.\n" + details);
        }
        if (ui.getWarningMessages().size() > 0)
        {
            String details = StringUtils.join("\n", ui.getErrorMessages());
            throw new ClientException("Warnings have occured during the personal build.\n" + details);
        }
        return ui;
    }

    /**
     * Create the file used to configure the personal build process.
     *
     * @param baseUrl           the base url of the pulse server
     * @param user              the pulse user that will be running the personal build
     * @param pass              the pulse users password
     * @param projectName       the name of the project building built
     * @param extraProperties   any extra properties to fine tune the configuration of the build
     * @throws IOException on error
     */
    public void createConfigFile(String baseUrl, String user, String pass, String projectName, Pair<String, ?>... extraProperties) throws IOException
    {
        File configFile = new File(base, PersonalBuildConfig.PROPERTIES_FILENAME);
        Properties config = new Properties();
        config.put(PersonalBuildConfig.PROPERTY_PULSE_URL, baseUrl);
        config.put(PersonalBuildConfig.PROPERTY_PULSE_USER, user);
        config.put(PersonalBuildConfig.PROPERTY_PULSE_PASSWORD, pass);
        config.put(PersonalBuildConfig.PROPERTY_PROJECT, projectName);

        for (Pair<String, ?> extra: extraProperties)
        {
            config.put(extra.first, extra.second.toString());
        }

        FileOutputStream os = null;
        try
        {
            os = new FileOutputStream(configFile);
            config.store(os, null);
        }
        finally
        {
            IOUtils.close(os);
        }
    }

    private AcceptancePersonalBuildUI requestPersonalBuild() throws IOException
    {
        AcceptancePersonalBuildUI ui = new AcceptancePersonalBuildUI();
        PersonalBuildConfig config = new PersonalBuildConfig(base, ui);
        PersonalBuildClient client = new PersonalBuildClient(config, ui);
        client.setPatchFormatFactory(patchFormatFactory);
        
        PluginSynchroniser pluginSynchroniser = mock(PluginSynchroniser.class);
        doReturn(new SynchronisationActions()).when(pluginSynchroniser).determineRequiredActions(Matchers.anyList());
        client.setPluginSynchroniser(pluginSynchroniser);

        PersonalBuildCommand command = new PersonalBuildCommand();
        int exitCode = command.execute(client);
        if (exitCode != 0)
        {
            throw new IOException("Unexpected exit code " + exitCode + " from personal build command. Errors: " + ui.getErrorMessages() + ", Status: " + ui.getStatusMessages());
        }
        return ui;
    }
}
