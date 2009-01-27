package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.postprocessors.api.*;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

import java.io.File;
import java.util.List;


/**
 * A group of post-processors.  Simply applies all processors in the group in
 * order.
 */
public class PostProcessorGroup extends PostProcessorSupport
{
    private PostProcessorFactory postProcessorFactory;

    public PostProcessorGroup(PostProcessorGroupConfiguration config)
    {
        super(config);
    }

    public void processFile(File artifactFile, PostProcessorContext ppContext)
    {
        PostProcessorGroupConfiguration config = (PostProcessorGroupConfiguration) getConfig();
        List<PostProcessor> processors = CollectionUtils.map(config.getProcessors(), new Mapping<PostProcessorConfiguration, PostProcessor>()
        {
            public PostProcessor map(PostProcessorConfiguration childConfig)
            {
                return postProcessorFactory.createProcessor(childConfig);
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
