package com.zutubi.pulse.core;

import com.zutubi.pulse.core.postprocessors.api.PostProcessorContext;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorSupport;

import java.io.File;

/**
 * A do-nothing post-processor for testing.
 */
public class NoopPostProcessor extends PostProcessorSupport
{
    public NoopPostProcessor(NoopPostProcessorConfiguration config)
    {
        super(config);
    }

    public void process(File artifactFile, PostProcessorContext ppContext)
    {
    }
}
