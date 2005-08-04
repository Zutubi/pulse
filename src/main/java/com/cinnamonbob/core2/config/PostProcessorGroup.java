package com.cinnamonbob.core2.config;

import java.util.List;
import java.util.LinkedList;

import com.cinnamonbob.model.StoredArtifact;


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

    public void setName(String name)
    {
        this.name = name;
    }
}
