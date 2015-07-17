package com.zutubi.pulse.servercore.jetty;

import com.zutubi.pulse.core.Stoppable;
import com.zutubi.util.logging.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import java.io.IOException;

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
        // It might seem simpler to set the context path on this outer handler, and mostly it is.
        // However, this means things inside nested handlers won't see the full context path,
        // which can confound things (like setting up $base so the web UI can construct links).
        // So we allow the path to be set at a lower level so it is still accessible from requests.
        ContextHandler context = new ContextHandler();
        handler.configure(contextPath, context);
        contexts.addHandler(context);
        startContextIfServerStarted(contextPath, context);
        return context;
    }

    public <T extends Handler> T getContextHandler(final Class<T> type)
    {
        return contexts.getChildHandlerByClass(type);
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
