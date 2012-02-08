package com.zutubi.pulse.master;

import com.zutubi.pulse.core.ResourceRepositorySupport;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * A resource repository based on the master's global settings.
 */
public class MasterResourceRepository extends ResourceRepositorySupport
{
    private ConfigurationProvider configurationProvider;

    public ResourceConfiguration getResource(String name)
    {
        GlobalConfiguration globalConfiguration = configurationProvider.get(GlobalConfiguration.class);
        if (globalConfiguration == null)
        {
            return null;
        }
        else
        {
            return globalConfiguration.getResources().get(name);
        }
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
