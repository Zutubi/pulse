package com.zutubi.pulse.bootstrap;

import java.io.File;

/**
 */
public abstract class AbstractConfigurationManager implements ConfigurationManager
{
    public static final String PULSE_HOME = "pulse.home";

    private SystemPaths systemPaths = null;

    public SystemPaths getSystemPaths()
    {
        if (systemPaths == null)
        {
            String pulseHome = System.getProperty(PULSE_HOME);
            if (pulseHome == null || pulseHome.length() == 0)
            {
                // fatal error, PULSE_HOME property needs to exist.
                throw new StartupException("Required property '" + PULSE_HOME + "' is not set");
            }

            File pulseRoot = new File(pulseHome);
            if (!pulseRoot.exists() || !pulseRoot.isDirectory())
            {
                // fatal error, PULSE_HOME property needs to reference pulse's home directory
                throw new StartupException("Property '" + PULSE_HOME + "' does not refer to a " +
                        "directory ('" + pulseHome + ")");
            }

            // initialise system paths based on pulse.home.
            systemPaths = new DefaultSystemPaths(pulseRoot);
        }
        return systemPaths;
    }

    public void setSystemPaths(SystemPaths paths)
    {
        this.systemPaths = paths;
    }
}
