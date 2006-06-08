package com.zutubi.pulse.slave;

import com.zutubi.pulse.bootstrap.conf.ConfigSupport;
import com.zutubi.pulse.bootstrap.conf.FileConfig;
import com.zutubi.pulse.bootstrap.conf.CompositeConfig;
import com.zutubi.pulse.bootstrap.CoreUserPaths;
import com.zutubi.pulse.bootstrap.SystemPaths;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;

/**
 */
public class DefaultSlaveApplicationConfiguration implements SlaveApplicationConfiguration
{
    private static final Logger LOG = Logger.getLogger(DefaultSlaveApplicationConfiguration.class);

    private static final String PROPERTIES_FILE = "pulse-agent.properties";

    private ConfigSupport config;
    private CoreUserPaths userPaths;
    private SystemPaths systemPaths;

    public DefaultSlaveApplicationConfiguration(CoreUserPaths userPaths, SystemPaths systemPaths)
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
        return config.getInt(WEBAPP_PORT, 8090);
    }

    public String getLogConfig()
    {
        return config.getProperty(LOGGING_CONFIG, "default");
    }
}
