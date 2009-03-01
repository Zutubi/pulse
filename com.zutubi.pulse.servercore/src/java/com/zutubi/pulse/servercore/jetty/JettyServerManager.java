package com.zutubi.pulse.servercore.jetty;

import com.zutubi.pulse.core.Stoppable;
import com.zutubi.util.logging.Logger;
import org.mortbay.jetty.Server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The jetty server manager is responsible for managing the lifecycles of
 * jetty server instances.
 */
public class JettyServerManager implements Stoppable
{
    private static final Logger LOG = Logger.getLogger(JettyServerManager.class);

    /**
     * Cache of all the created servers keyed by their names.
     */
    private final Map<String, Server> servers = new HashMap<String, Server>();

    /**
     * Configure a server using the provided handler to configure the instance.  If the named server does
     * not already exist, a new one is created.
     *
     * @param name      a key that can be used with {@link JettyServerManager#getServer} to retrieve
     * the server instance
     * @param handler   a configuration handler responsible for configuring the server instance
     * @return the newly configured server instance
     *
     * @throws IOException on configuration error.
     */
    public synchronized Server configureServer(String name, ServerConfigurationHandler handler) throws IOException
    {
        if (!servers.containsKey(name))
        {
            Server server = new Server();
            servers.put(name, server);
        }
        Server server = servers.get(name);
        handler.configure(server);
        return server;
    }

    /**
     * Retrieve a previously created jetty server instance.
     * @param name the name of the instance to be retrieved
     *
     * @return the server instance, or null if it does not exist.
     */
    public synchronized Server getServer(String name)
    {
        return servers.get(name);
    }

    public void stop(boolean force)
    {
        for (Map.Entry<String, Server> entry : servers.entrySet())
        {
            try
            {
                Server server = entry.getValue();
                if (server.isStarted())
                {
                    server.stop(true);
                }
            }
            catch (InterruptedException e)
            {
                LOG.severe("Error while stopping Jetty("+entry.getKey()+")", e);
            }
        }
    }
}
