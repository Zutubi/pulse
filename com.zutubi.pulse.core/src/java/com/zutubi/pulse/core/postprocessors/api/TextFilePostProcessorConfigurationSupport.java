package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.tove.annotations.SymbolicName;

/**
 * Support base class for configuration of {@link com.zutubi.pulse.core.postprocessors.api.TextFilePostProcessorSupport}
 * instances.
 */
@SymbolicName("zutubi.textFilePostProcessorConfigSupport")
public abstract class TextFilePostProcessorConfigurationSupport extends OutputPostProcessorConfigurationSupport
{
    protected TextFilePostProcessorConfigurationSupport(Class<? extends TextFilePostProcessorSupport> postProcessorType)
    {
        super(postProcessorType);
    }
}
