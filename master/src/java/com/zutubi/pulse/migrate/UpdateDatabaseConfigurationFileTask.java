package com.zutubi.pulse.migrate;

import com.zutubi.pulse.monitor.AbstractTask;
import com.zutubi.pulse.monitor.TaskException;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;

import java.util.Properties;
import java.io.IOException;

/**
 *
 *
 */
public class UpdateDatabaseConfigurationFileTask  extends AbstractTask
{
    private Properties updatedConfiguration;

    private MasterConfigurationManager configurationManager;

    public UpdateDatabaseConfigurationFileTask(String name)
    {
        super(name);
    }

    public void setUpdatedConfiguration(Properties updatedConfiguration)
    {
        this.updatedConfiguration = updatedConfiguration;
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        try
        {
            configurationManager.updateDatabaseConfig(updatedConfiguration);
        }
        catch (IOException e)
        {
            throw new TaskException(e);
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
