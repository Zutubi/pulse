package com.zutubi.pulse.core.postprocessors.test;

import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.SelfReference;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.postprocessors.PostProcessor;

/**
 */
public class TestPostProcessor extends SelfReference implements PostProcessor
{
    public void process(StoredFileArtifact artifact, CommandResult result, ExecutionContext context)
    {
        artifact.addFeature(new Feature(Feature.Level.ERROR, "Test error message"));
    }
}
