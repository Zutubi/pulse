package com.zutubi.pulse.servercore.jetty;

import com.zutubi.pulse.core.Stoppable;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;
import org.mortbay.http.HttpContext;

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
    private boolean stopped = false;
    
    /**
     * Configure a server using the provided handler to configure the instance.  If the named server does
     * not already exist, a new one is created.
     *
     * @param name    a key that can be used with {@link JettyServerManager#getServer} to retrieve
     *                the server instance
     * @param handler a configuration handler responsible for configuring the server instance
     * @return the newly configured server instance
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

    public synchronized HttpContext configureContext(final String serverName, final String contextPath, ContextConfigurationHandler handler) throws IOException
    {
        Server server = ensureServerAvailable(serverName);
        if (!isContextAvailable(serverName, contextPath))
        {
            throw new IllegalArgumentException("Context '"+contextPath+"' has already been configured.");
        }

        HttpContext context = server.getContext(contextPath);
        handler.configure(context);
        return context;
    }

    public synchronized WebApplicationContext configureContext(String serverName, String contextPath, WebappConfigurationHandler handler) throws IOException
    {
        Server server = ensureServerAvailable(serverName);
        if (!isContextAvailable(serverName, contextPath))
        {
            throw new IllegalArgumentException("Context '"+contextPath+"' has already been configured.");
        }

        WebApplicationContext context = new WebApplicationContext();
        handler.configure(context);
        server.addContext(context);
        return context;
    }

    private Server ensureServerAvailable(String serverName)
    {
        Server server = getServer(serverName);
        if (server == null)
        {
            throw new IllegalArgumentException("Unknown jetty server '" + serverName + "'");
        }
        return server;
    }

    public synchronized boolean isContextAvailable(final String serverName, final String contextPath)
    {
        Server server = getServer(serverName);
        if (server == null)
        {
            throw new IllegalArgumentException("Unknown jetty server '" + serverName + "'");
        }

        HttpContext context = CollectionUtils.find(server.getContexts(), new Predicate<HttpContext>()
        {
            public boolean satisfied(HttpContext httpContext)
            {
                return httpContext.getContextPath().compareTo(contextPath) == 0;
            }
        });

        return context == null;
    }

    /**
     * Retrieve a previously created jetty server instance.
     *
     * @param name the name of the instance to be retrieved
     * @return the server instance, or null if it does not exist.
     */
    public synchronized Server getServer(String name)
    {
        return servers.get(name);
    }

    public synchronized void stop(boolean force)
    {
        if (!stopped)
        {
            stopped = true;
            for (Map.Entry<String, Server> entry : servers.entrySet())
            {
                try
                {
                    Server server = entry.getValue();
                    if (server.isStarted())
                    {
                        server.stop(!force);
                    }
                }
                catch (InterruptedException e)
                {
                    LOG.severe("Error while stopping Jetty(" + entry.getKey() + ")", e);
                }
            }
        }
    }
}
