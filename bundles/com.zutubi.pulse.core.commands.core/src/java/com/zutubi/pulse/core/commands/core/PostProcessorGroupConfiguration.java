package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.postprocessors.api.PostProcessor;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;

import java.util.LinkedList;
import java.util.List;


/**
 * A group of post-processors.  Simply applies all processors in the group in
 * order.
 */
@SymbolicName("zutubi.postProcessorGroupConfig")
public class PostProcessorGroupConfiguration extends PostProcessorConfigurationSupport
{
    @Reference
    private List<PostProcessorConfiguration> processors = new LinkedList<PostProcessorConfiguration>();

    public PostProcessorGroupConfiguration()
    {
        super(PostProcessorGroup.class);
    }

    public List<PostProcessorConfiguration> getProcessors()
    {
        return processors;
    }

    public void setProcessors(List<PostProcessorConfiguration> processors)
    {
        this.processors = processors;
    }

    public int size()
    {
        return processors.size();
    }

    public PostProcessor createProcessor()
    {
        PostProcessorGroup result = new PostProcessorGroup();
        for (PostProcessorConfiguration childConfig: processors)
        {
            result.add(childConfig.createProcessor());
        }

        return result;
    }
}