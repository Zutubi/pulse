package com.cinnamonbob;

import com.cinnamonbob.util.IOHelper;
import org.apache.xmlrpc.XmlRpcClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is a helper that supports communication between a remote client
 * and the bob server. It abstracts the actual communication details when
 * sending requests to bob.
 *
 * @author Daniel Ostermeier
 */
public class BobServerProxy
{
    private static final Logger LOG = Logger.getLogger(BobServerProxy.class.getName());

    private int port;
    private String host;

    public BobServerProxy(String host, int port)
    {
        this.host = host;
        this.port = port;
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
