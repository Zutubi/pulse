package com.zutubi.pulse.master.bootstrap;

import com.opensymphony.xwork.config.providers.XmlConfigurationProvider;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.security.SecurityManager;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.jetty.JettyServerManager;
import com.zutubi.pulse.servercore.jetty.PulseWebappConfigurationHandler;
import com.zutubi.util.logging.Logger;
import org.mortbay.jetty.Server;

/**
 * The web manager is responsible for handling processes related
 * to the management and configuration of the web application(s)
 * deployed within the embedded jetty instances.
 */
public class WebManager
{
    private static final Logger LOG = Logger.getLogger(WebManager.class);

    public static final String WEBAPP_PULSE = "pulse";

    private ConfigurationManager configurationManager;

    private JettyServerManager jettyServerManager;

    /**
     * Load the setup process' xwork configuration.
     */
    public void deploySetup()
    {
        ensureJettyStarted();

        loadXworkConfiguration("xwork-setup.xml");
    }

    /**
     * Deploy the main pulse applications' xwork configuration.
     */
    public void deployMain()
    {
        ensureJettyStarted();

        loadXworkConfiguration("xwork.xml");

        // enable security only when the standard xwork file is loaded.
        SecurityManager securityManager = SpringComponentContext.getBean("securityManager");
        securityManager.secure();
    }

    private void loadXworkConfiguration(String name)
    {
        com.opensymphony.xwork.config.ConfigurationManager.clearConfigurationProviders();
        com.opensymphony.xwork.config.ConfigurationManager.addConfigurationProvider(new XmlConfigurationProvider(name));
        com.opensymphony.xwork.config.ConfigurationManager.getConfiguration().reload();
    }

    private void ensureJettyStarted()
    {
        Server server = jettyServerManager.getServer(WEBAPP_PULSE);
        if (server != null && server.isStarted())
        {
            return;
        }

        PulseWebappConfigurationHandler webapp = new PulseWebappConfigurationHandler();
        webapp.setConfigurationManager(configurationManager);
        webapp.setLogDir(configurationManager.getSystemPaths().getLogRoot());
        webapp.setTmpDir(configurationManager.getSystemPaths().getTmpRoot());
        
        try
        {
            server = jettyServerManager.createNewServer(WEBAPP_PULSE, webapp);
            server.start();
        }
        catch (Exception e)
        {
            LOG.severe("Failed to start jetty instance.", e);
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setJettyServerManager(JettyServerManager jettyServerManager)
    {
        this.jettyServerManager = jettyServerManager;
    }
}
