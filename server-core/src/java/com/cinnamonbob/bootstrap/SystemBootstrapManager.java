package com.cinnamonbob.bootstrap;

import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;

import java.io.File;

/**
 * This manager handles the first stage of system startup. It loads a spring context
 * that defines the systems "startupManager" bean and related beans such as the
 * configurationManager.
 *
 * The default bootstrapContext can be overriden using the -Dbootstrap= configuration
 * property. This property can refer to an absolute file location or a resource on the
 * classpath.
 *
 */
public class SystemBootstrapManager
{
    /**
     * The default bootstrapContext file.
     */
    private static final String DEFAULT_BOOTSTRAP_CONTEXT = "com/cinnamonbob/bootstrap/bootstrapContext.xml";

    /**
     * The system property used to override the default bootstrap context.
     */
    public static final String BOOTSTRAP_CONTEXT_PROPERTY = "bootstrap";

    public void bootstrapSystem()
    {
        // lookup bootstrap context via the system properties.
        String contextName = System.getProperty(BOOTSTRAP_CONTEXT_PROPERTY, DEFAULT_BOOTSTRAP_CONTEXT);

        // lookup the context resource.
        // a) try the file system.
        // b) try the classpath.

        File contextFile = new File(contextName);
        ApplicationContext bootstrapContext;
        if (contextFile.exists())
        {
            bootstrapContext = new FileSystemXmlApplicationContext(contextName);
        }
        else // look it up on the classpath.
        {
            bootstrapContext = new ClassPathXmlApplicationContext(contextName);
        }
        ((StartupManager) bootstrapContext.getBean("startupManager")).startup();
    }

    public static void main(String argv[])
    {
        new SystemBootstrapManager().bootstrapSystem();
    }
}
