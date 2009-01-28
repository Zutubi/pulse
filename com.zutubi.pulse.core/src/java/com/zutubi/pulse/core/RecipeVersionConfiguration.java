package com.zutubi.pulse.core;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

/**
 * Used to set the build version.
 */
@SymbolicName("zutubi.recipeVersionConfig")
public class RecipeVersionConfiguration extends AbstractConfiguration
{
    private String value;

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
