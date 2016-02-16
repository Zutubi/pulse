package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.handler.FormContext;
import com.zutubi.tove.ui.handler.ListOptionProvider;

import java.util.Arrays;
import java.util.List;

import static com.zutubi.pulse.master.tove.config.project.ProjectTypeSelectionConfiguration.*;

/**
 * An option provider that gives the high-level project types available.
 */
public class ProjectTypeOptionProvider extends ListOptionProvider
{
    public String getEmptyOption(TypeProperty property, FormContext context)
    {
        return null;
    }

    public List<String> getOptions(TypeProperty property, FormContext context)
    {
        return Arrays.asList(TYPE_CUSTOM, TYPE_MULTI_STEP, TYPE_SINGLE_STEP, TYPE_VERSIONED);
    }
}
