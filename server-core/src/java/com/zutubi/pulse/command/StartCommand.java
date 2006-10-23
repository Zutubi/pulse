package com.zutubi.pulse.command;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.SystemBootstrapManager;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.bootstrap.conf.EnvConfig;
import com.zutubi.pulse.util.logging.Logger;
import org.apache.commons.cli.*;

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

    @SuppressWarnings({"ACCESS_STATIC_VIA_INSTANCE", "AccessStaticViaInstance"})
    public void parse(String... argv) throws ParseException
    {
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("port")
                .withArgName("port")
                .hasArg()
                .withDescription("the port to be used by the pulse web interface.")
                .create('p'));

        options.addOption(OptionBuilder.withLongOpt("data")
                .withArgName("data")
                .hasArg()
                .withDescription("the pulse data directory.")
                .create('d'));

        options.addOption(OptionBuilder.withLongOpt("config")
                .withArgName("config")
                .hasArg()
                .withDescription("the pulse config file location.")
                .create('f'));

        options.addOption(OptionBuilder.withLongOpt("contextpath")
                .withArgName("contextpath")
                .hasArg()
                .withDescription("the web application's context path.")
                .create('c'));

        options.addOption(OptionBuilder.withLongOpt("bindaddress")
                .withArgName("address")
                .hasArg()
                .withDescription("the web application's bind address.")
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

    public static void main(String[] args) throws Exception
    {
        Command cmd = new StartCommand();
        cmd.parse(args);
        cmd.execute();
    }
}
