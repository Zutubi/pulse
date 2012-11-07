package com.zutubi.pulse.core.marshal.types;

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

import java.util.LinkedList;
import java.util.List;

@SymbolicName("fakeCommand")
public abstract class FakeCommand extends AbstractNamedConfiguration
{
    @Reference
    @Addable(value = "process", attribute = "processor")
    private List<FakePostProcessor> postProcessors = new LinkedList<FakePostProcessor>();

    protected FakeCommand()
    {
    }

    public FakeCommand(String name)
    {
        super(name);
    }

    public List<FakePostProcessor> getPostProcessors()
    {
        return postProcessors;
    }

    public void setPostProcessors(List<FakePostProcessor> postProcessors)
    {
        this.postProcessors = postProcessors;
    }

    public void addPostProcessor(FakePostProcessor postProcessor)
    {
        postProcessors.add(postProcessor);
    }
}
