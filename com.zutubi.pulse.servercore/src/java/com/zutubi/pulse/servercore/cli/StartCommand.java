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

package com.zutubi.pulse.servercore.cli;

import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.Command;
import com.zutubi.pulse.core.util.config.EnvConfig;
import com.zutubi.pulse.servercore.bootstrap.SystemBootstrapManager;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import org.apache.commons.cli.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The start command is used as the entry point to starting the system.
 *
 * @author Daniel Ostermeier
 */
public class StartCommand implements Command
{
    private static final Logger LOG = Logger.getLogger(StartCommand.class);

    private static final int UNSPECIFIED = -1;

    /**
     * @deprecated the property should be taken from the command line OR the config file. Using the ENV variable
     * is discouraged.
     */
    private static final String ENV_PULSE_DATA = "PULSE_DATA";

    private static final String ENV_PULSE_CONFIG = "PULSE_CONFIG";

    /**
     * The port to which pulse will bind its web user interface.
     */
    private int port = UNSPECIFIED;

    /**
     * The pulse data directory
     */
    private String pulseData = null;

    private String contextPath = null;

    private String pulseConfig = null;

    private String bindAddress = null;

    private String restoreFile = null;

    private String restoreArtifacts = null;

    private boolean migrateRequested = false;

    /**
     * Specify the port to which pulse will bind its web user interface.
     *
     * @param port the available port number to bind the pulse web ui to.
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    public void setContextPath(String contextPath)
    {
        this.contextPath = contextPath;
    }

    /**
     *
     * @param data the path to the pulse data directory.
     */
    public void setData(String data)
    {
        this.pulseData = data;
    }

    public void setConfig(String path)
    {
        this.pulseConfig = path;
    }

    public void setBindAddress(String bindAddress)
    {
        this.bindAddress = bindAddress;
    }

    public void setRestoreArtifacts(String value)
    {
        this.restoreArtifacts = value;
    }

    public void setRestoreFile(String optionValue)
    {
        this.restoreFile = optionValue;
    }

    private void setMigrateRequested(boolean b)
    {
        this.migrateRequested = b;
    }

    @SuppressWarnings({ "ACCESS_STATIC_VIA_INSTANCE", "AccessStaticViaInstance" })
    private void parse(String... argv) throws ParseException
    {
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("port")
                .hasArg()
                .create('p'));

        options.addOption(OptionBuilder.withLongOpt("data")
                .hasArg()
                .create('d'));

        options.addOption(OptionBuilder.withLongOpt("config")
                .hasArg()
                .create('f'));

        options.addOption(OptionBuilder.withLongOpt("contextpath")
                .hasArg()
                .create('c'));

        options.addOption(OptionBuilder.withLongOpt("bindaddress")
                .hasArg()
                .create('b'));

        options.addOption(OptionBuilder.withLongOpt("restore")
                .hasArg()
                .create('r'));

        options.addOption(OptionBuilder.withLongOpt("restore-artifacts")
                .hasArg()
                .create('a'));

        options.addOption(OptionBuilder.withLongOpt("migrate")
                .create('m'));

        CommandLineParser parser = new GnuParser();
        CommandLine commandLine = parser.parse(options, argv, false);

        if (commandLine.hasOption('p'))
        {
            setPort(Integer.parseInt(commandLine.getOptionValue('p')));
        }
        if (commandLine.hasOption('d'))
        {
            setData(commandLine.getOptionValue('d'));
        }
        if (commandLine.hasOption('c'))
        {
            setContextPath(commandLine.getOptionValue('c'));
        }
        if (commandLine.hasOption('f'))
        {
            setConfig(commandLine.getOptionValue('f'));
        }
        if (commandLine.hasOption('b'))
        {
            setBindAddress(commandLine.getOptionValue('b'));
        }
        if (commandLine.hasOption('r'))
        {
            setRestoreFile(commandLine.getOptionValue('r'));
        }
        if (commandLine.hasOption('a'))
        {
            setRestoreArtifacts(commandLine.getOptionValue('a'));
        }
        if (commandLine.hasOption('m'))
        {
            setMigrateRequested(true);
        }
    }

    public int execute(BootContext context) throws ParseException
    {
        return execute(context.getCommandArgv());
    }

    public int execute(String... argv) throws ParseException
    {
        parse(argv);
        return execute();
    }

    public int execute()
    {
        try
        {
            // update the system properties
            if (StringUtils.stringSet(pulseData))
            {
                System.setProperty(SystemConfiguration.PULSE_DATA, pulseData);
            }
            else if(StringUtils.stringSet(System.getenv(ENV_PULSE_DATA)))
            {
                System.setProperty(SystemConfiguration.PULSE_DATA, System.getenv(ENV_PULSE_DATA));
            }

            if (StringUtils.stringSet(pulseConfig))
            {
                System.setProperty(EnvConfig.PULSE_CONFIG, pulseConfig);
            }
            else if (StringUtils.stringSet(System.getenv(ENV_PULSE_CONFIG)))
            {
                System.setProperty(EnvConfig.PULSE_CONFIG, System.getenv(ENV_PULSE_CONFIG));
            }

            if (port != UNSPECIFIED)
            {
                System.setProperty(SystemConfiguration.WEBAPP_PORT, Integer.toString(port));
            }

            if (StringUtils.stringSet(contextPath))
            {
                System.setProperty(SystemConfiguration.CONTEXT_PATH, contextPath);
            }

            if (StringUtils.stringSet(bindAddress))
            {
                System.setProperty(SystemConfiguration.WEBAPP_BIND_ADDRESS, bindAddress);
            }

            if (StringUtils.stringSet(restoreFile))
            {
                System.setProperty(SystemConfiguration.RESTORE_FILE, restoreFile);
            }

            if (StringUtils.stringSet(restoreArtifacts))
            {
                System.setProperty(SystemConfiguration.RESTORE_ARTIFACTS, restoreArtifacts);
            }

            if (migrateRequested)
            {
                System.setProperty("migrate.database", Boolean.TRUE.toString());
            }

            SystemBootstrapManager bootstrap = new SystemBootstrapManager();
            bootstrap.bootstrapSystem();
            return 0;
        }
        catch (Exception e)
        {
            LOG.error(e);
            return 1;
        }
    }

    public String getHelp()
    {
        return "start the pulse server";
    }

    public String getDetailedHelp()
    {
        return "Starts the pulse server, running in this console.  A message will be printed\n" +
               "when the server has started and the web interface is available.";
    }

    public List<String> getUsages()
    {
        return Arrays.asList("");
    }

    public List<String> getAliases()
    {
        return Arrays.asList("st");
    }

    public Map<String, String> getOptions()
    {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put("-p [--port] port", "the port to be used by the pulse web interface");
        options.put("-d [--data] dir", "use the specified directory for all pulse data");
        options.put("-c [--contextpath] path", "the pulse web application context path");
        options.put("-b [--bindaddress] addr", "the address to bind the server to");
        options.put("-f [--config] file", "specify an alternate config file");
        options.put("-r [--restore] file", "restore this pulse installation from the specified archive");
        options.put("-a [--restore-artifacts] dir", "restore the artifacts from the specified directory");
        options.put("-m [--migrate]", "trigger the database migration process on server startup");
        return options;
    }

    public boolean isDefault()
    {
        return false;
    }

    public static void main(String[] args) throws Exception
    {
        StartCommand cmd = new StartCommand();
        cmd.execute(args);
    }
}
