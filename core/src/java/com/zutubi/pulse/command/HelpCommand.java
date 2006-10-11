package com.zutubi.pulse.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A command used to print documentation about the available commands.
 */
public class HelpCommand implements Command
{
    public int execute(BootContext context) throws Exception
    {
        // With no args, or when help is invoked due to an unrecognised
        // command, show top level help.  With args, use arg name as
        // command to show help for.
        String[] argv = context.getCommandArgv();
        if(argv.length == 0 || !argv[0].equals("help"))
        {
            showHelp(context.getCommands());
        }
        else
        {
            Map<String, Command> aliasCommands = new HashMap<String, Command>(context.getCommands());
            for(Command command: context.getCommands().values())
            {
                List<String> aliases = command.getAliases();
                if(aliases != null)
                {
                    for(String alias: aliases)
                    {
                        aliasCommands.put(alias, command);
                    }
                }
            }

            Command command = aliasCommands.get(argv[0]);
            if(command == null || command.getHelp() == null)
            {
                System.err.println("Unrecognised command '" + argv[0] + "'");
                return 1;
            }
            else
            {
                showHelp(argv[0], command);
            }
        }

        return 0;
    }


    public String getHelp()
    {
        return "show command documentation";
    }

    public String getDetailedHelp()
    {
        return "Displays usage and parameter information for available commands.";
    }

    public List<String> getUsages()
    {
        return Arrays.asList(new String[] { "[ <command> ]" });
    }

    public List<String> getAliases()
    {
        return null;
    }

    public Map<String, String> getOptions()
    {
        return null;
    }

    public boolean isDefault()
    {
        return true;
    }

    private void showHelp(Map<String, Command> commands)
    {
        System.out.println("Usage: pulse <command> [ <option> ... ] [ <arg> ... ]");
        System.out.println();
        System.out.println("Available commands:");
        for(Map.Entry<String, Command> entry: commands.entrySet())
        {
            Command command = entry.getValue();
            if(command.getHelp() != null)
            {
                System.out.println(String.format("  %-16s: %s", entry.getKey(), entry.getValue().getHelp()));
            }
        }
        System.out.println();
        System.out.println("For help on a specific command, type 'pulse help <command>'");
    }

    public void showHelp(String name, Command command)
    {
        System.out.println(name + ": " + command.getHelp());
        List<String> aliases = command.getAliases();
        if(aliases != null)
        {
            System.out.println("Aliases: " + aliases.toString());
        }

        String usageString = "Usage:";
        for(String usage: command.getUsages())
        {
            System.out.println(String.format("%s %s %s", usageString, name, usage));
            usageString = "      ";
        }

        System.out.println();
        System.out.println(command.getDetailedHelp());

        Map<String, String> options = command.getOptions();
        if(options != null)
        {
            System.out.println();
            System.out.println("Available options:");
            for(Map.Entry<String, String> option: options.entrySet())
            {
                System.out.println(String.format("%-26s: %s", option.getKey(), option.getValue()));
            }
        }

        System.out.println();
    }
}
