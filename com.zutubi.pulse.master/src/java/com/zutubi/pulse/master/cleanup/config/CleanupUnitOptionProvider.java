package com.zutubi.pulse.master.cleanup.config;

import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.handler.EnumOptionProvider;
import com.zutubi.tove.ui.handler.FormContext;

/**
 * An enum option provider for the CleanupUnit enumeration that does not provide
 * the empty option as a possible selection. 
 */
public class CleanupUnitOptionProvider extends EnumOptionProvider
{
    public Option getEmptyOption(TypeProperty property, FormContext context)
    {
        return null;
    }
}
