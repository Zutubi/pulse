package com.cinnamonbob.bootstrap;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
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
            SystemPaths systemPaths = ConfigUtils.getManager().getSystemPaths();
            Logger.getLogger("").addHandler(new FileHandler(systemPaths.getLogRoot().getAbsolutePath() + "/cinnabo%u.log"));
        }
        catch (IOException e)
        {
            throw new StartupException("Unable to configure logging: " + e.getMessage(), e);
        }

        ((StartupManager) ComponentContext.getBean("startupManager")).init();
    }

    public static void main(String argv[])
    {
        new SystemBootstrapManager().bootstrapSystem();
    }
}
