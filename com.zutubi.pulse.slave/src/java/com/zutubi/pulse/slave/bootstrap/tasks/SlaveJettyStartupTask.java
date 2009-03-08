package com.zutubi.pulse.slave.bootstrap.tasks;

import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.pulse.servercore.jetty.JettyServerManager;
import com.zutubi.pulse.servercore.jetty.PulseWebappConfigurationHandler;
import com.zutubi.util.logging.Logger;
import org.mortbay.jetty.Server;

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

        Server server = jettyServerManager.configureServer(WEBAPP_PULSE, webapp);
        jettyServerManager.configureContext(WEBAPP_PULSE, config.getContextPath(), webapp);

        server.start();
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
