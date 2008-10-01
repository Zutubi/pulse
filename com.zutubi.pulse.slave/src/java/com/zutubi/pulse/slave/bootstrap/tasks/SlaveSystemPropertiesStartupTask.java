package com.zutubi.pulse.slave.bootstrap.tasks;

import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 */
public class SlaveSystemPropertiesStartupTask implements StartupTask
{
    private static final Logger LOG = Logger.getLogger(SlaveSystemPropertiesStartupTask.class);

    private ConfigurationManager configurationManager;

    public void execute()
    {
        File propFile = new File(configurationManager.getUserPaths().getUserConfigRoot(), "system.properties");
        if(propFile.exists())
        {
            FileInputStream is = null;
            try
            {
                is = new FileInputStream(propFile);
                System.getProperties().load(is);
            }
            catch (IOException e)
            {
                LOG.warning("Unable to load system properties: " + e.getMessage(), e);
            }
            finally
            {
                IOUtils.close(is);
            }
        }
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
