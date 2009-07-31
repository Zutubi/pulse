package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.tove.handler.ListOptionProvider;
import static com.zutubi.pulse.master.tove.config.project.DependencyConfiguration.*;
import com.zutubi.tove.type.TypeProperty;

import java.util.List;
import java.util.Arrays;

/**
 * The option provider implementation for the DependencyConfiguration's revision field.
 */
public class DependencyConfigurationRevisionOptionProvider extends ListOptionProvider
{
    public String getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return null;
    }

    public List<String> getOptions(Object instance, String parentPath, TypeProperty property)
    {
        return Arrays.asList(REVISION_LATEST_INTEGRATION, REVISION_LATEST_MILESTONE, REVISION_LATEST_RELEASE, REVISION_CUSTOM);
    }
}
