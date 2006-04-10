package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Support class to make implementing the Command interface simpler for the
 * simple cases.
 */
public abstract class CommandSupport implements Command
{
    private String name;

    public CommandSupport()
    {
    }

    public CommandSupport(String name)
    {
        this.name = name;
    }

    public void execute(File baseDir, File outputDir, CommandResult result)
    {
    }

    public List<String> getArtifactNames()
    {
        return new LinkedList<String>();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void terminate()
    {
    }
}
