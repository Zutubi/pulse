package com.zutubi.pulse.slave;

import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.bootstrap.SystemPaths;
import com.zutubi.pulse.bootstrap.UserPaths;
import com.zutubi.pulse.bootstrap.conf.*;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;

/**
 */
public class DefaultSlaveConfiguration implements SlaveConfiguration, SystemConfiguration
{
    private static final Logger LOG = Logger.getLogger(DefaultSlaveConfiguration.class);

    private static final String PROPERTIES_FILE = "pulse-agent.properties";

    private ConfigSupport config;
    private UserPaths userPaths;
    private SystemPaths systemPaths;

    public DefaultSlaveConfiguration(UserPaths userPaths, SystemPaths systemPaths, EnvConfig env)
    {
        this.userPaths = userPaths;
        this.systemPaths = systemPaths;
        init(env);
    }

    public void init(EnvConfig env)
    {
        Config commandLineAndSystemProperties = new VolatileReadOnlyConfig(System.getProperties());

        File pulseConfigProperties = new File(userPaths.getUserConfigRoot(), PROPERTIES_FILE);
        if (env.hasPulseConfig())
        {
            pulseConfigProperties = new File(env.getPulseConfig());
        }
        FileConfig userConfig = new FileConfig(pulseConfigProperties);

        File systemConfigProperties = new File(systemPaths.getConfigRoot(), PROPERTIES_FILE);
        FileConfig systemConfig = new FileConfig(systemConfigProperties);

        config = new ConfigSupport(new CompositeConfig(commandLineAndSystemProperties, userConfig, systemConfig));
    }

    public String getBindAddress()
    {
        return config.getProperty(WEBAPP_BIND_ADDRESS, "0.0.0.0");
    }

    public int getServerPort()
    {
        return config.getInteger(WEBAPP_PORT, 8090);
    }

    public String getLoggingLevel()
    {
        return config.getProperty(LOGGING_CONFIG, "default");
    }

    public String getContextPath()
    {
        return config.getProperty(CONTEXT_PATH, "/");
    }

    public void setDataPath(String path)
    {
        // n/a
    }

    public String getDataPath()
    {
        // n/a
        return null;
    }

    public void setLoggingLevel(String c)
    {
        config.setProperty(LOGGING_CONFIG, c);
    }

    public boolean isEventLoggingEnabled()
    {
        return config.getBooleanProperty(LOG_EVENTS, false);
    }

    public void setEventLoggingEnabled(boolean b)
    {
        config.setBooleanProperty(LOG_EVENTS, b);
    }
}
