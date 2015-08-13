package com.zutubi.pulse.master.cleanup.config;

import com.zutubi.pulse.master.tove.config.EnumOptionProvider;
import com.zutubi.tove.type.TypeProperty;

/**
 * An enum option provider for the CleanupUnit enumeration that does not provide
 * the empty option as a possible selection. 
 */
public class CleanupUnitOptionProvider extends EnumOptionProvider
{
    public Option getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return null;
    }
}
