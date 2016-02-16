package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.dependency.ivy.IvyStatus;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.handler.FormContext;
import com.zutubi.tove.ui.handler.ListOptionProvider;

import java.util.List;

/**
 * A provider for the list of build statuses.
 */
public class BuildStatusOptionProvider extends ListOptionProvider
{
    public String getEmptyOption(TypeProperty property, FormContext context)
    {
        return null;
    }

    public List<String> getOptions(TypeProperty property, FormContext context)
    {
        return IvyStatus.getStatuses();
    }
}
