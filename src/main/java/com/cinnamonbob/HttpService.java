package com.cinnamonbob;

import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;

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
        server.addWebApplication("/", System.getProperty("bob.home")+"/lib/bob.war");
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
