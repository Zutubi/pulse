/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.acceptance.support;

import com.zutubi.pulse.core.test.TestUtils;
import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

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
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        // Setup proxy handler to handle CONNECT methods
        ConnectHandler proxy = new ConnectHandler();
        server.setHandler(proxy);

        // Setup proxy servlet
        ServletContextHandler context = new ServletContextHandler(proxy, "/", ServletContextHandler.SESSIONS);
        ServletHolder proxyServlet = new ServletHolder(ProxyServlet.class);
        context.addServlet(proxyServlet, "/*");

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
    public synchronized void stop() throws Exception
    {
        server.stop();
        executor.shutdown();
    }
}
