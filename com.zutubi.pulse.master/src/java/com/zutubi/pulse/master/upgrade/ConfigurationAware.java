package com.zutubi.pulse.master.upgrade;

import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;

/**
 * Interface for upgrade tasks that require access to the configuration
 * manager to perform the upgrade.
 */
public interface ConfigurationAware
{
    public void setConfigurationManager(MasterConfigurationManager configurationManager);
}
