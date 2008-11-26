package com.zutubi.pulse.core.postprocessors.test;

import com.zutubi.pulse.core.engine.api.SelfReference;
import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.pulse.core.postprocessors.api.PostProcessor;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorContext;

import java.io.File;

/**
 */
public class TestPostProcessor extends SelfReference implements PostProcessor
{
    public void process(File artifactFile, PostProcessorContext ppContext)
    {
        ppContext.addFeature(new Feature(Feature.Level.ERROR, "Test error message"));
    }
}
