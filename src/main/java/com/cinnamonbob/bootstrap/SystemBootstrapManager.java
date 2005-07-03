package com.cinnamonbob.bootstrap;

import java.io.File;

/**
 * Handle the system bootstraping.
 *
 */
public class SystemBootstrapManager
{

    private static final String DEFAULT_BOOTSTRAP_CONTEXT = "com/cinnamonbob/bootstrap/bootstrapContext.xml";

    public static final String BOOTSTRAP_CONTEXT_PROPERTY = "bootstrap";

    public void bootstrapSystem()
    {
        // lookup bootstrap context.        
        String contextName = System.getProperty(BOOTSTRAP_CONTEXT_PROPERTY, DEFAULT_BOOTSTRAP_CONTEXT);
        File contextFile = new File(contextName);
        if (contextFile.exists())
        {
            ComponentContext.addFileContextDefinitions(new String[]{contextName});
        }
        else // look it up on the classpath.
        {
            ComponentContext.addClassPathContextDefinitions(new String[]{contextName});
        }        

        ((StartupManager)ComponentContext.getBean("startupManager")).init();
    }

    public static void main(String argv[])
    {
        new SystemBootstrapManager().bootstrapSystem();
    }
}
