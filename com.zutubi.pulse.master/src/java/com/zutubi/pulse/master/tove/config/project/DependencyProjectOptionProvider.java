package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.forms.DefaultReferenceOptionProvider;
import com.zutubi.tove.ui.forms.FormContext;

import java.util.Map;

/**
 * Extension of the default reference option provider that filters out the current
 * project from the list of available options.
 */
public class DependencyProjectOptionProvider extends DefaultReferenceOptionProvider
{
    private ConfigurationProvider configurationProvider;

    @Override
    public Map<String, String> getMap(TypeProperty property, FormContext context)
    {
        Map<String, String> map = super.getMap(property, context);

        // Lookup the project we are presently configuring.
        ProjectConfiguration projectConfig = configurationProvider.getAncestorOfType(context.getClosestExistingPath(), ProjectConfiguration.class);

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
