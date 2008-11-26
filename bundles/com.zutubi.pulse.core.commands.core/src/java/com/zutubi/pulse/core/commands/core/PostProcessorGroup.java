package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.SelfReference;
import com.zutubi.pulse.core.postprocessors.PostProcessor;
import com.zutubi.pulse.core.postprocessors.PostProcessorContext;

import java.io.File;
import java.util.LinkedList;
import java.util.List;


/**
 * A group of post-processors.  Simply applies all processors in the group in
 * order.
 */
public class PostProcessorGroup extends SelfReference implements PostProcessor
{
    private List<PostProcessor> processors = new LinkedList<PostProcessor>();

    public void process(File artifactFile, PostProcessorContext ppContext)
    {
        for (PostProcessor processor : processors)
        {
            processor.process(artifactFile, ppContext);
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
