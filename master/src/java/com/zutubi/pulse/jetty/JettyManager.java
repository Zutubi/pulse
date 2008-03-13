package com.zutubi.pulse.jetty;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.SystemPaths;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.util.logging.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;
import org.mortbay.jetty.servlet.WebApplicationHandler;
import org.mortbay.util.MultiException;
import org.mortbay.http.NCSARequestLog;

import java.io.File;
import java.net.BindException;
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
    private MasterConfigurationManager configurationManager;
    private WebApplicationContext appContext;

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
    public void setConfigurationManager(MasterConfigurationManager configurationManager)
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

        SystemPaths systemPaths = configurationManager.getSystemPaths();
        File wwwRoot = systemPaths.getContentRoot();

        try
        {
            appContext = server.addWebApplication(getContextPath(), wwwRoot.getAbsolutePath());
            appContext.setDefaultsDescriptor(null);
            File tmpRoot = systemPaths.getTmpRoot();
            if(!tmpRoot.exists())
            {
                tmpRoot.mkdirs();
            }
            appContext.setAttribute("javax.servlet.context.tempdir", tmpRoot);

            File file = new File(systemPaths.getLogRoot(), "yyyy_mm_dd.request.log");

            //TODO: make this configurable.

            // configure the request logging.
            NCSARequestLog requestLog = new NCSARequestLog();
            requestLog.setAppend(false);
            requestLog.setExtended(true);
            requestLog.setIgnorePaths(new String[]{"/images/*.*", "*.css", "*.js", "*.ico", "*.gif"});
            requestLog.setRetainDays(30);
            requestLog.setFilename(file.getAbsolutePath());
            appContext.setRequestLog(requestLog);

            server.start();
        }
        catch(MultiException e)
        {
            for(Exception nested: (List<Exception>)e.getExceptions())
            {
                if (nested instanceof BindException)
                {
                    handleBindException();
                }
                else
                {
                    LOG.severe("Unable to start server: " + nested.getMessage(), nested);
                }
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

    private void handleBindException()
    {
        int port = configurationManager.getSystemConfig().getServerPort();
        LOG.severe(String.format("Pulse is unable to start on port %s because it " +
                "is being used by another process.  Please select a different port and restart pulse.", port));
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
            return ((WebApplicationHandler)server.getContext(getContextPath()).getHandlers()[0]);
        }
        return null;
    }

    protected String getContextPath()
    {
        return configurationManager.getSystemConfig().getContextPath();
    }
}