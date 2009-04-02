package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.tove.config.DefaultReferenceOptionProvider;
import com.zutubi.pulse.master.tove.handler.MapOption;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.TypeProperty;

import java.util.Map;

/**
 * An option provider that gives a list of stage names along with the option to select [all stages].
 */
public class DependencyStagesOptionProvider extends DefaultReferenceOptionProvider
{
    private ConfigurationProvider configurationProvider;

    public Map<String, String> getMap(Object instance, String path, TypeProperty property)
    {
        Configuration c = configurationProvider.get(path, Configuration.class);
        ProjectConfiguration projectConfig = configurationProvider.getAncestorOfType(c, ProjectConfiguration.class);

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
