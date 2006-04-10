package com.cinnamonbob.jetty;

import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.Stoppable;
import com.cinnamonbob.util.logging.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;
import org.mortbay.jetty.servlet.WebApplicationHandler;
import org.mortbay.util.MultiException;

import java.io.File;
import java.util.List;

/**
 * The Jetty Manager provides access to the runtime configuration of the jetty server, and hence
 * the Web Application Container and its configuration.
 *
 */
public class JettyManager implements Stoppable
{
    private static final Logger LOG = Logger.getLogger(JettyManager.class);

    private Server server;
    private ConfigurationManager configurationManager;
    private WebApplicationContext appContext;

    /**
     * The context path under which the applications Web UI is deployed.
     */
    private String contextPath = "/";

    /**
     * Required resource.
     *
     * @param server
     */
    public void setJettyServer(Server server)
    {
        this.server = server;
    }

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    /**
     * Start the embedded jetty server (to handle Http requests) and deploy the
     * default web application.
     *
     */
    public void start()
    {
        if (isStarted())
        {
            // server is already running, no need to start it a second time.
            LOG.warning("Request to start jetty server ignored. Server already started.");
            return;
        }

        File wwwRoot = configurationManager.getSystemPaths().getContentRoot();

        try
        {
            appContext = server.addWebApplication(contextPath, wwwRoot.getAbsolutePath());
            server.start();
        }
        catch(MultiException e)
        {
            for(Exception nested: (List<Exception>)e.getExceptions())
            {
                LOG.severe("Unable to start server: " + nested.getMessage(), nested);
            }

            // This is fatal.
            System.exit(1);
        }
        catch (Exception e)
        {
            LOG.severe("Unable to start server: " + e.getMessage(), e);

            // This is fatal.
            System.exit(1);
        }
    }

    /**
     * Indicates whether or not the jetty server is running.
     *
     * @return true if the jetty server has already been started, false otherwise.
     */
    public boolean isStarted()
    {
        return server.isStarted();
    }

    /**
     * Get the web application context. This context is used for 'things' that are
     * deployed application wide. Other things to checkout include the session context,
     * request context and page context.
     *
     * @return application context
     */
    public WebApplicationContext getApplicationContext()
    {
        return appContext;
    }

    /**
     * Stop the jetty server.
     * @param force
     */
    public void stop(boolean force)
    {
        try
        {
            if (server.isStarted())
            {
                server.stop(true);
            }
        }
        catch (InterruptedException e)
        {
            LOG.severe("Error while stopping Jetty", e);
        }
    }

    /**
     * Get the WebApplicationHandler for the deployed web application. It is through this handler
     * that new servlets and filters can be deployed into the running Web Application.
     *
     * @return handler for the deployed web application.
     */
    public WebApplicationHandler getHandler()
    {
        if (server.isStarted())
        {
            return ((WebApplicationHandler)server.getContext(contextPath).getHandlers()[0]);
        }
        return null;
    }
}