package com.zutubi.pulse.slave;

import com.zutubi.pulse.bootstrap.conf.ConfigSupport;
import com.zutubi.pulse.bootstrap.conf.FileConfig;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;

/**
 */
public class DefaultSlaveConfiguration implements SlaveConfiguration
{
    private static final Logger LOG = Logger.getLogger(DefaultSlaveConfiguration.class);

    private static final String PROPERTIES_FILE = "pulse-slave.properties";
    private static final String WEBAPP_PORT = "webapp.port";

    private ConfigSupport config;
    private SlavePaths paths;

    public void init()
    {
        File propertiesFile = new File(paths.getConfigRoot(), PROPERTIES_FILE);
        FileConfig fileConfig = new FileConfig(propertiesFile);
        config = new ConfigSupport(fileConfig);
    }

    public int getWebappPort()
    {
        return config.getInt(WEBAPP_PORT, 8080);
    }

    public void setWebappPort(int port)
    {
        config.setInt(WEBAPP_PORT, port);
    }

    public void setPaths(SlavePaths paths)
    {
        this.paths = paths;
    }
}
