package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Configuration for instances of {@link com.zutubi.pulse.core.commands.core.PostProcessorGroup}.
 */
@SymbolicName("zutubi.postProcessorGroupConfig")
public class PostProcessorGroupConfiguration extends PostProcessorConfigurationSupport
{
    @Ordered
    private Map<String, PostProcessorConfiguration> processors = new LinkedHashMap<String, PostProcessorConfiguration>();

    public PostProcessorGroupConfiguration()
    {
        super(PostProcessorGroup.class);
    }

    public PostProcessorGroupConfiguration(Class<? extends PostProcessorGroup> postProcessorType)
    {
        super(postProcessorType);
    }

    public Map<String, PostProcessorConfiguration> getProcessors()
    {
        return processors;
    }

    public void setProcessors(Map<String, PostProcessorConfiguration> processors)
    {
        this.processors = processors;
    }

    public void addPostProcessor(PostProcessorConfiguration postProcessor)
    {
        processors.put(postProcessor.getName(), postProcessor);
    }
}