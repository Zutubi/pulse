package com.cinnamonbob.bootstrap;

import com.cinnamonbob.jetty.JettyManager;

import java.util.List;

/**
 * 
 *
 */
public class StartupManager
{
    private List<String> systemContexts;

    private boolean systemStarted;
    private long startTime;

    public void init() throws StartupException
    {
        if (isSystemStarted())
        {
            throw new StartupException("Attempt to start system when it has already started.");
        }

        try
        {

            for (String context : systemContexts)
            {
                ComponentContext.addClassPathContextDefinitions(new String[]{context});
                // init.
                ComponentContext.getBean("initContext");
            }

            setSystemStarted(true);
        }
        catch (Exception e)
        {
            throw new StartupException(e);
        }
    }

    private void setSystemStarted(boolean b)
    {
        systemStarted = b;
        if (systemStarted)
        {
            startTime = System.currentTimeMillis();
        }
    }

    public boolean isSystemStarted()
    {
        return systemStarted;
    }

    public void setSystemContexts(List<String> contexts)
    {
        this.systemContexts = contexts;
    }

    public long getUptime()
    {
        return System.currentTimeMillis() - startTime;
    }

}
