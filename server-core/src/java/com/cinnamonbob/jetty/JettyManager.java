package com.cinnamonbob.jetty;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.event.EventListener;
import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.event.system.SystemEvent;
import com.cinnamonbob.event.system.SystemStartedEvent;
import com.cinnamonbob.util.logging.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;

import java.io.File;

/**
 * 
 *
 */
public class JettyManager implements EventListener
{
    private static final Logger LOG = Logger.getLogger(JettyManager.class);
    private static final String BEAN_NAME = "jettyManager";

    private Server server;
    private ConfigurationManager configurationManager;
    private WebApplicationContext appContext;

    public void setJettyServer(Server server)
    {
        this.server = server;
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void deployWebapp() throws Exception
    {
        File wwwRoot = configurationManager.getApplicationPaths().getContentRoot();

        appContext = server.addWebApplication("/", wwwRoot.getAbsolutePath());

        if (!server.isStarted())
        {
            server.start();
        }
    }

    public void deployInWebApplicationContext(String name, Object obj)
    {
        appContext.setAttribute(name, obj);
    }

    public static JettyManager getInstance()
    {
        return (JettyManager) ComponentContext.getBean(BEAN_NAME);
    }

    public void stop()
    {
        try
        {
            server.stop(true);
            server.destroy();
        }
        catch (InterruptedException e)
        {
            // Empty
            LOG.severe("Error while stopping Jetty", e);
        }
    }

    public void handleEvent(Event evt)
    {
        if (evt instanceof SystemStartedEvent)
        {
            try
            {
                deployWebapp();
            }
            catch (Exception e)
            {
                LOG.severe("Unable to deploy web application", e);
            }
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{SystemEvent.class};
    }
}
