package com.zutubi.pulse.command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.hsqldb.util.TransferAPI;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 */
public class ImportCommand extends DataCommand
{
    public int doExecute(BootContext context) throws IOException, ParseException
    {
        CommandLineParser parser = new PosixParser();
        CommandLine commandLine = parser.parse(getSharedOptions(), context.getCommandArgv(), true);

        String[] args = commandLine.getArgs();
        if (args.length == 0)
        {
            HelpCommand helpCommand = new HelpCommand();
            helpCommand.showHelp(context.getArgv()[0], this);
            return 1;
        }

        File inFile = new File(args[0]);
        if (!inFile.exists())
        {
            System.err.println("Input file '" + inFile.getPath() + "' does not exist");
            return 2;
        }

        TransferAPI transferAPI = new TransferAPI();
        try
        {
            transferAPI.restore(inFile, dataSource);
        }
        catch (Exception e)
        {
            System.err.println("Error importing data.  Trace below:");
            e.printStackTrace(System.err);
            return 2;
        }

        return 0;
    }

    public List<String> getUsages()
    {
        return Arrays.asList("<input file>");
    }

    public String getHelp()
    {
        return "imports the Pulse database from a file";
    }

    public String getDetailedHelp()
    {
        return "Imports the data in the given file to the current Pulse database.  This\n" +
               "data should previously have been exported from Pulse, and the target\n" +
               "database should be empty.";
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
            command.execute(new BootContext(null, argv, null, null, null));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
