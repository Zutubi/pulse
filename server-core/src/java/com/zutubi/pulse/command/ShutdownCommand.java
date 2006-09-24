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

    private boolean exitJvm = true;

    public void setForce(boolean b)
    {
        this.force = b;
    }

    public void setExitJvm(boolean exitJvm)
    {
        this.exitJvm = exitJvm;
    }

    @SuppressWarnings({"ACCESS_STATIC_VIA_INSTANCE"})
    public void parse(String... argv) throws ParseException
    {
        Options options = new Options();
        options.addOption(new Option("F", "force", false, "force shutdown"));
        options.addOption(OptionBuilder.withLongOpt("port")
                .withArgName("port")
                .hasArg()
                .withDescription("the port to be used by the pulse web interface.")
                .create('p'));
        options.addOption(OptionBuilder.withLongOpt("contextpath")
                .withArgName("contextpath")
                .hasArg()
                .withDescription("the webapps context path.")
                .create('c'));
        options.addOption(OptionBuilder.withLongOpt("config")
                .withArgName("config")
                .hasArg()
                .withDescription("the pulse config file location.")
                .create('f'));

        CommandLineParser parser = new PosixParser();
        CommandLine commandLine = parser.parse(options, argv, true);
        
        setForce(commandLine.hasOption('F'));

        if (commandLine.hasOption('p'))
        {
            setPort(Integer.parseInt(commandLine.getOptionValue('p')));
        }
        if (commandLine.hasOption('c'))
        {
            setContextPath(commandLine.getOptionValue('c'));
        }
        if (commandLine.hasOption('f'))
        {
            setConfig(commandLine.getOptionValue('f'));
        }
    }

    public int doExecute() throws XmlRpcException, IOException
    {
        xmlRpcClient.execute("RemoteApi.shutdown", new Vector<Object>(Arrays.asList(new Object[]{adminToken, force, exitJvm})));
        return 0;
    }
}