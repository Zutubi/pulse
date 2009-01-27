package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.tove.annotations.SymbolicName;

/**
 */
@SymbolicName("zutubi.textFilePostProcessorConfigSupport")
public class TextFilePostProcessorConfigurationSupport extends PostProcessorConfigurationSupport
{
    protected TextFilePostProcessorConfigurationSupport(Class<? extends TextFilePostProcessorSupport> postProcessorType)
    {
        super(postProcessorType);
    }
}
