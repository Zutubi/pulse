package com.zutubi.pulse.command;

import com.zutubi.pulse.transfer.TransferAPI;
import com.zutubi.pulse.transfer.TransferException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 */
public class ImportCommand extends DataCommand
{
    public int doExecute(CommandLine commandLine) throws IOException, ParseException
    {
        String[] args = commandLine.getArgs();
        if (args.length != 1)
        {
            HelpCommand helpCommand = new HelpCommand();
            helpCommand.showHelp("import", this);
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
            transferAPI.restore(configuration, inFile, dataSource);
        }
        catch (TransferException e)
        {
            System.err.println("Error importing data from database located at "+ databaseConfig.getUrl() +".  Trace below:");
            e.printStackTrace(System.err);
            return 2;
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
            command.execute(argv);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
