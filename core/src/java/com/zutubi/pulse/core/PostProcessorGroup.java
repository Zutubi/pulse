package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;

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

    public void process(File outputDir, StoredFileArtifact artifact, CommandResult result)
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

    public int size()
    {
        return processors.size();
    }

    public PostProcessor get(int index)
    {
        return processors.get(index);
    }
}
