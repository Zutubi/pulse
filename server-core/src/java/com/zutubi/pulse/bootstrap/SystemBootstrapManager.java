package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.freemarker.CustomFreemarkerManager;

import java.io.File;
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
    public static final String DEFAULT_BOOTSTRAP_CONTEXT = "com/zutubi.pulse/bootstrap/bootstrapContext.xml";
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
