package com.zutubi.pulse.core.marshal.types;

import com.zutubi.tove.annotations.SymbolicName;

@SymbolicName("fakeAntCommand")
public class FakeAntCommand extends FakeCommand
{
    private String buildFile;
    private String targets;

    public FakeAntCommand()
    {
    }

    public FakeAntCommand(String name)
    {
        super(name);
    }

    public String getBuildFile()
    {
        return buildFile;
    }

    public void setBuildFile(String buildFile)
    {
        this.buildFile = buildFile;
    }

    public String getTargets()
    {
        return targets;
    }

    public void setTargets(String targets)
    {
        this.targets = targets;
    }
}
