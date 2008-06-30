package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.postprocessors.PostProcessor;

import java.util.LinkedList;
import java.util.List;


/**
 * 
 *
 */
public class PostProcessorGroup extends SelfReference implements PostProcessor
{
    private List<PostProcessor> processors = new LinkedList<PostProcessor>();

    public void process(StoredFileArtifact artifact, CommandResult result, ExecutionContext context)
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
