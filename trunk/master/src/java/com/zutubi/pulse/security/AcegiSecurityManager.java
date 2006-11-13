package com.zutubi.pulse.security;

import com.zutubi.pulse.jetty.JettyManager;
import com.zutubi.pulse.spring.web.context.FilterToBeanProxy;
import com.zutubi.pulse.util.logging.Logger;
import org.acegisecurity.providers.ProviderManager;
import org.acegisecurity.util.FilterChainProxy;
import org.mortbay.jetty.servlet.Dispatcher;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.WebApplicationHandler;

/**
 * The security manager is responsible for handling security related functionality.
 *
 */
public class AcegiSecurityManager implements SecurityManager
{
    private static final String ACEGI_FILTER_NAME = "Acegi Filter Chain Proxy";

    private static final Logger LOG = Logger.getLogger(AcegiSecurityManager.class);

    private ProviderManager authenticationManager;
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

    public void setAuthenticationManager(ProviderManager authenticationManager)
    {
        this.authenticationManager = authenticationManager;
    }
}
