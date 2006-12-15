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
    /**
     * The name of the command.
     */
    private String name;

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

    /**
     * The name of the command is used to uniquely identify the command
     * within its execution context.
     *
     * @return the name of the command.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Setter for the commands name property.
     *
     * @param name of this command.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public void terminate()
    {
    }
}
