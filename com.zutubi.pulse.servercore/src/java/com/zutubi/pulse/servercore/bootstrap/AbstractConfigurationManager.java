package com.zutubi.pulse.servercore.bootstrap;

import com.zutubi.util.config.PropertiesConfig;
import com.zutubi.pulse.core.util.config.EnvConfig;

import java.io.File;

/**
 * 
 */
public abstract class AbstractConfigurationManager implements ConfigurationManager
{
    private SystemPaths systemPaths = null;
    protected EnvConfig envConfig;

    /**
     * The Environment Config provides read only access to important configuration properties
     * that are defined in the startup environment.
     */
    public EnvConfig getEnvConfig()
    {
        if (envConfig == null)
        {
            envConfig = new EnvConfig(new PropertiesConfig(System.getProperties()));
        }
        return envConfig;
    }

    public SystemPaths getSystemPaths()
    {
        if (systemPaths == null)
        {
            File pulseHome = getHomeDir(getEnvConfig().getPulseHome(), EnvConfig.PULSE_HOME);
            File versionHome = getHomeDir(getEnvConfig().getVersionHome(), EnvConfig.VERSION_HOME);

            // initialise system paths based on pulse.home.
            systemPaths = new DefaultSystemPaths(pulseHome, versionHome);
        }
        return systemPaths;
    }

    private File getHomeDir(String home, String property)
    {
        if (home == null || home.length() == 0)
        {
            // fatal error, PULSE_HOME property needs to exist.
            throw new StartupException("Required property '" + property + "' is not set");
        }

        File homeDir = new File(home);
        if (!homeDir.exists() || !homeDir.isDirectory())
        {
            // fatal error, PULSE_HOME property needs to reference pulse's home directory
            throw new StartupException("Property '" + property + "' does not refer to a " +
                    "directory ('" + home + ")");
        }

        return homeDir;
    }

    public void setSystemPaths(SystemPaths paths)
    {
        this.systemPaths = paths;
    }
}
