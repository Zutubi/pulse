package com.zutubi.pulse.core.postprocessors.test;

import com.zutubi.pulse.core.SelfReference;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.postprocessors.PostProcessor;
import com.zutubi.pulse.core.postprocessors.PostProcessorContext;

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
