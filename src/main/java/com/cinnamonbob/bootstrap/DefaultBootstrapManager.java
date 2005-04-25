package com.cinnamonbob.bootstrap;

import java.io.File;

/**
 * 
 *
 */
public class DefaultBootstrapManager implements BootstrapManager
{

    private ApplicationPaths paths = null;

    public void init() throws StartupException
    {
        String bobHome = System.getProperty("bob.home");
        if (bobHome == null || bobHome.length() == 0)
        {
            // fatal error, BOB_HOME property needs to exist.
            throw new StartupException();
        }

        File bobRoot = new File(bobHome);
        if (!bobRoot.exists() || !bobRoot.isDirectory())
        {
            // fatal error, BOB_HOME property needs to reference bobs home directory
            throw new StartupException();
        }

        paths = new DefaultApplicationPaths(bobRoot);
    }

    public ApplicationPaths getApplicationPaths()
    {
        return paths;
    }
}
