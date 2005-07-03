package com.cinnamonbob.bootstrap;

import com.cinnamonbob.bootstrap.jetty.JettyManager;

import java.util.List;

/**
 * 
 *
 */
public class StartupManager
{
    private List<String> contexts;

    private boolean systemStarted;

    public void init() throws StartupException
    {
        if (isSystemStarted())
        {
            throw new StartupException("Attempt to start system when it has already started.");
        }

        try
        {
            ComponentContext.addClassPathContextDefinitions(contexts.toArray(new String[contexts.size()]));
            
            // run the various bootstrap/system startup tasks.
            // initialise database.
            DatabaseBootstrap databaseBootstrap = new DatabaseBootstrap();
            databaseBootstrap.initialiseDatabase();
            
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
    }

    public boolean isSystemStarted()
    {
        return systemStarted;
    }

    public void setSystemContexts(List<String> contexts)
    {
        this.contexts = contexts;
    }    
}
