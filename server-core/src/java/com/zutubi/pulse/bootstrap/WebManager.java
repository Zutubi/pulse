package com.cinnamonbob.bootstrap;

import com.cinnamonbob.jetty.JettyManager;
import com.opensymphony.xwork.config.ConfigurationManager;
import com.opensymphony.xwork.config.providers.XmlConfigurationProvider;

/**
 * <class-comment/>
 */
public class WebManager
{
    private JettyManager jettyManager;

    public void deployStartup()
    {
        ensureJettyStarted();

        loadXworkConfiguration("xwork-startup.xml");
    }

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


    /**
     * Required resource.
     *
     * @param jettyManager
     */
    public void setJettyManager(JettyManager jettyManager)
    {
        this.jettyManager = jettyManager;
    }
}
