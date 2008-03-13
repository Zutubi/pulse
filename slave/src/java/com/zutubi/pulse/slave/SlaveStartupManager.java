package com.zutubi.pulse.slave;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.Startup;
import com.zutubi.pulse.bootstrap.StartupException;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.config.ConfigSupport;
import com.zutubi.pulse.config.FileConfig;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

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
        LOG.debug("Initialising startup manager.");

        ComponentContext.addClassPathContextDefinitions(systemContexts.toArray(new String[systemContexts.size()]));

        SystemConfiguration config = configurationManager.getSystemConfig();
        File configRoot = configurationManager.getSystemPaths().getConfigRoot();
        File startupConfigFile = new File(configRoot, "runtime.properties");
        ConfigSupport startupConfig = new ConfigSupport(new FileConfig(startupConfigFile));
        startupConfig.setProperty(SystemConfiguration.CONTEXT_PATH, config.getContextPath());
        startupConfig.setInteger(SystemConfiguration.WEBAPP_PORT, config.getServerPort());

        loadSystemProperties();
        runStartupRunnables();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            public void uncaughtException(Thread t, Throwable e)
            {
                java.util.logging.Logger.getLogger("").log(Level.SEVERE, "Uncaught exception: " + e.getMessage(), e);
            }
        });

        jettyServer = new Server();
        int port = config.getServerPort();

        try
        {
            SocketListener listener = new SocketListener();
            listener.setHost(config.getBindAddress());
            listener.setPort(port);
            jettyServer.addListener(listener);
            WebApplicationContext context = jettyServer.addWebApplication(config.getContextPath(), configurationManager.getSystemPaths().getContentRoot().getAbsolutePath());
            context.setDefaultsDescriptor(null);
            jettyServer.start();
            startTime = System.currentTimeMillis();

            LOG.info("Agent startup complete.");

            String date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(new Date());
            System.err.format("[%s] Pulse agent %s is now listening on port %d\n", date, Version.getVersion().getVersionNumber(), port);
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }
    }

    public long getUptime()
    {
        return System.currentTimeMillis() - startTime;
    }

    private void loadSystemProperties()
    {
        File propFile = new File(configurationManager.getUserPaths().getUserConfigRoot(), "system.properties");
        if(propFile.exists())
        {
            FileInputStream is = null;
            try
            {
                is = new FileInputStream(propFile);
                System.getProperties().load(is);
            }
            catch (IOException e)
            {
                LOG.warning("Unable to load system properties: " + e.getMessage(), e);
            }
            finally
            {
                IOUtils.close(is);
            }
        }
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
            LOG.info("Agent shutdown requested.");
            jettyServer.stop(true);
        }
        catch (InterruptedException e)
        {
            LOG.warning("Exception generated while attempting to shutdown agent. Reason " + e.getMessage(), e);
        }
    }
}
