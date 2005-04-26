package com.cinnamonbob.bootstrap;

import java.io.File;

/**
 *
 *
 */
public class TestBootstrapManager implements BootstrapManager
{
    private ApplicationPaths paths = null;

    public void init() throws StartupException
    {
        String bobHome = System.getProperty("bob.home");
        if (bobHome == null)
        {
            throw new StartupException();
        }
        
        paths = new TestApplicationPaths(new File(bobHome));
    }

    public ApplicationPaths getApplicationPaths()
    {
        return paths;
    }
}
