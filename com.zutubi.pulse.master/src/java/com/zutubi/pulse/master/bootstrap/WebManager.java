package com.zutubi.pulse.master.bootstrap;

import com.opensymphony.xwork.config.providers.XmlConfigurationProvider;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.security.SecurityManager;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.pulse.servercore.jetty.JettyServerManager;
import com.zutubi.pulse.servercore.jetty.PulseWebappConfigurationHandler;
import com.zutubi.util.logging.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.util.MultiException;

import java.util.List;
import java.net.BindException;

/**
 * The web manager is responsible for handling processes related
 * to the management and configuration of the web application(s)
 * deployed within the embedded jetty instances.
 */
public class WebManager
{
    private static final Logger LOG = Logger.getLogger(WebManager.class);

    public static final String WEBAPP_PULSE = "pulse";
    public static final String REPOSITORY_PATH = "/repository";
    
    private ConfigurationManager configurationManager;

    private JettyServerManager jettyServerManager;

    private boolean mainDeployed = false;

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
        
        SystemConfiguration config = configurationManager.getSystemConfig();

        try
        {
            server = jettyServerManager.configureServer(WEBAPP_PULSE, webapp);
            jettyServerManager.configureContext(WEBAPP_PULSE, config.getContextPath(), webapp);
            
            server.start();
        }
        catch(MultiException e)
        {
            for(Exception nested: (List<Exception>)e.getExceptions())
            {
                if (nested instanceof BindException)
                {
                    handleBindException(config);
                }
                else
                {
                    LOG.severe("Unable to start server: " + nested.getMessage(), nested);
                }
            }

            // This is fatal.
            System.exit(1);
        }
        catch (Exception e)
        {
            LOG.severe("Unable to start server: " + e.getMessage(), e);

            // This is fatal.
            System.exit(1);
        }
    }

    private void handleBindException(SystemConfiguration config)
    {
        LOG.severe(String.format("Unable to start on port %s because it " +
                "is being used by another process.  Please select a different port and restart pulse.", config.getServerPort()));
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
