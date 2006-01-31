package com.cinnamonbob.bootstrap;

import java.io.File;

/**
 * This manager handles the first stage of system startup.
 *
 *
 */
public class SystemBootstrapManager
{
    /**
     * The default bootstrapContext file.
     */
    private static final String DEFAULT_BOOTSTRAP_CONTEXT = "com/cinnamonbob/bootstrap/bootstrapContext.xml";

    public static final String BOOTSTRAP_CONTEXT_PROPERTY = "bootstrap";

    public void bootstrapSystem()
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

        ((StartupManager) ComponentContext.getBean("startupManager")).init();
    }

    public static void main(String argv[])
    {
        new SystemBootstrapManager().bootstrapSystem();
    }
}
