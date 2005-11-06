package com.cinnamonbob.bootstrap;

import com.cinnamonbob.bootstrap.jetty.JettyManager;

import java.util.List;

/**
 * 
 *
 */
public class StartupManager
{
    private List<String> systemContexts;
    private List<String> databaseContexts;

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
            ComponentContext.addClassPathContextDefinitions(databaseContexts.toArray(new String[databaseContexts.size()]));

            // run the various bootstrap/system startup tasks.
            // initialise database.
            DatabaseBootstrap databaseBootstrap = new DatabaseBootstrap();
            databaseBootstrap.initialiseDatabase();

            ComponentContext.addClassPathContextDefinitions(systemContexts.toArray(new String[systemContexts.size()]));
            
            // initialise jetty.
            JettyManager jettyManager = JettyManager.getInstance();
            jettyManager.deployWebapp();
            
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

    public void setDatabaseContexts(List<String> contexts)
    {
        this.databaseContexts = contexts;
    }
}
