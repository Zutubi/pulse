package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.tove.config.DefaultReferenceOptionProvider;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.TypeProperty;

import java.util.Map;

/**
 * Extension of the default reference option provider that filters out the current
 * project from the list of available options.
 */
public class DependencyProjectOptionProvider extends DefaultReferenceOptionProvider
{
    private ConfigurationProvider configurationProvider;

    @Override
    public Map<String, String> getMap(Object instance, String path, TypeProperty property)
    {
        Map<String, String> map = super.getMap(instance, path, property);

        // Lookup the project we are presently configuring.
        Configuration c = configurationProvider.get(path, Configuration.class);
        ProjectConfiguration projectConfig = configurationProvider.getAncestorOfType(c, ProjectConfiguration.class);

        if (map.containsKey(projectConfig.getConfigurationPath()))
        {
            map.remove(projectConfig.getConfigurationPath());
        }

        return map;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
