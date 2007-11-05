package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.StoredFileArtifact;

/**
 */
public class TestPostProcessor extends SelfReference implements PostProcessor
{
    public void process(StoredFileArtifact artifact, CommandResult result, ExecutionContext context)
    {
        artifact.addFeature(new Feature(Feature.Level.ERROR, "Test error message"));
    }
}
