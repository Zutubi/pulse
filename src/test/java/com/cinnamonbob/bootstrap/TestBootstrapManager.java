package com.cinnamonbob.bootstrap;

import java.io.File;

/**
 * 
 *
 */
public class TestBootstrapManager implements BootstrapManager
{
    private ApplicationPaths paths = null;

    public ApplicationPaths getApplicationPaths()
    {
        if (paths == null)
        {
            paths = new DefaultApplicationPaths(new File(System.getProperty("bob.home", ".")));
        }
        return paths;
    }
}
