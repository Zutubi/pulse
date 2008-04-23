package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.jetty.JettyManager;
import com.opensymphony.xwork.config.ConfigurationManager;
import com.opensymphony.xwork.config.providers.XmlConfigurationProvider;
import com.zutubi.pulse.security.SecurityManager;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.system.SystemStartedEvent;

/**
 */
public class WebManager
{
    private JettyManager jettyManager;

    public void deployShutdown()
    {
        ensureJettyStarted();

        loadXworkConfiguration("xwork-shutdown.xml");
    }

    public void deploySetup()
    {
        ensureJettyStarted();

        loadXworkConfiguration("xwork-setup.xml");
    }

    public void deployMain()
    {
        ensureJettyStarted();

        loadXworkConfiguration("xwork.xml");

        // enable security only when the standard xwork file is loaded.
        SecurityManager securityManager = ComponentContext.getBean("securityManager");
        securityManager.secure();
    }

    private void loadXworkConfiguration(String name)
    {
        ConfigurationManager.clearConfigurationProviders();
        ConfigurationManager.addConfigurationProvider(new XmlConfigurationProvider(name));
        ConfigurationManager.getConfiguration().reload();
    }

    private void ensureJettyStarted()
    {
        if (!jettyManager.isStarted())
        {
            jettyManager.start();
        }
    }

    public void setJettyManager(JettyManager jettyManager)
    {
        this.jettyManager = jettyManager;
    }
}
