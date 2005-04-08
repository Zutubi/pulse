package com.cinnamonbob;

import com.cinnamonbob.util.IOHelper;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author Daniel Ostermeier
 */
public class BobServerProxy
{

    private static final Logger LOG = Logger.getLogger(BobServerProxy.class.getName());

    private int port = 2345;
    private String host = "localhost";

    public BobServerProxy()
    {

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
            out.write(AdminService.Command.SHUTDOWN.getBytes());
            out.close();

        } catch (IOException e) {
            LOG.log(Level.WARNING, "Unable to send shutdown request", e);

        } finally {
            IOHelper.close(socket);
        }

    }

    public void build(String projectName)
    {
        // connect to the admin port and send the shutdown command...
        Socket socket = null;
        try {
            socket = new Socket(host, port);

            // send the shutdown command.
            OutputStream out = socket.getOutputStream();
            out.write(AdminService.Command.BUILD.getBytes());
            out.close();

        } catch (IOException e) {
            LOG.log(Level.WARNING, "Unable to send build request", e);

        } finally {
            IOHelper.close(socket);
        }

    }

}
