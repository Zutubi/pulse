package com.zutubi.pulse.slave;

import com.zutubi.pulse.bootstrap.SystemPaths;
import com.zutubi.pulse.bootstrap.UserPaths;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.bootstrap.conf.CompositeConfig;
import com.zutubi.pulse.bootstrap.conf.ConfigSupport;
import com.zutubi.pulse.bootstrap.conf.FileConfig;
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

    public DefaultSlaveConfiguration(UserPaths userPaths, SystemPaths systemPaths)
    {
        this.userPaths = userPaths;
        this.systemPaths = systemPaths;
        init();
    }

    public void init()
    {
        File userProperties = new File(userPaths.getUserConfigRoot(), PROPERTIES_FILE);
        FileConfig userConfig = new FileConfig(userProperties);
        File systemProperties = new File(systemPaths.getConfigRoot(), PROPERTIES_FILE);
        FileConfig systemConfig = new FileConfig(systemProperties);
        config = new ConfigSupport(new CompositeConfig(userConfig, systemConfig));
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

    public void setLoggingLevel(String c)
    {
        config.setProperty(LOGGING_CONFIG, c);
    }
}
