package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeException;
import com.zutubi.pulse.upgrade.ConfigurationAware;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.Data;

import java.util.List;
import java.util.LinkedList;

/**
 * <class comment/>
 */
public class TransferExampleTemplatesUpgradeTask implements PulseUpgradeTask, ConfigurationAware
{
    private int buildNumber;

    private List<String> errors = new LinkedList<String>();
    private MasterConfigurationManager configurationManager;

    public String getName()
    {
        return "Transfer example templates";
    }

    public String getDescription()
    {
        return "Transfer example templates into the PULSE_DATA/config/templates directory.";
    }

    public void setBuildNumber(int buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    public int getBuildNumber()
    {
        return buildNumber;
    }

    public void execute() throws UpgradeException
    {
        // this is part of the initialisation process of the data directory. However, if the data directory
        // was created before this initialisation was introduced, the upgrade task is required.
        Data data = configurationManager.getData();
        data.transferExampleTemplates(configurationManager.getSystemPaths());
    }

    public List<String> getErrors()
    {
        return errors;
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public boolean hasFailed()
    {
        return errors.size() > 0;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
