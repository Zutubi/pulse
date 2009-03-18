package com.zutubi.pulse.core.marshal.types;

import com.zutubi.tove.annotations.SymbolicName;

import java.util.LinkedList;
import java.util.List;

@SymbolicName("fakeMakeCommand")
public class FakeMakeCommand extends FakeCommand
{
    private String makefile;
    private List<String> targets = new LinkedList<String>();

    public FakeMakeCommand()
    {
    }

    public FakeMakeCommand(String name)
    {
        super(name);
    }

    public String getMakefile()
    {
        return makefile;
    }

    public void setMakefile(String makefile)
    {
        this.makefile = makefile;
    }

    public List<String> getTargets()
    {
        return targets;
    }

    public void setTargets(List<String> targets)
    {
        this.targets = targets;
    }
}