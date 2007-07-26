package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;

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

    public void process(StoredFileArtifact artifact, CommandResult result, CommandContext context)
    {
        for (PostProcessor processor : processors)
        {
            processor.process(artifact, result, context);
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

    public int size()
    {
        return processors.size();
    }

    public List<PostProcessor> getProcessors()
    {
        return processors;
    }

    public PostProcessor get(int index)
    {
        return processors.get(index);
    }
}
