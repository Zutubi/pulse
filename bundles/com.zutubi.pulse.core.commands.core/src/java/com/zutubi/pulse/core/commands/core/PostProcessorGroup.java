package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.postprocessors.api.PostProcessor;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorContext;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorFactory;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

import java.io.File;
import java.util.List;


/**
 * A group of post-processors.  Simply applies all processors in the group in
 * order.
 */
public class PostProcessorGroup implements PostProcessor
{
    private PostProcessorFactory postProcessorFactory;
    private PostProcessorGroupConfiguration config;

    public PostProcessorGroup(PostProcessorGroupConfiguration config)
    {
        this.config = config;
    }

    public PostProcessorGroupConfiguration getConfig()
    {
        return config;
    }

    protected PostProcessor createChildProcessor(PostProcessorConfiguration childConfig)
    {
        return postProcessorFactory.createProcessor(childConfig);
    }

    public void process(File artifactFile, PostProcessorContext ppContext)
    {
        List<PostProcessor> processors = CollectionUtils.map(config.getProcessors().values(), new Mapping<PostProcessorConfiguration, PostProcessor>()
        {
            public PostProcessor map(PostProcessorConfiguration childConfig)
            {
                return createChildProcessor(childConfig);
            }
        });

        for (PostProcessor processor : processors)
        {
            processor.process(artifactFile, ppContext);
        }
    }

    public void setPostProcessorFactory(PostProcessorFactory postProcessorFactory)
    {
        this.postProcessorFactory = postProcessorFactory;
    }
}
