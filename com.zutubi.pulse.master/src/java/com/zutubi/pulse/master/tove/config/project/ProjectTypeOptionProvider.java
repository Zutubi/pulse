package com.zutubi.pulse.master.tove.config.project;

import static com.zutubi.pulse.master.tove.config.project.ProjectTypeSelectionConfiguration.*;
import com.zutubi.pulse.master.tove.handler.ListOptionProvider;
import com.zutubi.tove.type.TypeProperty;

import java.util.Arrays;
import java.util.List;

/**
 * An option provider that gives the high-level project types available.
 */
public class ProjectTypeOptionProvider extends ListOptionProvider
{
    public String getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return null;
    }

    public List<String> getOptions(Object instance, String parentPath, TypeProperty property)
    {
        return Arrays.asList(TYPE_CUSTOM, TYPE_MULTI_STEP, TYPE_SINGLE_STEP, TYPE_VERSIONED);
    }
}
