package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.tove.config.DefaultReferenceOptionProvider;
import com.zutubi.pulse.master.tove.handler.MapOption;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.TypeProperty;

import java.util.Map;
import java.util.Collections;

/**
 * An option provider that gives a list of stage names.
 */
public class DependencyStagesOptionProvider extends DefaultReferenceOptionProvider
{
    private ConfigurationProvider configurationProvider;

    public Map<String, String> getMap(Object instance, String path, TypeProperty property)
    {
        if (instance == null)
        {
            return Collections.emptyMap();
        }

        ProjectConfiguration projectConfig;
        if (instance instanceof ProjectConfiguration)
        {
            projectConfig = (ProjectConfiguration) instance;
        }
        else
        {
            Configuration c = configurationProvider.get(path, Configuration.class);
            projectConfig = configurationProvider.getAncestorOfType(c, ProjectConfiguration.class);
        }

        return super.getMap(instance, projectConfig.getConfigurationPath(), property);
    }

    public MapOption getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return null;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
