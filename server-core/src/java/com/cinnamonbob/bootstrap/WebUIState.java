package com.cinnamonbob.bootstrap;

import com.cinnamonbob.jetty.JettyManager;
import com.opensymphony.xwork.config.ConfigurationManager;
import com.opensymphony.xwork.config.providers.XmlConfigurationProvider;

/**
 * The webwork config is a helper object used to manage the systems xwork configurations
 *
 */
public class WebUIState
{
    public static void startStarting()
    {
        load("xwork-startup.xml");
    }

    public static void startSetup()
    {
        load("xwork-setup.xml");
    }

    public static void startMain()
    {
        load("xwork.xml");
    }

    private static void load(String name)
    {
        ConfigurationManager.clearConfigurationProviders();
        ConfigurationManager.addConfigurationProvider(new XmlConfigurationProvider(name));
        ConfigurationManager.getConfiguration().reload();

        JettyManager jetty = (JettyManager) ComponentContext.getBean("jettyManager");
        if (!jetty.isStarted())
        {
            try
            {
                jetty.start();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
