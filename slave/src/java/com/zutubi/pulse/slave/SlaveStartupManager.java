package com.zutubi.pulse.slave;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.Startup;
import com.zutubi.pulse.bootstrap.StartupException;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.bootstrap.conf.ConfigSupport;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.util.logging.Logger;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class SlaveStartupManager implements Startup, Stoppable
{
    private static final Logger LOG = Logger.getLogger(SlaveStartupManager.class);

    private List<String> systemContexts;
    private List<String> startupRunnables = new LinkedList<String>();
    private ObjectFactory objectFactory;
    private Server jettyServer;
    private SlaveConfigurationManager configurationManager;
    private long startTime;

    public void init() throws StartupException
    {
        ComponentContext.addClassPathContextDefinitions(systemContexts.toArray(new String[systemContexts.size()]));

        // record the startup config to the config directory.
        SystemConfiguration config = configurationManager.getSystemConfig();
        File configRoot = configurationManager.getSystemPaths().getConfigRoot();
        File startupConfigFile = new File(configRoot, "runtime.properties");
        ConfigSupport startupConfig = new ConfigSupport(new com.zutubi.pulse.bootstrap.conf.FileConfig(startupConfigFile));
        startupConfig.setProperty(SystemConfiguration.CONTEXT_PATH, config.getContextPath());
        startupConfig.setInteger(SystemConfiguration.WEBAPP_PORT, config.getServerPort());

        runStartupRunnables();
        jettyServer = new Server();
        int port = configurationManager.getSystemConfig().getServerPort();

        try
        {
            SocketListener listener = new SocketListener();
            listener.setHost(config.getBindAddress());
            listener.setPort(port);
            jettyServer.addListener(listener);
            WebApplicationContext context = jettyServer.addWebApplication("/", configurationManager.getSystemPaths().getContentRoot().getAbsolutePath());
            context.setDefaultsDescriptor(null);
            jettyServer.start();
            startTime = System.currentTimeMillis();
            System.out.println("The agent is now listening on port: " + port);
        }
        catch (Exception e)
        {
            throw new StartupException(e);
        }
    }

    public long getUptime()
    {
        return System.currentTimeMillis() - startTime;
    }

    private void runStartupRunnables()
    {
        for (String name : startupRunnables)
        {
            try
            {
                Runnable instance = objectFactory.buildBean(name);
                instance.run();
            }
            catch (Exception e)
            {
                LOG.warning("Failed to run startup task " + name + ". Reason: " + e.getMessage(), e);
            }
        }
    }

    public void setSystemContexts(List<String> systemContexts)
    {
        this.systemContexts = systemContexts;
    }

    public void setStartupRunnables(List<String> startupRunnables)
    {
        this.startupRunnables = startupRunnables;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setConfigurationManager(SlaveConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void stop(boolean force)
    {
        try
        {
            jettyServer.stop(true);
        }
        catch (InterruptedException e)
        {
            // Ignore.
        }
    }
}
