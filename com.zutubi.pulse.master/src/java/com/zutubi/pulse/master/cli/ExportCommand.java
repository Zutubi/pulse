package com.zutubi.pulse.master.cli;

import com.zutubi.pulse.core.cli.HelpCommand;
import com.zutubi.pulse.servercore.cli.AdminCommand;
import org.apache.commons.cli.*;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.*;

/**
 * Command-line interface to RemoteApi.exportConfig, used for archiving configuration.
 */
public class ExportCommand extends AdminCommand
{
    private boolean append = true;

    public void setAppend(boolean append)
    {
        this.append = append;
    }

    @SuppressWarnings({"ACCESS_STATIC_VIA_INSTANCE"})
    private String[] parse(String... argv) throws ParseException
    {
        Options options = getSharedOptions();
        options.addOption(new Option("o", "overwrite", false, "overwrite existing file"));

        CommandLineParser parser = new GnuParser();
        CommandLine commandLine = parser.parse(options, argv, false);

        setAppend(!commandLine.hasOption('o'));
        processSharedOptions(commandLine);
        return commandLine.getArgs();
    }

    public int doExecute(String argv[]) throws IOException, ParseException
    {
        argv = parse(argv);

        if (argv.length < 2)
        {
            HelpCommand helpCommand = new HelpCommand();
            helpCommand.showHelp("export", this);
            return 1;
        }

        Vector<String> paths = new Vector<String>(Arrays.asList(argv).subList(1, argv.length));
        try
        {
            System.out.println("Exporting " + paths.size() + (paths.size() == 1 ? " entity" : " entities") + " to file '" + argv[0] + "'...");
            xmlRpcClient.execute("RemoteApi.exportConfig", new Vector<Object>(Arrays.asList(adminToken, argv[0], append, paths)));
            System.out.println("Export complete.");
        }
        catch (XmlRpcException e)
        {
            System.err.println(e.getMessage());
            System.exit(2);
        }

        return 0;
    }

    @Override
    public Map<String, String> getOptions()
    {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put("-o [--overwrite]", "overwrite (rather than add to) an existing file");
        options.putAll(super.getOptions());
        return options;
    }

    public List<String> getUsages()
    {
        return Arrays.asList("<output file> <configuration path>...");
    }

    public String getHelp()
    {
        return "exports a subset of your Pulse configuration to a file";
    }

    public String getDetailedHelp()
    {
        return "Exports a subset of your Pulse configuration to a file.  This data can\n" +
               "then be imported into another compatible Pulse instance.  If the file\n" +
                "already exists, by default the configuration will be added to it, use\n" +
                "the overwrite option to discard any existing file.";
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
