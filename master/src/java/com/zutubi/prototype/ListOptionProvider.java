package com.zutubi.prototype;

import com.zutubi.prototype.type.TypeProperty;

import java.util.List;

/**
 */
public abstract class ListOptionProvider implements OptionProvider
{
    public abstract String getEmptyOption(Object instance, String parentPath, TypeProperty property);
    public abstract List<String> getOptions(Object instance, String parentPath, TypeProperty property);

    public String getOptionKey()
    {
        return null;
    }

    public String getOptionValue()
    {
        return null;
    }
}
