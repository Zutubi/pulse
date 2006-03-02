package com.cinnamonbob.jetty;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.Stoppable;
import com.cinnamonbob.util.logging.Logger;
import org.acegisecurity.util.FilterToBeanProxy;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Dispatcher;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.WebApplicationContext;
import org.mortbay.jetty.servlet.WebApplicationHandler;

import java.io.File;

/**
 * The Jetty Manager provides access to the runtime configuration of the jetty server, and hence
 * the Web Application Container and its configuration.
 *
 */
public class JettyManager implements Stoppable
{
    private static final Logger LOG = Logger.getLogger(JettyManager.class);

    /**
     * The name of the JettyManager bean deployed within Spring.
     */
    private static final String BEAN_NAME = "jettyManager";

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
     * @throws Exception
     */
    public void start() throws Exception
    {
        File wwwRoot = configurationManager.getSystemPaths().getContentRoot();

        appContext = server.addWebApplication(contextPath, wwwRoot.getAbsolutePath());

        if (!server.isStarted())
        {
            server.start();
        }
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

    public void stop(boolean force)
    {
        try
        {
            server.stop(true);
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