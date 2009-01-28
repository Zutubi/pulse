package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.pulse.core.engine.api.Referenceable;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.NamedConfiguration;

/**
 */
@SymbolicName("zutubi.postProcessorConfig")
@Referenceable
public interface PostProcessorConfiguration extends NamedConfiguration
{
    Class<? extends PostProcessor> processorType();
}
