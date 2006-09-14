package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.bootstrap.conf.EnvConfig;
import com.zutubi.pulse.config.PropertiesConfig;

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
            String versionHome = getEnvConfig().getVersionHome();
            if (versionHome == null || versionHome.length() == 0)
            {
                // fatal error, PULSE_HOME property needs to exist.
                throw new StartupException("Required property '" + EnvConfig.VERSION_HOME + "' is not set");
            }

            File pulseRoot = new File(versionHome);
            if (!pulseRoot.exists() || !pulseRoot.isDirectory())
            {
                // fatal error, PULSE_HOME property needs to reference pulse's home directory
                throw new StartupException("Property '" + EnvConfig.VERSION_HOME + "' does not refer to a " +
                        "directory ('" + versionHome + ")");
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
