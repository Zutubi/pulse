package com.zutubi.pulse.slave.bootstrap.tasks;

import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.pulse.servercore.jetty.JettyServerManager;
import com.zutubi.pulse.servercore.jetty.PulseWebappConfigurationHandler;
import com.zutubi.util.logging.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.MultiException;

import java.net.BindException;

/**
 * Start the slave web application.
 */
public class SlaveJettyStartupTask implements StartupTask
{
    private static final Logger LOG = Logger.getLogger(SlaveJettyStartupTask.class);

    private JettyServerManager jettyServerManager;
    private ConfigurationManager configurationManager;

    public void execute() throws Exception
    {
        PulseWebappConfigurationHandler webapp = new PulseWebappConfigurationHandler();
        webapp.setConfigurationManager(configurationManager);
        webapp.setLogDir(configurationManager.getSystemPaths().getLogRoot());
        webapp.setTmpDir(configurationManager.getSystemPaths().getTmpRoot());

        SystemConfiguration config = configurationManager.getSystemConfig();

        try
        {
            Server server = jettyServerManager.configureServer(webapp);
            jettyServerManager.configureContext(config.getContextPath(), webapp);

            server.start();
        }
        catch(MultiException e)
        {
            handleMultiException(config, e);

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

    private void handleMultiException(SystemConfiguration config, MultiException e)
    {
        for (Throwable nested: e.getThrowables())
        {
            if (nested instanceof BindException)
            {
                LOG.severe(String.format("Unable to start on port %s because it " +
                        "is being used by another process.  Please select a different port and restart pulse.", config.getServerPort()));
            }
            else
            {
                LOG.severe("Unable to start server: " + nested.getMessage(), nested);
            }
        }
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void setJettyServerManager(JettyServerManager jettyServerManager)
    {
        this.jettyServerManager = jettyServerManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
