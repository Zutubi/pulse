package com.zutubi.pulse.api.clients;

import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.Command;
import com.zutubi.pulse.command.ShutdownCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public class AdminClient
{
    private Map<String, Command> commands = new HashMap<String, Command>();

    public AdminClient()
    {
        // setup the setpassword options
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

            Command command = commands.get(commandName);
            return command.execute(new BootContext(null, null, argv, null, null, null));
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
        System.err.println("To see specific help information about any of these commands, type admin 'command' --help");
    }

    public static void main(String argv[])
    {
        System.exit(new AdminClient().process(argv));
    }
}
