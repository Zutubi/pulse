package com.zutubi.pulse.command;

import org.apache.commons.cli.*;
import com.zutubi.pulse.bootstrap.SystemBootstrapManager;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.util.logging.Logger;
import com.opensymphony.util.TextUtils;

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
     * The port to which pulse will bind its web user interface.
     */
    private int port = UNSPECIFIED;

    /**
     * The pulse data directory
     */
    private String data;
    private static final String ENV_PULSE_DATA = "PULSE_DATA";

    private String contextPath;

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
        this.data = data;
    }

    @SuppressWarnings({"ACCESS_STATIC_VIA_INSTANCE"})
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

        options.addOption(OptionBuilder.withLongOpt("contextpath")
                .withArgName("contextpath")
                .hasArg()
                .withDescription("the webapps context path.")
                .create('c'));

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
    }

    public int execute()
    {
        try
        {
            // update the system properties
            if (port != UNSPECIFIED)
            {
                System.setProperty(SystemConfiguration.WEBAPP_PORT, Integer.toString(port));
            }

            if (TextUtils.stringSet(data))
            {
                System.setProperty("pulse.data", data);
            }
            else if(TextUtils.stringSet(System.getenv(ENV_PULSE_DATA)))
            {
                System.setProperty("pulse.data", System.getenv(ENV_PULSE_DATA));
            }

            if (TextUtils.stringSet(contextPath))
            {
                System.setProperty(SystemConfiguration.CONTEXT_PATH, contextPath);
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
