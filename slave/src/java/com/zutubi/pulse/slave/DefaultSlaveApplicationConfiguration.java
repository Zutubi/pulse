package com.zutubi.pulse.slave;

import com.zutubi.pulse.bootstrap.conf.ConfigSupport;
import com.zutubi.pulse.bootstrap.conf.FileConfig;
import com.zutubi.pulse.bootstrap.CoreUserPaths;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;

/**
 */
public class DefaultSlaveApplicationConfiguration implements SlaveApplicationConfiguration
{
    private static final Logger LOG = Logger.getLogger(DefaultSlaveApplicationConfiguration.class);

    private static final String PROPERTIES_FILE = "pulse-slave.properties";
    private static final String WEBAPP_PORT = "webapp.port";

    private ConfigSupport config;
    private CoreUserPaths userPaths;

    public DefaultSlaveApplicationConfiguration(CoreUserPaths userPaths)
    {
        this.userPaths = userPaths;
        init();
    }

    public void init()
    {
        File propertiesFile = new File(userPaths.getUserConfigRoot(), PROPERTIES_FILE);
        FileConfig fileConfig = new FileConfig(propertiesFile);
        config = new ConfigSupport(fileConfig);
    }

    public int getServerPort()
    {
        return config.getInt(WEBAPP_PORT, 8080);
    }

    public void setUserPaths(CoreUserPaths userPaths)
    {
        this.userPaths = userPaths;
    }
}
