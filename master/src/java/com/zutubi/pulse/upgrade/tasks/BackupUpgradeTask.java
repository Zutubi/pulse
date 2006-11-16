package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.upgrade.UpgradeTask;
import com.zutubi.pulse.upgrade.ConfigurationAware;
import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.upgrade.UpgradeException;

import java.util.List;
import java.util.LinkedList;
import java.io.IOException;

/**
 * <class comment/>
 */
public class BackupUpgradeTask implements UpgradeTask, ConfigurationAware
{
    private MasterConfigurationManager configurationManager;

    private List<String> errors = new LinkedList<String>();

    public String getName()
    {
        return "Backup";
    }

    public String getDescription()
    {
        return "Creates a backup of your pulse database.";
    }

    public int getBuildNumber()
    {
        // -/ve indicates that this build number should not be recorded against the target data directory. 
        return -1;
    }

    public void execute(UpgradeContext context) throws UpgradeException
    {
        try
        {
            configurationManager.getData().backup(configurationManager.getSystemPaths());
        }
        catch (IOException e)
        {
            errors.add(e.getMessage());
        }
    }

    public List<String> getErrors()
    {
        return errors;
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public boolean hasFailed()
    {
        return errors.size() != 0;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
