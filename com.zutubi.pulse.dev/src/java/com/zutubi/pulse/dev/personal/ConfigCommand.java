package com.zutubi.pulse.dev.personal;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.Command;
import com.zutubi.pulse.core.ui.api.UserInterface;
import com.zutubi.pulse.dev.client.ClientException;
import com.zutubi.pulse.dev.config.DevConfigSetup;
import com.zutubi.pulse.dev.ui.ConsoleUI;
import com.zutubi.util.config.PropertiesConfig;
import org.apache.commons.cli.*;

import java.io.File;
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
        PersonalBuildConfig config = new PersonalBuildConfig(new File(System.getProperty("user.dir")), new PropertiesConfig(), ui);
        if(!projectOnly)
        {
            DevConfigSetup.setupPulseConfig(ui, config);
        }

        setupLocalConfig(ui, config);
        return 0;
    }

    public void setupLocalConfig(UserInterface ui, PersonalBuildConfig config) throws ClientException
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
