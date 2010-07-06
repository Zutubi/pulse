package com.zutubi.pulse.master.migrate;

import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.util.monitor.AbstractTask;
import com.zutubi.pulse.master.util.monitor.TaskException;

import java.io.IOException;
import java.util.Properties;

/**
 *
 *
 */
public class UpdateDatabaseConfigurationFileTask extends AbstractTask
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
