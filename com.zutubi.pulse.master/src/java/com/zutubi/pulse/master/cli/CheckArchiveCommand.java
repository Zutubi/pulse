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
 * Checks a previously-exported archive file and lists its contents.
 */
public class CheckArchiveCommand extends AdminCommand
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
            System.out.println("Checking archive '" + inputFile.getAbsolutePath() + "'...");
            @SuppressWarnings("unchecked")
            Vector<String> paths = (Vector<String>) xmlRpcClient.execute("RemoteApi.checkConfigArchive", new Vector<Object>(Arrays.asList(adminToken, argv[0])));
            System.out.println("Archive contains " + paths.size() + (paths.size() == 1 ? " entity:" : " entities:"));
            for (String path : paths)
            {
                System.out.println("  " + path);
            }
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
        return "checks and lists a Pulse configuration archive file";
    }

    public String getDetailedHelp()
    {
        return "Checks that a configuration archive file is valid and may be\n" +
                "imported into this version of Pulse.  If so, the contents of the\n" +
                "archive are listed.";
    }

    public List<String> getAliases()
    {
        return Arrays.asList("ca", "char");
    }

    public static void main(String argv[])
    {
        CheckArchiveCommand command = new CheckArchiveCommand();
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
