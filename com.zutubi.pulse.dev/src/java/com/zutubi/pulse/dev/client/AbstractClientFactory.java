/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.dev.client;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.util.config.CommandLineConfig;
import com.zutubi.pulse.dev.config.DevConfig;
import com.zutubi.pulse.dev.ui.ConsoleUI;
import com.zutubi.pulse.dev.util.OptionUtils;
import com.zutubi.util.config.CompositeConfig;
import com.zutubi.util.config.PropertiesConfig;
import com.zutubi.util.io.FileSystemUtils;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Helper base class for building configured clients that talk to the Pulse
 * server.
 */
public abstract class AbstractClientFactory<T>
{
    private static final Messages I18N = Messages.getInstance(AbstractClientFactory.class);

    /**
     * Returns a mapping of flags processed by this base class, useful as the
     * basis of a {@link com.zutubi.pulse.command.Command#getOptions()}
     * implementation.
     * 
     * @return documentation for the options supported by this factory
     */
    public static Map<String, String> getOptions()
    {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put("-s [--server] url", I18N.format("flag.server"));
        options.put("-u [--user] name", I18N.format("flag.user"));
        options.put("-p [--password] password", I18N.format("flag.password"));
        options.put("-b [--base-dir] dir", I18N.format("flag.base.dir"));
        options.put("-d [--define] name=value", I18N.format("flag.define"));
        options.put("-q [--quiet]", I18N.format("flag.quiet"));
        options.put("-v [--verbose]", I18N.format("flag.verbose"));
        return options;
    }
    
    /**
     * Builds a new client based on given command line arguments and
     * configuration files found on disk.  The supported arguments are
     * documented by {@link #getOptions()}, additional options may be added
     * in derived classes via {@link #addExtraOptions(com.zutubi.pulse.core.util.config.CommandLineConfig, org.apache.commons.cli.Options)}.
     * The configuration files are defined by {@link com.zutubi.pulse.dev.config.DevConfig}.
     *
     * @param arguments command line arguments
     * @return a client configured by the given arguments and config files
     * @throws org.apache.commons.cli.ParseException if the given command line arguments are invalid
     */
    public T newInstance(String... arguments) throws ParseException
    {
        CommandLineConfig switchConfig = new CommandLineConfig();
        CommandLine commandLine = parseCommandLine(switchConfig, arguments);

        File base = getBase(commandLine);

        switchConfig.setCommandLine(commandLine);
        PropertiesConfig defineConfig = processDefines(commandLine);
        CompositeConfig uiConfig = new CompositeConfig(switchConfig, defineConfig);

        return newInstance(base, uiConfig, configureConsole(commandLine), commandLine);
    }

    @SuppressWarnings({"AccessStaticViaInstance"})
    private CommandLine parseCommandLine(CommandLineConfig switchConfig, String... argv) throws ParseException
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

        addPropertyOption(switchConfig, options, 's', "server", DevConfig.PROPERTY_PULSE_URL);
        addPropertyOption(switchConfig, options, 'u', "user", DevConfig.PROPERTY_PULSE_USER);
        addPropertyOption(switchConfig, options, 'p', "password", DevConfig.PROPERTY_PULSE_PASSWORD);
        addExtraOptions(switchConfig, options);

        CommandLineParser parser = new GnuParser();
        return parser.parse(options, argv, false);
    }

    @SuppressWarnings({"AccessStaticViaInstance"})
    protected void addPropertyOption(CommandLineConfig switchConfig, Options options, char shortOption, String longOption, String property)
    {
        options.addOption(OptionBuilder.withLongOpt(longOption)
                .hasArg()
                .create(shortOption));
        switchConfig.mapSwitch(Character.toString(shortOption), property);
    }

    @SuppressWarnings({"AccessStaticViaInstance"})
    protected void addBooleanOption(CommandLineConfig switchConfig, Options options, String name, String property)
    {
        options.addOption(OptionBuilder.withLongOpt(name).create());
        options.addOption(OptionBuilder.withLongOpt(CommandLineConfig.INVERT_PREFIX + name).create());
        switchConfig.mapBoolean(name, property);
    }

    private ConsoleUI configureConsole(CommandLine commandLine)
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

    private File getBase(CommandLine commandLine) throws ParseException
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

    private PropertiesConfig processDefines(CommandLine commandLine) throws ParseException
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

    protected abstract T newInstance(File base, CompositeConfig uiConfig, ConsoleUI ui, CommandLine commandLine) throws ParseException;
    protected abstract void addExtraOptions(CommandLineConfig switchConfig, Options options);
}
