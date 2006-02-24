package com.cinnamonbob.jetty;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.events.Event;
import com.cinnamonbob.events.EventListener;
import com.cinnamonbob.events.EventManager;
import com.cinnamonbob.core.Stoppable;
import com.cinnamonbob.events.system.SystemEvent;
import com.cinnamonbob.events.system.SystemStartedEvent;
import com.cinnamonbob.util.logging.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;
import org.mortbay.jetty.servlet.WebApplicationHandler;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.Dispatcher;
import org.mortbay.http.HttpContext;
import org.acegisecurity.util.FilterToBeanProxy;

import java.io.File;

/**
 * 
 *
 */
public class JettyManager implements Stoppable
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

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void deployWebapp() throws Exception
    {
        File wwwRoot = configurationManager.getSystemPaths().getContentRoot();

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

    public void stop(boolean force)
    {
        try
        {
            server.stop(true);
            //server.destroy();
        }
        catch (InterruptedException e)
        {
            LOG.severe("Error while stopping Jetty", e);
        }
    }

    public void addFilter()
    {
        WebApplicationHandler handler = ((WebApplicationHandler)server.getContext("/").getHandlers()[0]);

        FilterHolder filter = new FilterHolder(handler, "Acegi Filter Chain Proxy", "com.cinnamonbob.spring.web.context.FilterToBeanProxy");
        filter.setInitParameter("targetClass", "org.acegisecurity.util.FilterChainProxy");
        handler.addFilterHolder(filter);
        handler.addFilterPathMapping("/*", "Acegi Filter Chain Proxy", Dispatcher.__REQUEST | Dispatcher.__FORWARD);
        try
        {
            filter.start();
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }
        assert filter.getFilter() != null;
    }
}
/*
    <filter>
        <filter-name>Acegi Filter Chain Proxy</filter-name>
        <filter-class>org.acegisecurity.util.FilterToBeanProxy</filter-class>
        <init-param>
            <param-name>targetClass</param-name>
            <param-value>org.acegisecurity.util.FilterChainProxy</param-value>
        </init-param>
    </filter>

    <filter-mapping>
        <filter-name>Acegi Filter Chain Proxy</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
*/
