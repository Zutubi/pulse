package com.zutubi.pulse.master.command;

import com.zutubi.pulse.core.command.HelpCommand;
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
public class ExportCommand extends DataCommand
{
    public int doExecute(CommandLine commandLine) throws IOException, ParseException
    {
        String[] args = commandLine.getArgs();
        if (args.length != 1)
        {
            HelpCommand helpCommand = new HelpCommand();
            helpCommand.showHelp("export", this);
            return 1;
        }

        File outFile = new File(args[0]);
        if (outFile.exists() && !outFile.delete())
        {
            System.err.println("Unable to remove existing file '" + outFile.getPath() + "'");
            return 2;
        }

        try
        {
            if (!outFile.createNewFile())
            {
                System.err.println("Unable to create output file '" + outFile.getPath() + "'");
                return 2;
            }
        }
        catch (IOException e)
        {
            System.err.println("Unable to create output file '" + outFile.getPath() + "': " + e.getMessage());
            return 2;
        }

        TransferAPI transferAPI = new TransferAPI();
        try
        {
            transferAPI.dump(configuration, dataSource, outFile);
        }
        catch (TransferException e)
        {
            System.err.println("Error exporting data from database located at "+ databaseConfig.getUrl() +".  Trace below:");
            e.printStackTrace(System.err);
            return 2;
        }
        catch (Exception e)
        {
            System.err.println("Error exporting data.  Trace below:");
            e.printStackTrace(System.err);
            return 2;
        }

        return 0;
    }

    public List<String> getUsages()
    {
        return Arrays.asList("<output file>");
    }

    public String getHelp()
    {
        return "exports the Pulse database to a file";
    }

    public String getDetailedHelp()
    {
        return "Exports the data in the current Pulse database to a file.  This data can\n" +
               "then be imported into another database (after updating your Pulse\n" +
               "configuration).";
    }

    public List<String> getAliases()
    {
        return Arrays.asList("ex", "exp");
    }

    public static void main(String argv[])
    {
        ExportCommand command = new ExportCommand();
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
