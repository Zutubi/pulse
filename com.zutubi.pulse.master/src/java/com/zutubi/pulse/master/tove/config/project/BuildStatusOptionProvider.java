package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.dependency.ivy.IvyStatus;
import com.zutubi.pulse.master.tove.handler.FormContext;
import com.zutubi.pulse.master.tove.handler.ListOptionProvider;
import com.zutubi.tove.type.TypeProperty;

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
