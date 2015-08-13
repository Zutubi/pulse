package com.zutubi.pulse.master.tove.handler;

import com.zutubi.tove.type.TypeProperty;

import java.util.List;

/**
 * Base for providers where the value and text are the same, so the options are just strings.
 */
public abstract class ListOptionProvider implements OptionProvider
{
    public abstract String getEmptyOption(Object instance, String parentPath, TypeProperty property);
    public abstract List<String> getOptions(Object instance, String parentPath, TypeProperty property);

    public String getOptionValue()
    {
        return null;
    }

    public String getOptionText()
    {
        return null;
    }
}
