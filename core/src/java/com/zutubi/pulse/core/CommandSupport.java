package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;

import java.util.LinkedList;
import java.util.List;

/**
 * Support class to make implementing the Command interface simpler for the
 * simple cases.
 */
public abstract class CommandSupport implements Command
{
    private String name;
    private boolean force;

    public CommandSupport()
    {
    }

    public CommandSupport(String name)
    {
        this.name = name;
    }

    public void execute(CommandContext context, CommandResult result)
    {
    }

    public List<Artifact> getArtifacts()
    {
        return new LinkedList<Artifact>();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isForce()
    {
        return force;
    }

    public void setForce(boolean force)
    {
        this.force = force;
    }

    public void terminate()
    {
    }
}
