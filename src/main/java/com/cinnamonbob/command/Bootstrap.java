package com.cinnamonbob.command;

import com.cinnamonbob.BobServer;
import com.cinnamonbob.ShutdownService;
import com.cinnamonbob.util.IOHelper;
import org.apache.xmlrpc.XmlRpcClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Daniel Ostermeier
 */
public class Bootstrap
{

    private static final Logger LOG = Logger.getLogger(Bootstrap.class.getName());

    //TODO: support command the form ... options COMMAND command-options ARGUMENTS

    // server connection details.
    private int port = 2345;
    private String host = "localhost";

    public void parse(String args[]) throws Exception
    {
        if (args.length == 0)
        {
            return;
        }

        //TODO: support configuration of host and port settings.
        String command = args[0];
        if ("start".equals(command))
        {
            start();
        } else if ("stop".equals(command))
        {
            stop();
        } else if ("build".equals(command))
        {
            build(args[1]);
        }
    }

    public static void main(String argv[]) throws Exception
    {
        new Bootstrap().parse(argv);
    }

    public void start() throws Exception
    {
        BobServer server = new BobServer(port);
        server.start();
    }

    public void stop()
    {
        // connect to the admin port and send the shutdown command...
        Socket socket = null;
        try {
            socket = new Socket(host, port);

            // send the shutdown command.
            OutputStream out = socket.getOutputStream();
            out.write(ShutdownService.Command.SHUTDOWN.getBytes());
            out.close();

        } catch (IOException e) {
            LOG.log(Level.WARNING, "Unable to send shutdown request", e);

        } finally {
            IOHelper.close(socket);
        }

    }

    public void build(String projectName) throws Exception
    {
        XmlRpcClient client = new XmlRpcClient("http://localhost:8080/api/xmlrpc");
        Vector<String> params = new Vector<String>();
        params.add(projectName);
        client.execute("build", params);
    }

}
