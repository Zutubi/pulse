package com.zutubi.pulse.command;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.SystemBootstrapManager;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.bootstrap.conf.EnvConfig;
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

    /**
     * Specify the port to which pulse will bind its web user interface.
     *
     * @param port
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
     * @param data
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

        CommandLineParser parser = new PosixParser();
        CommandLine commandLine = parser.parse(options, argv, true);

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
    }

    public int execute(BootContext context) throws ParseException
    {
        return execute(context.getCommandArgv());
    }

    public int execute(String[] argv) throws ParseException
    {
        parse(argv);
        return execute();
    }

    public int execute()
    {
        try
        {
            // update the system properties
            if (TextUtils.stringSet(pulseData))
            {
                System.setProperty(SystemConfiguration.PULSE_DATA, pulseData);
            }
            else if(TextUtils.stringSet(System.getenv(ENV_PULSE_DATA)))
            {
                System.setProperty(SystemConfiguration.PULSE_DATA, System.getenv(ENV_PULSE_DATA));
            }

            if (TextUtils.stringSet(pulseConfig))
            {
                System.setProperty(EnvConfig.PULSE_CONFIG, pulseConfig);
            }
            else if (TextUtils.stringSet(System.getenv(ENV_PULSE_CONFIG)))
            {
                System.setProperty(EnvConfig.PULSE_CONFIG, System.getenv(ENV_PULSE_CONFIG));
            }

            if (port != UNSPECIFIED)
            {
                System.setProperty(SystemConfiguration.WEBAPP_PORT, Integer.toString(port));
            }

            if (TextUtils.stringSet(contextPath))
            {
                System.setProperty(SystemConfiguration.CONTEXT_PATH, contextPath);
            }

            if (TextUtils.stringSet(bindAddress))
            {
                System.setProperty(SystemConfiguration.WEBAPP_BIND_ADDRESS, bindAddress);
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
        return Arrays.asList(new String[] { "" });
    }

    public List<String> getAliases()
    {
        return Arrays.asList(new String[] { "st" });
    }

    public Map<String, String> getOptions()
    {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put("-p [--port] port", "the port to be used by the pulse web interface");
        options.put("-d [--data] dir", "use the specified directory for all pulse data");
        options.put("-c [--contextpath] path", "the pulse web application context path");
        options.put("-b [--bindaddress] addr", "the address to bind the server to");
        options.put("-f [--config] file", "specify an alternate config file");
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
