package com.zutubi.pulse.command;

import org.apache.commons.cli.*;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Vector;
import java.util.Arrays;


/**
 * The shutdown admin command, supports the force argument.
 *
 * @author Daniel Ostermeier
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