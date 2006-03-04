package com.cinnamonbob.bootstrap;

import com.cinnamonbob.jetty.JettyManager;
import com.cinnamonbob.util.logging.Logger;
import com.opensymphony.xwork.config.ConfigurationManager;
import com.opensymphony.xwork.config.providers.XmlConfigurationProvider;


/**
 * <class-comment/>
 */
public class DefaultSetupManager implements SetupManager
{
    private static final Logger LOG = Logger.getLogger(DefaultSetupManager.class);

    private com.cinnamonbob.bootstrap.ConfigurationManager configurationManager;
    private JettyManager jettyManager;

    public boolean setup()
    {
        try
        {
            jettyManager.start();
        }
        catch (Exception e)
        {
            LOG.severe(e);
            throw new StartupException("Unable to start Jetty server: " + e.getMessage(), e);
        }

        if (configurationManager.getUserPaths() == null)
        {
            // need to show setup workflow.
            doSetup();
            return false;
        }
        return true;
    }

    private void doSetup()
    {
        // specify setup-xwork.xml.
        try
        {
            ConfigurationManager.clearConfigurationProviders();
            ConfigurationManager.addConfigurationProvider(new XmlConfigurationProvider("setup-xwork.xml"));
            ConfigurationManager.getConfiguration().reload();
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(com.cinnamonbob.bootstrap.ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setJettyManager(JettyManager jettyManager)
    {
        this.jettyManager = jettyManager;
    }
}
