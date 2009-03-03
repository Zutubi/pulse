package com.zutubi.pulse.core.postprocessors;

import com.zutubi.pulse.core.ConfiguredInstanceFactory;
import com.zutubi.pulse.core.postprocessors.api.PostProcessor;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorFactory;

/**
 * Default implementation of {@link com.zutubi.pulse.core.postprocessors.api.PostProcessorFactory},
 * which uses the object factory to build processors.
 */
public class DefaultPostProcessorFactory extends ConfiguredInstanceFactory<PostProcessor, PostProcessorConfiguration> implements PostProcessorFactory
{
    protected Class<? extends PostProcessor> getType(PostProcessorConfiguration configuration)
    {
        return configuration.processorType();
    }
}
