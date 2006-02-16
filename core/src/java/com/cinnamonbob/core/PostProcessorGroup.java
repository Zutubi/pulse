package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.StoredArtifact;

import java.io.File;
import java.util.LinkedList;
import java.util.List;


/**
 * 
 *
 */
public class PostProcessorGroup implements PostProcessor
{
    private String name;

    private List<PostProcessor> processors = new LinkedList<PostProcessor>();

    public void process(File outputDir, StoredArtifact artifact, CommandResult result)
    {
        for (PostProcessor processor : processors)
        {
            processor.process(outputDir, artifact, result);
        }
    }

    public void add(PostProcessor processor)
    {
        processors.add(processor);
    }

    public String getName()
    {
        return name;
    }

    public Object getValue()
    {
        return this;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
