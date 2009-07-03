package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.engine.api.FieldScope;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for instances of {@link CustomFieldsPostProcessor}.
 */
@SymbolicName("zutubi.customFieldPostProcessorConfig")
public class CustomFieldsPostProcessorConfiguration extends PostProcessorConfigurationSupport
{
    private FieldScope scope = FieldScope.RECIPE;

    public CustomFieldsPostProcessorConfiguration()
    {
        super(CustomFieldsPostProcessor.class);
    }

    public CustomFieldsPostProcessorConfiguration(String name)
    {
        super(CustomFieldsPostProcessor.class);
        setName(name);
    }

    public FieldScope getScope()
    {
        return scope;
    }

    public void setScope(FieldScope scope)
    {
        this.scope = scope;
    }
}

