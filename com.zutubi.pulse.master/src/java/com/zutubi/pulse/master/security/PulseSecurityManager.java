package com.zutubi.pulse.master.security;

import com.zutubi.pulse.master.spring.web.context.SpringSecurityFilter;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.jetty.JettyServerManager;
import com.zutubi.util.logging.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * The Pulse Security Manager is responsible for enabling and
 * disabling web based security.
 */
public class PulseSecurityManager implements SecurityManager
{
    private static final String SECURITY_FILTER_NAME = "security";

    private static final Logger LOG = Logger.getLogger(PulseSecurityManager.class);

    private JettyServerManager jettyServerManager;

    private ConfigurationManager configurationManager;

    /**
     * Enable security in the Pulse web application.
     */
    public void secure()
    {
        WebAppContext handler = getHandler();
        if (handler == null)
        {
            throw new RuntimeException("Can not enable web security before the web app is fully deployed.");
        }
        enableWebUISecurity(handler);
    }

    private void enableWebUISecurity(WebAppContext handler)
    {
        FilterHolder holder = handler.getServletHandler().getFilter(SECURITY_FILTER_NAME);
        try
        {
            SpringSecurityFilter filter = (SpringSecurityFilter) holder.getFilter();
            filter.enableSecurity();
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }
    }

    private WebAppContext getHandler()
    {
        Server server = jettyServerManager.getServer();
        if (server.isStarted())
        {
            String contextPath = configurationManager.getSystemConfig().getContextPath();
            ContextHandler contextHandler = jettyServerManager.getContextHandler(contextPath);
            return contextHandler.getChildHandlerByClass(WebAppContext.class);
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
