package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.Command;
import com.zutubi.pulse.core.personal.PersonalBuildException;
import com.zutubi.pulse.core.scm.api.PersonalBuildUI;
import com.zutubi.pulse.dev.xmlrpc.PulseXmlRpcClient;
import com.zutubi.pulse.dev.xmlrpc.PulseXmlRpcException;
import com.zutubi.util.config.PropertiesConfig;
import org.apache.commons.cli.*;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class ConfigCommand implements Command
{
    private boolean projectOnly = false;

    @SuppressWarnings({"AccessStaticViaInstance"})
    public int execute(BootContext bootContext) throws Exception
    {
        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("project")
                .create('p'));

        CommandLineParser parser = new PosixParser();
        CommandLine commandLine = parser.parse(options, bootContext.getCommandArgv());
        if(commandLine.hasOption('p'))
        {
            projectOnly = true;
        }

        ConsoleUI ui = new ConsoleUI();
        PersonalBuildConfig config = new PersonalBuildConfig(new File(System.getProperty("user.dir")), new PropertiesConfig(), ui);
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

        ui.status("Storing Pulse server details in '" + config.getUserConfigFile().getAbsolutePath() + "'.");
        config.setProperty(PersonalBuildConfig.PROPERTY_PULSE_URL, pulseURL);
        config.setProperty(PersonalBuildConfig.PROPERTY_PULSE_USER, pulseUser);
    }

    private String getPulseURL(PersonalBuildUI ui)
    {
        PersonalBuildUI.Response response;
        String pulseURL;

        while (true)
        {
            pulseURL = ui.inputPrompt("Pulse URL [e.g. http://pulse:8080]");

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
                response = ui.ynPrompt("Continue with this URL anyway?", PersonalBuildUI.Response.NO);
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

        if (systemUser == null)
        {
            pulseUser = ui.inputPrompt("Pulse user");
        }
        else
        {
            pulseUser = ui.inputPrompt("Pulse user", systemUser);
        }
        return pulseUser;
    }

    public void setupLocalConfig(PersonalBuildUI ui, PersonalBuildConfig config) throws PersonalBuildException
    {
        String pulseProject = ui.inputPrompt("Pulse project");

        ui.status("Storing project details in '" + config.getLocalConfigFile().getAbsolutePath() + "'.");
        config.setProperty(PersonalBuildConfig.PROPERTY_PROJECT, pulseProject, true);
    }

    public String getHelp()
    {
        return "configure pulse server and project";
    }

    public String getDetailedHelp()
    {
        return "Configures the Pulse server and project details used for personal builds.\n" +
                "You will be prompted to enter the required details, and they will be stored\n" +
                "in $HOME/.pulse2.properties (server details) and ./.pulse2.properties (project\n" +
                "details).  You should run this command from the base directory of a working\n" +
                "copy for your project.  You can set up additional working copies for other\n" +
                "projects using the -p flag, which indicates that you do not wish to\n" +
                "reconfigure the Pulse server details.";
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
        options.put("-p [--project]", "only configure project settings");
        return options;
    }

    public boolean isDefault()
    {
        return false;
    }
}
