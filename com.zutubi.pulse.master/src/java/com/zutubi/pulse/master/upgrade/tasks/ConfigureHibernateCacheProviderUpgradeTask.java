/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
