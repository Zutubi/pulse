package com.zutubi.pulse.acceptance.support;

import com.zutubi.pulse.core.test.TestUtils;
import org.mortbay.http.HttpContext;
import org.mortbay.http.handler.ProxyHandler;
import org.mortbay.jetty.Server;
import org.mortbay.util.InetAddrPort;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple wrapper around Jetty configuring it to run as a proxy.
 */
public class ProxyServer
{
    private int port;
    private Server server;
    private ExecutorService executor;

    /**
     * Creates a new proxy to run on the specified port.
     *
     * @param port port the proxy should listen on
     */
    public ProxyServer(int port)
    {
        this.port = port;
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Starts this proxy, waiting for it to begin listening before returning.
     * The proxy runs in a separate thread until stopped using {@link #stop}.
     *
     * @throws java.io.IOException  if an error occurs on jetty configuration
     * @throws InterruptedException if interrupted waiting for the proxy to
     *         start
     */
    public synchronized void start() throws IOException, InterruptedException
    {
        HttpContext context = new HttpContext();
        context.setContextPath("/");
        context.addHandler(new ProxyHandler());

        server = new Server();
        server.addListener(new InetAddrPort(port));
        server.addContext(context);

        executor.submit(new Runnable()
        {
            public void run()
            {
                try
                {
                    server.start();
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        TestUtils.waitForServer(port);
    }

    /**
     * Stops the proxy.  It cannot be restarted.
     *
     * @throws InterruptedException if interrupted while stopping
     */
    public synchronized void stop() throws InterruptedException
    {
        server.stop(true);
        executor.shutdown();
    }
}
