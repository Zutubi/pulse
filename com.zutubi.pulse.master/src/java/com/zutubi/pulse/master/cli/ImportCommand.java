package com.zutubi.pulse.master.cli;

import com.zutubi.pulse.core.cli.HelpCommand;
import com.zutubi.pulse.servercore.cli.AdminCommand;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Imports configuration from a previously-exported archive file.
 */
public class ImportCommand extends AdminCommand
{

    public int doExecute(String[] argv) throws IOException, ParseException
    {
        CommandLineParser parser = new GnuParser();
        Options options = getSharedOptions();
        options.addOption(new Option("d", "debug", false, "show extra debugging output"));

        CommandLine commandLine = parser.parse(options, argv, false);
        processSharedOptions(commandLine);
        boolean debug = commandLine.hasOption('d');
        argv = commandLine.getArgs();

        if (argv.length != 1)
        {
            HelpCommand helpCommand = new HelpCommand();
            helpCommand.showHelp("import", this);
            return 1;
        }

        File inputFile = new File(argv[0]);
        if (!inputFile.exists())
        {
            System.err.println("Input file '" + argv[0] + "' does not exist");
            System.exit(1);
        }

        try
        {
            System.out.println("Importing from '" + inputFile.getAbsolutePath() + "'...");
            @SuppressWarnings("unchecked")
            Vector<String> paths = (Vector<String>) xmlRpcClient.execute("RemoteApi.importConfig", new Vector<Object>(Arrays.asList(adminToken, argv[0])));
            System.out.println("Imported " + paths.size() + (paths.size() == 1 ? " entity:" : " entities:"));
            for (String path : paths)
            {
                System.out.println("  " + path);
            }
        }
        catch (Exception e)
        {
            if (debug)
            {
                e.printStackTrace(System.err);
            }

            System.err.println(e.getMessage());
            System.exit(2);
        }

        return 0;
    }

    @Override
    public Map<String, String> getOptions()
    {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put("-d [--debug]", "show extra debugging output");
        options.putAll(super.getOptions());
        return options;
    }

    public List<String> getUsages()
    {
        return Arrays.asList("<input file>");
    }

    public String getHelp()
    {
        return "imports archived Pulse configuration from a file";
    }

    public String getDetailedHelp()
    {
        return "Imports the configuration in the given file to this Pulse instance.\n" +
                "The data must have been exported from a compatible version of Pulse.\n" +
                "If any of the imported items conflict with existing configuration,\n" +
                "the conflict is resolved by appending (restored) to the name.";
    }

    public List<String> getAliases()
    {
        return Arrays.asList("im", "imp");
    }

    public static void main(String argv[])
    {
        ImportCommand command = new ImportCommand();
        try
        {
            command.execute(argv);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
