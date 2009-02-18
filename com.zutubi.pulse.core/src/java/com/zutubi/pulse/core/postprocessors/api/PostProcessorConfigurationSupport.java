package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

/**
 * Support base class to configure an instance of {@link OutputPostProcessorSupport}.
 */
@SymbolicName("zutubi.postProcessorConfigSupport")
public abstract class PostProcessorConfigurationSupport extends AbstractNamedConfiguration implements PostProcessorConfiguration
{
    private Class<? extends PostProcessor> postProcessorType;

    protected PostProcessorConfigurationSupport(Class<? extends PostProcessor> postProcessorType)
    {
        this.postProcessorType = postProcessorType;
    }

    public Class<? extends PostProcessor> processorType()
    {
        return postProcessorType;
    }
}