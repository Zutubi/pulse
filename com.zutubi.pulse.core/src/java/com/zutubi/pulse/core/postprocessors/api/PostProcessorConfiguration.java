package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.NamedConfiguration;

/**
 */
@SymbolicName("zutubi.postProcessorConfig")
public interface PostProcessorConfiguration extends NamedConfiguration
{
    Class<? extends PostProcessor> processorType();
}
