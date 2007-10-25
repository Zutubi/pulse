package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;

/**
 */
public class TestPostProcessor implements PostProcessor
{
    private String name;

    public void process(StoredFileArtifact artifact, CommandResult result, CommandContext context)
    {
        artifact.addFeature(new Feature(Feature.Level.ERROR, "Test error message"));
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Object getValue()
    {
        return this;
    }
}
