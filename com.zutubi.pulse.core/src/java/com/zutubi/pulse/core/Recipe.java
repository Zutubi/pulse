package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.engine.api.Scope;
import com.zutubi.pulse.core.engine.api.SelfReference;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Pair;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class Recipe extends SelfReference
{
    /**
     * The ordered list of commands that are executed by this recipe.
     */
    private LinkedList<Pair<Command, Scope>> commands = new LinkedList<Pair<Command, Scope>>();

    /**
     * The list of resource dependencies that need to be resolved prior to
     * being able to execute this recipe.
     */
    private List<Dependency> dependencies = new LinkedList<Dependency>();
    private Version version = null;

    /**
     * Default no-arg constructor.
     */
    public Recipe()
    {
    }

    /**
     * Add a new command instance to this recipe.
     *
     * @param command instance
     * @param scope   scope at the point where the command is loaded
     */
    public void add(Command command, Scope scope)
    {
        if (scope != null)
        {
            scope = scope.copyTo(scope.getAncestor(BuildProperties.SCOPE_RECIPE));
        }

        commands.add(new Pair<Command, Scope>(command, scope));
    }

    /**
     * Get the named command instance.
     *
     * @param name of the command being retrieved.
     * @return the named command instance, or null if no matching command was found.
     */
    public Command getCommand(String name)
    {
        for (Pair<Command, Scope> c : commands)
        {
            if (c.first.getName().equals(name))
            {
                return c.first;
            }
        }
        return null;
    }

    /**
     * Get the list of commands associated with this recipe. This list is in the commands execution order.
     *
     * @return list of commands.
     */
    public List<Command> getCommands()
    {
        return CollectionUtils.map(commands, new Mapping<Pair<Command, Scope>, Command>()
        {
            public Command map(Pair<Command, Scope> pair)
            {
                return pair.first;
            }
        });
    }

    public LinkedList<Pair<Command, Scope>> getCommandScopePairs()
    {
        return commands;
    }

    public List<Dependency> getDependencies()
    {
        return Collections.unmodifiableList(dependencies);
    }

    public void addDependency(Dependency dependency)
    {
        dependencies.add(dependency);
    }

    public Version getVersion()
    {
        return version;
    }

    public void addVersion(Version version)
    {
        this.version = version;
    }

    public static String getCommandDirName(int i, CommandResult result)
    {
        // Use the command name because:
        // a) we do not have an id for the command model
        // b) for local builds, this is a lot friendlier for the developer
        return String.format("%08d-%s", i, result.getCommandName());
    }
}
