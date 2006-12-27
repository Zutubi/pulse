package com.zutubi.pulse.core;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class Recipe implements Reference
{
    /**
     * The name uniquely identifying this recipe.
     */
    private String name;

    /**
     * The ordered list of commands that are executed by this recipe.
     */
    private List<Command> commands = new LinkedList<Command>();

    /**
     * The list of resource dependencies that need to be resolved prior to
     * being able to execute this recipe.
     */
    private List<Dependency> dependencies = new LinkedList<Dependency>();

    /**
     * Getter for the recipes name.
     *
     * @return the recipies name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Implementation of the value getter for the Reference interface.
     *
     * @return this
     */
    public Object getValue()
    {
        return this;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Add a new command instance to this recipe.
     *
     * @param command instance
     */
    public void add(Command command)
    {
        commands.add(command);
    }

    /**
     * Get the named command instance.
     *
     * @param name 
     *
     * @return the named command instance, or null if no matching command was found.
     */
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
