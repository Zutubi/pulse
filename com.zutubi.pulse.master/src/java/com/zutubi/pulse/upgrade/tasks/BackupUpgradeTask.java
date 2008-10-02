package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.upgrade.ConfigurationAware;
import com.zutubi.pulse.upgrade.UpgradeException;
import com.zutubi.util.logging.Logger;

/**
 * <class comment/>
 */
public class BackupUpgradeTask extends AbstractUpgradeTask implements ConfigurationAware
{
    private static final Logger LOG = Logger.getLogger(BackupUpgradeTask.class);
    
    private MasterConfigurationManager configurationManager;

    public String getName()
    {
        return "Backup";
    }

    public String getDescription()
    {
        return "Creates a backup of your embedded pulse database (will NOT backup an external database).";
    }

    public int getBuildNumber()
    {
        // -/ve indicates that this build number should not be recorded against the target data directory. 
        return -1;
    }

    public void execute() throws UpgradeException
    {
        try
        {
            configurationManager.getData().backup(configurationManager.getSystemPaths());
        }
        catch (Exception e)
        {
            LOG.severe(e);
            addError(e.getMessage());
        }
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
