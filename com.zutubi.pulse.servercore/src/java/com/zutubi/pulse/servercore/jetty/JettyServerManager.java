package com.zutubi.pulse.servercore.jetty;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.util.logging.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import java.io.IOException;

import static java.util.Arrays.asList;

/**
 * The jetty server manager is responsible for managing the lifecycle of Jetty.
 */
public class JettyServerManager implements Stoppable
{
    private static final Logger LOG = Logger.getLogger(JettyServerManager.class);

    private Server server;
    private ContextHandlerCollection contexts = new ContextHandlerCollection();
    private boolean stopped = false;
    
    public synchronized Server configureServer(ServerConfigurationHandler handler) throws IOException
    {
        server = new Server();
        handler.configure(server);
        server.setHandler(contexts);
        return server;
    }

    public synchronized ContextHandler configureContext(final String contextPath, ContextConfigurationHandler handler) throws IOException
    {
        if (!isContextAvailable(contextPath))
        {
            throw new IllegalArgumentException("Context '" + contextPath + "' has already been configured.");
        }

        ContextHandler context = new ContextHandler(contextPath);
        handler.configure(context);
        contexts.addHandler(context);
        startContextIfServerStarted(contextPath, context);
        return context;
    }

    public ContextHandler getContextHandler(final String contextPath)
    {
        Handler[] handlers = contexts.getHandlers();
        if (handlers == null)
        {
            return null;
        }

        return (ContextHandler) Iterables.tryFind(asList(handlers), new Predicate<Handler>()
        {
            public boolean apply(Handler handler)
            {
                return ((ContextHandler) handler).getContextPath().equals(contextPath);
            }
        }).orNull();
    }

    public synchronized boolean isContextAvailable(final String contextPath)
    {
        return getContextHandler(contextPath) == null;
    }

    private void startContextIfServerStarted(String contextPath, ContextHandler context) throws IOException
    {
        if (server.isStarted())
        {
            try
            {
                context.start();
            }
            catch (Exception e)
            {
                throw new IOException("Failed to start context '" + contextPath + "': " + e.getMessage(), e);
            }
        }
    }

    public synchronized Server getServer()
    {
        return server;
    }

    public synchronized void stop(boolean force)
    {
        if (!stopped)
        {
            stopped = true;
            try
            {
                if (server.isStarted())
                {
                    server.stop();
                }
            }
            catch (Exception e)
            {
                LOG.severe("Error while stopping Jetty", e);
            }
        }
    }
}
