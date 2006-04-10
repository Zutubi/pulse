package com.zutubi.pulse.core;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

/**
 * 
 *
 */
public class Recipe implements Reference, Namespace
{
    private String name;

    private List<Command> commands = new LinkedList<Command>();
    private List<Dependency> dependencies = new LinkedList<Dependency>();

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
    
    public void add(Command c)
    {
        commands.add(c);
    }

    public Command getCommand(String name)
    {
        for(Command c: commands)
        {
            if(c.getName().equals(name))
            {
                return c;
            }
        }

        return null;
    }

    public List<Command> getCommands()
    {
        return Collections.unmodifiableList(commands);
    }

    public List<Dependency> getDependencies()
    {
        return Collections.unmodifiableList(dependencies);
    }

    public void addDependency(Dependency dependency)
    {
        dependencies.add(dependency);
    }
}
