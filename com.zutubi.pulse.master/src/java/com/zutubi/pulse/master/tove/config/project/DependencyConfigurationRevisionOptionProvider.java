package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.tove.handler.FormContext;
import com.zutubi.pulse.master.tove.handler.ListOptionProvider;
import com.zutubi.tove.type.TypeProperty;

import java.util.Arrays;
import java.util.List;

import static com.zutubi.pulse.master.tove.config.project.DependencyConfiguration.*;

/**
 * The option provider implementation for the DependencyConfiguration's revision field.
 */
public class DependencyConfigurationRevisionOptionProvider extends ListOptionProvider
{
    public String getEmptyOption(TypeProperty property, FormContext context)
    {
        return null;
    }

    public List<String> getOptions(TypeProperty property, FormContext context)
    {
        return Arrays.asList(REVISION_LATEST_INTEGRATION, REVISION_LATEST_MILESTONE, REVISION_LATEST_RELEASE, REVISION_CUSTOM);
    }
}
