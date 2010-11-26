package com.zutubi.pulse.slave.bootstrap.tasks;

import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.pulse.servercore.jetty.JettyServerManager;
import com.zutubi.pulse.servercore.jetty.PulseWebappConfigurationHandler;
import com.zutubi.util.logging.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.util.MultiException;

import java.net.BindException;
import java.util.List;

/**
 * Start the slave web application.
 */
public class SlaveJettyStartupTask implements StartupTask
{
    private static final Logger LOG = Logger.getLogger(SlaveJettyStartupTask.class);
    private static final String WEBAPP_PULSE = "pulse";

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
            Server server = jettyServerManager.configureServer(WEBAPP_PULSE, webapp);
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
