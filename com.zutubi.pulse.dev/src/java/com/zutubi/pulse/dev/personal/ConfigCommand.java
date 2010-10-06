package com.zutubi.pulse.dev.personal;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.Command;
import com.zutubi.pulse.core.personal.PersonalBuildException;
import com.zutubi.pulse.core.scm.api.PersonalBuildUI;
import com.zutubi.pulse.core.scm.api.YesNoResponse;
import com.zutubi.pulse.dev.xmlrpc.PulseXmlRpcClient;
import com.zutubi.pulse.dev.xmlrpc.PulseXmlRpcException;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.config.PropertiesConfig;
import org.apache.commons.cli.*;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A command which allows interactive setup of required configuration for the
 * dev tools.
 */
public class ConfigCommand implements Command
{
    private static final Messages I18N = Messages.getInstance(ConfigCommand.class);

    private boolean projectOnly = false;

    @SuppressWarnings({"AccessStaticViaInstance"})
    public int execute(BootContext bootContext) throws Exception
    {
        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("project")
                .create('p'));

        CommandLineParser parser = new GnuParser();
        CommandLine commandLine = parser.parse(options, bootContext.getCommandArgv(), false);
        if(commandLine.hasOption('p'))
        {
            projectOnly = true;
        }

        ConsoleUI ui = new ConsoleUI();
        PersonalBuildConfig config = new PersonalBuildConfig(FileSystemUtils.getWorkingDirectory(), new PropertiesConfig(), ui);
        if(!projectOnly)
        {
            setupPulseConfig(ui, config);
        }

        setupLocalConfig(ui, config);
        return 0;
    }

    public void setupPulseConfig(PersonalBuildUI ui, PersonalBuildConfig config) throws PersonalBuildException
    {
        String pulseURL = getPulseURL(ui);
        String pulseUser = getPulseUser(ui);

        ui.status(I18N.format("status.storing.server", config.getUserConfigFile().getAbsolutePath()));
        config.setProperty(PersonalBuildConfig.PROPERTY_PULSE_URL, pulseURL);
        config.setProperty(PersonalBuildConfig.PROPERTY_PULSE_USER, pulseUser);
    }

    private String getPulseURL(PersonalBuildUI ui)
    {
        YesNoResponse response;
        String pulseURL;

        while (true)
        {
            pulseURL = ui.inputPrompt(I18N.format("prompt.pulse.url"));

            PulseXmlRpcClient rpc;
            try
            {
                rpc = new PulseXmlRpcClient(pulseURL);
            }
            catch (MalformedURLException e)
            {
                ui.error("Invalid URL: " + e.getMessage(), e);
                continue;
            }

            try
            {
                rpc.getVersion();

                // If we got here, it worked!
                break;
            }
            catch (PulseXmlRpcException e)
            {
                ui.error("Unable to contact pulse server: " + e, e);
                response = ui.yesNoPrompt(I18N.format("prompt.url.continue"), false, false, YesNoResponse.NO);
                if (response.isAffirmative())
                {
                    break;
                }
            }
        }

        return pulseURL;
    }

    private String getPulseUser(PersonalBuildUI ui)
    {
        String systemUser = System.getProperty("user.name");
        String pulseUser;

        String prompt = I18N.format("prompt.pulse.user");
        if (systemUser == null)
        {
            pulseUser = ui.inputPrompt(prompt);
        }
        else
        {
            pulseUser = ui.inputPrompt(prompt, systemUser);
        }
        return pulseUser;
    }

    public void setupLocalConfig(PersonalBuildUI ui, PersonalBuildConfig config) throws PersonalBuildException
    {
        String pulseProject = ui.inputPrompt(I18N.format("prompt.pulse.project"));

        ui.status("Storing project details in '" + config.getLocalConfigFile().getAbsolutePath() + "'.");
        config.setProperty(PersonalBuildConfig.PROPERTY_PROJECT, pulseProject, true);
    }

    public String getHelp()
    {
        return I18N.format("command.help");
    }

    public String getDetailedHelp()
    {
        return I18N.format("command.detailed.help");
    }

    public List<String> getUsages()
    {
        return Arrays.asList("");
    }

    public List<String> getAliases()
    {
        return Arrays.asList("conf", "configure");
    }

    public Map<String, String> getOptions()
    {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put("-p [--project]", I18N.format("flag.project"));
        return options;
    }

    public boolean isDefault()
    {
        return false;
    }
}
