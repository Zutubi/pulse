package com.zutubi.pulse.master.cli;

import com.zutubi.pulse.core.cli.HelpCommand;
import com.zutubi.pulse.servercore.cli.AdminCommand;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.apache.xmlrpc.XmlRpcException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * Imports configuration from a previously-exported archive file.
 */
public class ImportCommand extends AdminCommand
{
    public int doExecute(String[] argv) throws IOException, ParseException
    {
        CommandLineParser parser = new GnuParser();
        CommandLine commandLine = parser.parse(getSharedOptions(), argv, false);
        processSharedOptions(commandLine);
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
            xmlRpcClient.execute("RemoteApi.importConfig", new Vector<Object>(Arrays.asList(adminToken, argv[0])));
        }
        catch (XmlRpcException e)
        {
            System.err.println(e.getMessage());
            System.exit(2);
        }

        return 0;
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
