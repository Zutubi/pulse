package com.cinnamonbob;

import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHttpContext;
import com.cinnamonbob.api.XmlRpcApiServlet;
import com.cinnamonbob.setup.StartupManager;

import java.io.File;

/**
 *
 */
public class HttpService
{

    private int port = -1;

    private Server server = null;

    public HttpService(int port)
    {
        this.port = port;
    }

    public void start() throws Exception
    {

        // Create the server
        server = new Server();

        // Create a port listener
        SocketListener listener = new SocketListener();
        listener.setPort(port);
        server.addListener(listener);

        // dynamically deploy a servlet.., TODO: support enable/disable of the remote APIs.
        ServletHttpContext context = (ServletHttpContext) server.getContext("/");
        context.addServlet("XmlRpcApiServlet", "/api/xmlrpc/*", XmlRpcApiServlet.class.getName());

        String wwwRoot = StartupManager.getInstance().getContentRoot();

        server.addWebApplication("/", wwwRoot);
        server.start();
    }


    public void stop()
    {
        if (server != null)
        {
            try
            {
                server.stop();
            } catch (InterruptedException e)
            {
                // nop
            }
        }
    }
}
