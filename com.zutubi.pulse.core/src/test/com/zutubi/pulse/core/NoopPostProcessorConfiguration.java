package com.zutubi.pulse.core;

import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for the testing no-op post-processor.
 */
@SymbolicName("zutubi.noopPostProcessorConfig")
public class NoopPostProcessorConfiguration extends PostProcessorConfigurationSupport
{
    public NoopPostProcessorConfiguration()
    {
        super(NoopPostProcessor.class);
    }
}