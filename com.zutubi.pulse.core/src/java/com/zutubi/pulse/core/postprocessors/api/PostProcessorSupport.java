package com.zutubi.pulse.core.postprocessors.api;

/**
 * Basic support for implementation of {@link PostProcessor}.  Stores the
 * configuration associated with the processor.
 *
 * @see OutputPostProcessorSupport
 * @see TestReportPostProcessorSupport
 */
public abstract class PostProcessorSupport implements PostProcessor
{
    private PostProcessorConfigurationSupport config;

    protected PostProcessorSupport(PostProcessorConfigurationSupport config)
    {
        this.config = config;
    }

    public PostProcessorConfigurationSupport getConfig()
    {
        return config;
    }
}