package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.bootstrap.Data;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.upgrade.ConfigurationAware;
import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.util.io.PropertiesWriter;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Update existing hibernate configuration files, adding
 *
 * hibernate.cache.provider_class=com.zutubi.pulse.master.hibernate.EhCacheProvider
 */
public class ConfigureHibernateCacheProviderUpgradeTask extends AbstractUpgradeTask implements ConfigurationAware
{
    private MasterConfigurationManager configurationManager;
    
    private static final String HIBERNATE_CACHE_PROVIDER_CLASS = "hibernate.cache.provider_class";

    public void execute() throws TaskException
    {
        try
        {
            Data data = configurationManager.getData();

            File databaseConfig = new File(data.getUserConfigRoot(), "database.properties");
            if (databaseConfig.isFile())
            {
                Properties additionalConfig = new Properties();
                additionalConfig.put(HIBERNATE_CACHE_PROVIDER_CLASS, "com.zutubi.pulse.master.hibernate.EhCacheProvider");

                PropertiesWriter writer = new PropertiesWriter();
                writer.write(databaseConfig, additionalConfig);
            }
        }
        catch (IOException e)
        {
            throw new TaskException(e);
        }
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
