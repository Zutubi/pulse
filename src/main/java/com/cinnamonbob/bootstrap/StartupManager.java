package com.cinnamonbob.bootstrap;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.beans.BeansException;

/**
 * 
 *
 */
public class StartupManager
{
    private static final StartupManager INSTANCE = new StartupManager();

    public static StartupManager getInstance()
    {
        return INSTANCE;
    }

    public static void startupSystem() throws StartupException
    {
        StartupManager manager = getInstance();

        // startup spring..
        String[] bootstrapConfigLocations = new String[]{
                            "com/cinnamonbob/bootstrap/bootstrapContext.xml"
                    };

        String[] configLocations = new String[] {
                            "com/cinnamonbob/bootstrap/applicationContext.xml",
                            "com/cinnamonbob/bootstrap/webworkContext.xml"
                    };

        try
        {
            // load the bootstrap config first to ensure that it is available to the
            // rest of the system during initialisation.
            manager.applicationContext = new ClassPathXmlApplicationContext(bootstrapConfigLocations);
            manager.applicationContext = new ClassPathXmlApplicationContext(configLocations, manager.applicationContext);

        }
        catch (BeansException b)
        {
            manager.setSystemStarted(false);
            throw new StartupException("An exception has occured during system startup.", b);
        }

        manager.setSystemStarted(true);
    }

    private ApplicationContext applicationContext;

    public ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }

    private boolean systemStarted;

    private void setSystemStarted(boolean b)
    {
        systemStarted = b;
    }

    public boolean isSystemStarted()
    {
        return systemStarted;
    }

    public static Object getBean(String name)
    {
        return getInstance().getApplicationContext().getBean(name);
    }
}
