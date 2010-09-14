package com.zutubi.pulse.master.security;

import com.zutubi.pulse.master.spring.web.context.FilterToBeanProxy;
import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.jetty.JettyServerManager;
import com.zutubi.util.logging.Logger;
import org.springframework.security.util.FilterChainProxy;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Dispatcher;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.WebApplicationHandler;

/**
 * The security manager is responsible for handling security related functionality.
 */
public class AcegiSecurityManager implements SecurityManager
{
    private static final String ACEGI_FILTER_NAME = "Acegi Filter Chain Proxy";

    private static final Logger LOG = Logger.getLogger(AcegiSecurityManager.class);

    private JettyServerManager jettyServerManager;

    private ConfigurationManager configurationManager;

    /**
     * Enable security in the Pulse web application.
     */
    public void secure()
    {
        WebApplicationHandler handler = getHandler();
        if (handler == null)
        {
            throw new RuntimeException("Can not enable web security before the web app is fully deployed.");
        }
        deploySecurityFilter(handler);
        enableWebUISecurity(handler);
    }

    private void enableWebUISecurity(WebApplicationHandler handler)
    {
        FilterHolder filter = handler.getFilter(ACEGI_FILTER_NAME);
        try
        {
            filter.start();
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }
    }

    private void deploySecurityFilter(WebApplicationHandler handler)
    {
        FilterHolder filter = new FilterHolder(handler, ACEGI_FILTER_NAME, FilterToBeanProxy.class.getName());
        filter.setInitParameter("targetClass", FilterChainProxy.class.getName());
        handler.addFilterHolder(filter);
        handler.addFilterPathMapping("/*", ACEGI_FILTER_NAME, Dispatcher.__REQUEST | Dispatcher.__FORWARD);
    }

    private WebApplicationHandler getHandler()
    {
        Server server = jettyServerManager.getServer(WebManager.WEBAPP_PULSE);
        if (server.isStarted())
        {
            String contextPath = configurationManager.getSystemConfig().getContextPath();
            return ((WebApplicationHandler) server.getContext(contextPath).getHandlers()[0]);
        }
        return null;
    }

    public void setJettyServerManager(JettyServerManager jettyServerManager)
    {
        this.jettyServerManager = jettyServerManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
