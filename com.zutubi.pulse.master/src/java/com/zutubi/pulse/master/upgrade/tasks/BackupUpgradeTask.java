package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.upgrade.ConfigurationAware;
import com.zutubi.pulse.master.upgrade.UpgradeException;
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
        return "Create a cut down PULSE_DATA snapshot prior to upgrading.";
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
