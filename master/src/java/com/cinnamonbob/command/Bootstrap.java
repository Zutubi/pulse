package com.cinnamonbob.command;

import com.cinnamonbob.BobServer;
import com.cinnamonbob.ShutdownService;
import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.core.util.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;


/**
 * @author Daniel Ostermeier
 */
public class Bootstrap
{
    private static final Logger LOG = Logger.getLogger(Bootstrap.class);

    //TODO: support command the form ... options COMMAND command-options ARGUMENTS

    // server connection details.
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
        }
        else if ("stop".equals(command))
        {
            stop();
        }
        else
        {
            LOG.warning("Unrecognised command '" + command + "'");
        }
    }

    public static void main(String argv[]) throws Exception
    {
        new Bootstrap().parse(argv);
    }

    public void start() throws Exception
    {
        BobServer server = new BobServer();
        server.start();
    }

    public void stop()
    {
        // connect to the admin port and send the shutdown command...
        Socket socket = null;
        try
        {
            socket = new Socket(host, 8081);

            // send the shutdown command.
            OutputStream out = socket.getOutputStream();
            out.write(ShutdownService.Command.SHUTDOWN.getBytes());
            out.close();

        }
        catch (IOException e)
        {
            LOG.warning("Unable to send shutdown request", e);

        }
        finally
        {
            IOUtils.close(socket);
        }

    }

}
