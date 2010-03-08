package com.zutubi.pulse.master.bootstrap;

import com.opensymphony.xwork.config.providers.XmlConfigurationProvider;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.security.SecurityManager;

/**
 * The web manager is responsible for handling processes related
 * to the management and configuration of the web application(s)
 * deployed within the embedded jetty instances.
 */
public class WebManager
{
    public static final String WEBAPP_PULSE = "pulse";
    public static final String REPOSITORY_PATH = "/repository";

    private boolean mainDeployed = false;

    /**
     * Load the setup process' xwork configuration.
     */
    public void deploySetup()
    {
        loadXworkConfiguration("xwork-setup.xml");
    }

    /**
     * Deploy the main pulse applications' xwork configuration.
     */
    public void deployMain()
    {
        mainDeployed = true;
        loadXworkConfiguration("xwork.xml");

        // enable security only when the standard xwork file is loaded.
        SecurityManager securityManager = SpringComponentContext.getBean("securityManager");
        securityManager.secure();
    }

    public boolean isMainDeployed()
    {
        return mainDeployed;
    }

    private void loadXworkConfiguration(String name)
    {
        com.opensymphony.xwork.config.ConfigurationManager.clearConfigurationProviders();
        com.opensymphony.xwork.config.ConfigurationManager.addConfigurationProvider(new XmlConfigurationProvider(name));
        com.opensymphony.xwork.config.ConfigurationManager.getConfiguration().reload();
    }
}
