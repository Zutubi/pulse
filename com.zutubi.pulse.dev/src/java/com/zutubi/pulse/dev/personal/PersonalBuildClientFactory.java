package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.core.util.config.CommandLineConfig;
import com.zutubi.pulse.dev.util.OptionUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.config.CompositeConfig;
import com.zutubi.util.config.PropertiesConfig;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.Properties;

/**
 * A factory for building configured {@link PersonalBuildClient} instances.
 */
public class PersonalBuildClientFactory
{
    /**
     * Builds a new client based on given command line arguments and
     * configuration files found on disk.  The supported arguments are
     * documented by {@link PersonalBuildCommand}, and the configuration files
     * are defined by {@link PersonalBuildConfig}.
     *
     * @param arguments command line arguments
     * @return a client configured by the given arguments and config files
     * @throws ParseException if the given command line arguments are invalid
     */
    public static PersonalBuildClient newInstance(String... arguments) throws ParseException
    {
        CommandLineConfig switchConfig = new CommandLineConfig();
        CommandLine commandLine = parseCommandLine(switchConfig, arguments);

        File base = getBase(commandLine);

        switchConfig.setCommandLine(commandLine);
        PropertiesConfig defineConfig = processDefines(commandLine);
        CompositeConfig uiConfig = new CompositeConfig(switchConfig, defineConfig);

        ConsoleUI ui = configureConsole(commandLine);
        PersonalBuildConfig config = new PersonalBuildConfig(base, uiConfig, ui, commandLine.getArgs());
        return new PersonalBuildClient(config, ui);
    }

    @SuppressWarnings({"AccessStaticViaInstance"})
    private static CommandLine parseCommandLine(CommandLineConfig switchConfig, String... argv) throws ParseException
    {
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("quiet")
                .create('q'));
        options.addOption(OptionBuilder.withLongOpt("verbose")
                .create('v'));
        options.addOption(OptionBuilder.withLongOpt("define")
                .hasArg()
                .create('d'));
        options.addOption(OptionBuilder.withLongOpt("base-dir")
                .hasArg()
                .create('b'));

        addPropertyOption(switchConfig, options, 's', "server", PersonalBuildConfig.PROPERTY_PULSE_URL);
        addPropertyOption(switchConfig, options, 'u', "user", PersonalBuildConfig.PROPERTY_PULSE_USER);
        addPropertyOption(switchConfig, options, 'p', "password", PersonalBuildConfig.PROPERTY_PULSE_PASSWORD);
        addPropertyOption(switchConfig, options, 'r', "project", PersonalBuildConfig.PROPERTY_PROJECT);
        addPropertyOption(switchConfig, options, 'e', "revision", PersonalBuildConfig.PROPERTY_REVISION);
        addPropertyOption(switchConfig, options, 'f', "file", PersonalBuildConfig.PROPERTY_PATCH_FILE);
        addPropertyOption(switchConfig, options, 't', "patch-type", PersonalBuildConfig.PROPERTY_PATCH_TYPE);

        addBooleanOption(switchConfig, options, "send-request", PersonalBuildConfig.PROPERTY_SEND_REQUEST);
        addBooleanOption(switchConfig, options, "update", PersonalBuildConfig.PROPERTY_UPDATE);

        CommandLineParser parser = new GnuParser();
        return parser.parse(options, argv, false);
    }

    @SuppressWarnings({"AccessStaticViaInstance"})
    private static void addPropertyOption(CommandLineConfig switchConfig, Options options, char shortOption, String longOption, String property)
    {
        options.addOption(OptionBuilder.withLongOpt(longOption)
                .hasArg()
                .create(shortOption));
        switchConfig.mapSwitch(Character.toString(shortOption), property);
    }

    @SuppressWarnings({"AccessStaticViaInstance"})
    private static void addBooleanOption(CommandLineConfig switchConfig, Options options, String name, String property)
    {
        options.addOption(OptionBuilder.withLongOpt(name).create());
        options.addOption(OptionBuilder.withLongOpt(CommandLineConfig.INVERT_PREFIX + name).create());
        switchConfig.mapBoolean(name, property);
    }

    private static ConsoleUI configureConsole(CommandLine commandLine)
    {
        ConsoleUI ui = new ConsoleUI();
        if (commandLine.hasOption('q'))
        {
            ui.setVerbosity(ConsoleUI.Verbosity.QUIET);
        }
        if (commandLine.hasOption('v'))
        {
            ui.setVerbosity(ConsoleUI.Verbosity.VERBOSE);
        }
        return ui;
    }

    private static File getBase(CommandLine commandLine) throws ParseException
    {
        File base;
        if(commandLine.hasOption('b'))
        {
            String baseName = commandLine.getOptionValue('b');
            base = new File(baseName);
            if (!base.isDirectory())
            {
                throw new ParseException("Base directory specified '" + baseName + "' is not a directory");
            }
        }
        else
        {
            base = FileSystemUtils.getWorkingDirectory();
        }
        return base;
    }

    private static PropertiesConfig processDefines(CommandLine commandLine) throws ParseException
    {
        Properties defines = new Properties();
        if (commandLine.hasOption('d'))
        {
            String[] values = commandLine.getOptionValues('d');
            for (String value: values)
            {
                OptionUtils.addDefinedOption(value, defines);
            }
        }
        return new PropertiesConfig(defines);
    }
}
