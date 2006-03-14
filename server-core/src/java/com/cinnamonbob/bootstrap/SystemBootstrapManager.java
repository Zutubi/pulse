package com.cinnamonbob.bootstrap;

import com.cinnamonbob.freemarker.CustomFreemarkerManager;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This manager handles the first stage of system startup.
 */
public class SystemBootstrapManager
{
    /**
     * The default bootstrapContext file.
     */
    public static final String DEFAULT_BOOTSTRAP_CONTEXT = "com/cinnamonbob/bootstrap/bootstrapContext.xml";
    public static final String BOOTSTRAP_CONTEXT_PROPERTY = "bootstrap";

    /**
     * The name of the configuration manager bean. This bean is REQUIRED in the initial context.
     */
    private static final String CONFIGURATION_MANAGER_BEAN = "configurationManager";

    /**
     * The name of the startup manager bean. This bean is REQUIRED in the initial context.
     */
    private static final String STARTUP_MANAGER_BEAN = "startupManager";

    /**
     * Load the systems bootstrap context.
     */
    public static void loadBootstrapContext()
    {
        // lookup bootstrap context via the system properties.
        String contextName = System.getProperty(BOOTSTRAP_CONTEXT_PROPERTY, DEFAULT_BOOTSTRAP_CONTEXT);

        // lookup the context resource.
        // a) try the file system.
        // b) try the classpath.

        File contextFile = new File(contextName);
        if (contextFile.exists())
        {
            ComponentContext.addFileContextDefinitions(new String[]{contextName});
        }
        else // look it up on the classpath.
        {
            ComponentContext.addClassPathContextDefinitions(new String[]{contextName});
        }
    }

    public void bootstrapSystem()
    {
        loadBootstrapContext();

        // Now we know where the system path is, add a file handler.
        try
        {
            ConfigurationManager configManager = (ConfigurationManager) ComponentContext.getBean(CONFIGURATION_MANAGER_BEAN);
            SystemPaths systemPaths = configManager.getSystemPaths();
            // ensure that the directory exists.

            File logRoot = systemPaths.getLogRoot();
            if (!logRoot.exists() && !logRoot.mkdirs())
            {
                throw new IOException("Unable to create log directory '" + logRoot + "'. Please check that you have " +
                        "permissions to create this directory.");
            }
            FileHandler handler = new FileHandler(logRoot.getAbsolutePath() + "/cinnabo%u.log");
            handler.setLevel(Level.INFO);
            Logger.getLogger("").addHandler(handler);
        }
        catch (IOException e)
        {
            throw new StartupException("Unable to configure logging: " + e.getMessage(), e);
        }

        CustomFreemarkerManager.initialiseLogging();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            public void uncaughtException(Thread t, Throwable e)
            {
                Logger.getLogger("").log(Level.SEVERE, "Uncaught exception: " + e.getMessage(), e);
            }
        });

        ((StartupManager) ComponentContext.getBean(STARTUP_MANAGER_BEAN)).init();
    }

    public static void main(String argv[])
    {
        new SystemBootstrapManager().bootstrapSystem();
    }
}
