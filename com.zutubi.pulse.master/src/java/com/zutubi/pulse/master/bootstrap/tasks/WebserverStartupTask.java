package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.bootstrap.WebManager;
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

public class WebserverStartupTask implements StartupTask
{
    private static final Logger LOG = Logger.getLogger(WebserverStartupTask.class);

    private ConfigurationManager configurationManager;
     
    public void execute()
    {
        SpringComponentContext.addClassPathContextDefinitions("com/zutubi/pulse/master/bootstrap/context/webserverContext.xml");

        JettyServerManager jettyServerManager = SpringComponentContext.getBean("jettyServerManager");
        Server server = jettyServerManager.getServer(WebManager.WEBAPP_PULSE);
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
            server = jettyServerManager.configureServer(WebManager.WEBAPP_PULSE, webapp);
            jettyServerManager.configureContext(WebManager.WEBAPP_PULSE, config.getContextPath(), webapp);
            
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

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}