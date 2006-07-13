package com.zutubi.pulse.command;

import org.apache.commons.cli.*;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

/**
 * The ping server command sends a ping request to the server, useful for checking the
 * pulse servers availability.
 */
public class PingServerCommand implements Command
{
    private String host;

    private int port = 8080;

    /**
     * The host address.
     *
     * @param host
     */
    public void setHost(String host)
    {
        this.host = host;
    }

    /**
     * The port.
     *
     * @param port
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    public void parse(String... argv) throws Exception
    {
        Options options = new Options();
        CommandLineParser parser = new PosixParser();
        CommandLine cl = parser.parse(options, argv, true);

        // TODO: is there some way that the following requirement can be handled by the parser?
        // the left over args are the host to ping.
        String[] args = cl.getArgs();
        if (args.length == 0)
        {
            throw new ParseException("Expected one argument.");
        }
        
        setHost(args[0]);
        if (args.length > 1)
        {
            setPort(Integer.parseInt(args[1]));
        }
    }

    public int execute()
    {
        // ping each of these urls.
        try
        {
            URL url = new URL("http", host, port, "/xmlrpc");
            XmlRpcClient client = new XmlRpcClient(url);
            client.execute("RemoteApi.ping", new Vector<Object>());
            return 0;
        }
        catch (MalformedURLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return 1;
        }
        catch (IOException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return 2;
        }
        catch (XmlRpcException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return 3;
        }
    }
}
