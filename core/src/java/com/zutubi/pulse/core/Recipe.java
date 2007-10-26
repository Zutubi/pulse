package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.CommandCommencedEvent;
import com.zutubi.pulse.events.build.CommandCompletedEvent;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 *
 */
public class Recipe implements Reference
{
    private static final Logger LOG = Logger.getLogger(Recipe.class);

    /**
     * The name uniquely identifying this recipe.
     */
    private String name;

    /**
     * The ordered list of commands that are executed by this recipe.
     */
    private LinkedList<Command> commands = new LinkedList<Command>();

    /**
     * The list of resource dependencies that need to be resolved prior to
     * being able to execute this recipe.
     */
    private List<Dependency> dependencies = new LinkedList<Dependency>();

    /**
     * The systems event manager.
     */
    private EventManager eventManager;

    //---( support for the termination of the recipe )---

    /**
     * The currently executing command.
     */
    private Command runningCommand;

    /**
     * Flag indicating whether or not this recipe is terminating.
     */
    private boolean terminating = false;

    /**
     * A lock used to control access to the running state of the recipe.
     */
    private Lock runningLock = new ReentrantLock();

    /**
     * Default no-arg constructor.
     */
    public Recipe()
    {
    }

    //---( implementation of the reference interface )---

    /**
     * Getter for the recipes name.
     *
     * @return the recipies name.
     */
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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
     * @param name of the command being retrieved.
     * 
     * @return the named command instance, or null if no matching command was found.
     */
    public Command getCommand(String name)
    {
        for (Command c : commands)
        {
            if (c.getName().equals(name))
            {
                return c;
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

    /**
     * Add a command to the front of the command execution list. This command will be executed first
     * unless another command is added via this method.
     *
     * @param command to be scheduled before all existing commands.
     */
    public void addFirstCommand(Command command)
    {
        commands.addFirst(command);
    }

    /**
     * Terminate this 
     */
    public void terminate()
    {
        runningLock.lock();
        try
        {
            terminating = true;
            if (runningCommand != null)
            {
                runningCommand.terminate();
            }
        }
        finally
        {
            runningLock.unlock();
        }
    }

    public void execute(RecipeContext context)
    {
        boolean success = true;
        for (int i = 0; i < commands.size(); i++)
        {
            Command command = commands.get(i);
            if (success || command.isForce())
            {
                CommandResult result = new CommandResult(command.getName());

                File commandOutput = new File(context.getPaths().getOutputDir(), getCommandDirName(i, result));
                if (!commandOutput.mkdirs())
                {
                    throw new BuildException("Could not create command output directory '" + commandOutput.getAbsolutePath() + "'");
                }

                CommandContext commandContext = createCommandContext(context, commandOutput);
                if (!executeCommand(commandContext, result, command))
                {
                    // Recipe terminated.
                    return;
                }

                switch (result.getState())
                {
                    case FAILURE:
                    case ERROR:
                        success = false;
                }
            }
        }
    }

    public static String getCommandDirName(int i, CommandResult result)
    {
        // Use the command name because:
        // a) we do not have an id for the command model
        // b) for local builds, this is a lot friendlier for the developer
        return String.format("%08d-%s", i, result.getCommandName());
    }

    private boolean executeCommand(CommandContext commandContext, CommandResult commandResult, Command command)
    {
        File commandOutput = commandContext.getOutputDir();

        runningLock.lock();
        if (terminating)
        {
            runningLock.unlock();
            return false;
        }

        runningCommand = command;
        runningLock.unlock();

        commandResult.commence();
        commandResult.setOutputDir(commandOutput.getPath());
        eventManager.publish(new CommandCommencedEvent(this, commandContext.getRecipeId(), commandResult.getCommandName(), commandResult.getStartTime()));

        try
        {
            try
            {
                command.execute(commandContext, commandResult);
            }
            finally
            {
                // still need to process any available artifacts, even in the event of an error.
                processArtifacts(command, commandContext, commandResult);
            }
        }
        catch (BuildException e)
        {
            commandResult.error(e);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            commandResult.error(new BuildException("Unexpected error: " + e.getMessage(), e));
        }
        finally
        {
            runningLock.lock();
            runningCommand = null;
            runningLock.unlock();

            commandResult.complete();
            eventManager.publish(new CommandCompletedEvent(this, commandContext.getRecipeId(), commandResult));
        }
        return true;
    }

    private void processArtifacts(Command command, CommandContext context, CommandResult result)
    {
        for(Artifact artifact: command.getArtifacts())
        {
            try
            {
                artifact.capture(result, context);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private CommandContext createCommandContext(RecipeContext recipeContext, File commandOutput)
    {
        CommandContext commandContext = new CommandContext();
        commandContext.setRecipeContext(recipeContext);
        commandContext.setOutputDir(commandOutput);
        return commandContext;
    }

    /**
     * Required resource.
     *
     * @param eventManager instance.
     */
    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
