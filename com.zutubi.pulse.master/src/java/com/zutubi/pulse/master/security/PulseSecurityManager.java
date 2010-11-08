package com.zutubi.pulse.master.security;

import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.master.spring.web.context.SpringSecurityFilter;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.jetty.JettyServerManager;
import com.zutubi.util.logging.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.WebApplicationHandler;

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
        WebApplicationHandler handler = getHandler();
        if (handler == null)
        {
            throw new RuntimeException("Can not enable web security before the web app is fully deployed.");
        }
        enableWebUISecurity(handler);
    }

    private void enableWebUISecurity(WebApplicationHandler handler)
    {
        FilterHolder holder = handler.getFilter(SECURITY_FILTER_NAME);
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
