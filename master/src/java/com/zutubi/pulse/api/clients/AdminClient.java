/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.api.clients;

import org.apache.commons.cli.*;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.Arrays;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.File;

import com.zutubi.pulse.bootstrap.SystemBootstrapManager;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.api.TokenManager;
import com.zutubi.pulse.util.IOUtils;

/**
 * <class-comment/>
 */
public class AdminClient
{
    private Map<String, Command> commands = new HashMap<String, Command>();

    public AdminClient()
    {
        // setup the setpassword options
        commands.put("setpassword", new SetPasswordCommand());
        commands.put("shutdown", new ShutdownCommand());
    }

    public int process(String[] argv)
    {
        try
        {
            if (argv.length == 0)
            {
                printHelp();
                return 1;
            }

            // validate the requested command.
            String commandName = argv[0];

            if (!commands.containsKey(commandName))
            {
                // print warning, unknown command requested.
                printError("Unknown command " + commandName);
                printHelp();
                return 2;
            }

            String[] commandArgs = new String[argv.length - 1];
            System.arraycopy(argv, 1, commandArgs, 0, commandArgs.length);

            Command command = commands.get(commandName);
            command.parse(commandArgs);
            return command.execute();
        }
        catch (Exception e)
        {
            printError(e.getMessage());
            return 1;
        }
    }

    private void printError(String msg)
    {
        System.err.println(msg);
    }

    private void printHelp()
    {
        System.err.println("The following admin commands are available:");
        System.err.println("    shutdown:\t\t\tshutdown the server.");
        System.err.println("    setpassword:\t\t\tset a users password.");
        System.err.println("To see specific help information about any of these commands, type admin 'command' --help");
    }

    public static void main(String argv[])
    {
        System.exit(new AdminClient().process(argv));
    }

    /**
     *
     */
    public interface Command
    {
        /**
         * Parse the command line arguments, recording any information required to
         * execute this command.
         *
         * @param commandArgs
         *
         * @throws ParseException is thrown if a problem is detected with the command args
         */
        public void parse(String commandArgs[]) throws ParseException;

        /**
         * Execute this command
         *
         * @return the commands exit code. Return 0 if the command completed successfully,
         * any non-zero number if it not.
         */
        public int execute();
    }

    /**
     * The abstract base command for commands that are run on the same host as the
     * running server and that require the admin token to authenticate themselves.
     *
     */
    public abstract class AdminCommand implements Command
    {
        /**
         * The xml rpc client used to connect to the server.
         */
        protected XmlRpcClient xmlRpcClient;

        /**
         * The admin token that allows the xml rpc request to be authenticated as a local request.
         */
        protected String adminToken;

        private String loadAdminToken(ConfigurationManager configurationManager) throws IOException
        {
            File tokenFile = TokenManager.getAdminTokenFilename(configurationManager);
            return IOUtils.fileToString(tokenFile);
        }

        public int execute()
        {
            // initialise the necessary resources
            // a) the xml rpc client
            // b) the admin token.

            SystemBootstrapManager.loadBootstrapContext();
            ConfigurationManager configurationManager = (ConfigurationManager) ComponentContext.getBean("configurationManager");

            URL url;
            try
            {
                int webPort = configurationManager.getAppConfig().getServerPort();
                url = new URL("http", "127.0.0.1", webPort, "/xmlrpc");
            }
            catch (MalformedURLException e)
            {
                // Programmer error
                e.printStackTrace();
                return 1;
            }

            xmlRpcClient = new XmlRpcClient(url);

            try
            {
                adminToken = loadAdminToken(configurationManager);
                return doExecute();
            }
            catch (IOException e)
            {
                System.err.println("I/O Error: " + e.getMessage());
                return 1;
            }
            catch (XmlRpcException e)
            {
                System.err.println("Unable to send shutdown command to server: " + e.getMessage());
                return 1;
            }
        }

        /**
         * Admin command implementations should implement there custom functionality
         * in this method. When this method is invoked, both the XmlRpcClient and the
         * AdminToken will be available.
         */
        public abstract int doExecute() throws XmlRpcException, IOException;

    }

    /**
     *
     */
    public class SetPasswordCommand extends AdminCommand
    {
        private String user;
        private String password;

        @SuppressWarnings({"ACCESS_STATIC_VIA_INSTANCE"})
        public void parse(String argv[]) throws ParseException
        {
            Options options = new Options();
            options.addOption(OptionBuilder.withLongOpt("user")
                    .withArgName("username")
                    .hasArg()
                    .withDescription("the user whose password is being set")
                    .isRequired()
                    .create('u'));
            options.addOption(OptionBuilder.withLongOpt("password")
                    .withArgName("password")
                    .hasArg()
                    .withDescription("the new password")
                    .isRequired()
                    .create('p'));


            CommandLineParser parser = new PosixParser();
            CommandLine commandLine = parser.parse(options, argv, true);

            // process the command.
            user = commandLine.getOptionValue('u');
            password = commandLine.getOptionValue('p');
        }

        public int doExecute() throws XmlRpcException, IOException
        {
            xmlRpcClient.execute("RemoteApi.setPassword", new Vector<Object>(Arrays.asList(
                    new Object[]{adminToken, user, password})));
            return 0;
        }
    }

    /**
     * The shutdown admin command, supports the force argument.
     *
     */
    public class ShutdownCommand extends AdminCommand
    {
        private boolean force;

        @SuppressWarnings({"ACCESS_STATIC_VIA_INSTANCE"})
        public void parse(String argv[]) throws ParseException
        {
            Options options = new Options();
            options.addOption(new Option("force", "force shutdown"));

            CommandLineParser parser = new PosixParser();
            CommandLine commandLine = parser.parse(options, argv, true);
            force = (commandLine.hasOption("force"));
        }

        public int doExecute() throws XmlRpcException, IOException
        {
            xmlRpcClient.execute("RemoteApi.shutdown", new Vector<Object>(Arrays.asList(new Object[]{adminToken, force})));
            return 0;
        }
    }
}
