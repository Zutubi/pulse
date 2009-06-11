package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for instances of {@link CustomFieldsPostProcessor}.
 */
@SymbolicName("zutubi.customFieldPostProcessorConfig")
public class CustomFieldsPostProcessorConfiguration extends PostProcessorConfigurationSupport
{
    public CustomFieldsPostProcessorConfiguration()
    {
        super(CustomFieldsPostProcessor.class);
    }

    public CustomFieldsPostProcessorConfiguration(String name)
    {
        super(CustomFieldsPostProcessor.class);
        setName(name);
    }
}

