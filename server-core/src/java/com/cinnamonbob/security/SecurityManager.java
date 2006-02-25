package com.cinnamonbob.security;

import com.cinnamonbob.jetty.JettyManager;
import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.spring.web.context.FilterToBeanProxy;
import org.mortbay.jetty.servlet.WebApplicationHandler;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.Dispatcher;
import org.acegisecurity.util.FilterChainProxy;

/**
 * The security manager is responsible for handling security related functionality.
 *
 */
public class SecurityManager
{
    private static final String ACEGI_FILTER_NAME = "Acegi Filter Chain Proxy";

    private static final Logger LOG = Logger.getLogger(SecurityManager.class);

    private JettyManager jettyManager;

    public void init()
    {
        deploySecurityFilter();
        enableWebUISecurity();
    }

    private void disableWebUISecurity()
    {
        FilterHolder filter = jettyManager.getHandler().getFilter(ACEGI_FILTER_NAME);
        if (filter.isStarted())
        {
            filter.stop();
        }
    }

    private void enableWebUISecurity()
    {
        FilterHolder filter = jettyManager.getHandler().getFilter(ACEGI_FILTER_NAME);
        try
        {
            filter.start();
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }
    }

    private void deploySecurityFilter()
    {
        WebApplicationHandler handler = jettyManager.getHandler();

        FilterHolder filter = new FilterHolder(handler, ACEGI_FILTER_NAME, FilterToBeanProxy.class.getName());
        filter.setInitParameter("targetClass", FilterChainProxy.class.getName());
        handler.addFilterHolder(filter);
        handler.addFilterPathMapping("/*", ACEGI_FILTER_NAME, Dispatcher.__REQUEST | Dispatcher.__FORWARD);
    }

    public void setJettyManager(JettyManager jettyManager)
    {
        this.jettyManager = jettyManager;
    }
}
