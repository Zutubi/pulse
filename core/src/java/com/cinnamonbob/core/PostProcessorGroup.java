package com.cinnamonbob.core;

import com.cinnamonbob.core.model.StoredArtifact;

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

    public void process(StoredArtifact a)
    {
        for (PostProcessor processor: processors)
        {
            processor.process(a);
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
