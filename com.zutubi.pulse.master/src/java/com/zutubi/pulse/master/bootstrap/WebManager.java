package com.zutubi.pulse.master.bootstrap;

import com.opensymphony.xwork.config.ConfigurationManager;
import com.opensymphony.xwork.config.providers.XmlConfigurationProvider;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.security.SecurityManager;

/**
 */
public class WebManager
{
    public void deploySetup()
    {
        loadXworkConfiguration("xwork-setup.xml");
    }

    public void deployMain()
    {
        loadXworkConfiguration("xwork.xml");

        // enable security only when the standard xwork file is loaded.
        SecurityManager securityManager = SpringComponentContext.getBean("securityManager");
        securityManager.secure();
    }

    private void loadXworkConfiguration(String name)
    {
        ConfigurationManager.clearConfigurationProviders();
        ConfigurationManager.addConfigurationProvider(new XmlConfigurationProvider(name));
        ConfigurationManager.getConfiguration().reload();
    }
}
