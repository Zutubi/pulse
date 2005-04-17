package com.cinnamonbob;

import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHttpContext;
import com.cinnamonbob.api.XmlRpcApiServlet;

/**
 *
 */
public class HttpService {

    private int port = -1;

    private Server server = null;

    public HttpService(int port) {
        this.port = port;
    }

    public void start() throws Exception {

        // Create the server
        server = new Server();

        // Create a port listener
        SocketListener listener = new SocketListener();
        listener.setPort(port);
        server.addListener(listener);
        //TODO: retrieve the bob.home directory from Bob

        // dynamically deploy a servlet..
        ServletHttpContext context = (ServletHttpContext)
        server.getContext("/");
        context.addServlet("XmlRpcApiServlet","/api/xmlrpc/*", XmlRpcApiServlet.class.getName());

        server.addWebApplication("/", System.getProperty("bob.home")+"/content");
        server.start();

    }

    public void stop() {
        if (server != null) {
            try {
                server.stop();
            } catch (InterruptedException e) {
                // nop
            }
        }
    }
}
