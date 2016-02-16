package com.zutubi.tove.ui.handler;

import com.zutubi.tove.type.TypeProperty;

import java.util.List;

/**
 * Base for providers where the value and text are the same, so the options are just strings.
 */
public abstract class ListOptionProvider implements OptionProvider
{
    public abstract String getEmptyOption(TypeProperty property, FormContext context);
    public abstract List<String> getOptions(TypeProperty property, FormContext context);

    public String getOptionValue()
    {
        return null;
    }

    public String getOptionText()
    {
        return null;
    }
}
