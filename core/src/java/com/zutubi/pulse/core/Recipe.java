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
 */
public class Recipe extends SelfReference
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

    public void execute(ExecutionContext context)
    {
        boolean success = true;
        File outputDir = context.getValue(BuildProperties.PROPERTY_RECIPE_PATHS, RecipePaths.class).getOutputDir();
        for (int i = 0; i < commands.size(); i++)
        {
            Command command = commands.get(i);
            if (success || command.isForce())
            {
                CommandResult result = new CommandResult(command.getName());

                File commandOutput = new File(outputDir, getCommandDirName(i, result));
                if (!commandOutput.mkdirs())
                {
                    throw new BuildException("Could not create command output directory '" + commandOutput.getAbsolutePath() + "'");
                }

                if (!executeCommand(context, commandOutput, result, command))
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

    private boolean executeCommand(ExecutionContext context, File commandOutput, CommandResult commandResult, Command command)
    {
        pushCommandContext(context, commandOutput);
        try
        {
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
            long recipeId = context.getLong(BuildProperties.PROPERTY_RECIPE_ID);
            eventManager.publish(new CommandCommencedEvent(this, recipeId, commandResult.getCommandName(), commandResult.getStartTime()));

            try
            {
                try
                {
                    command.execute(context, commandResult);
                }
                finally
                {
                    // still need to process any available artifacts, even in the event of an error.
                    processArtifacts(command, context, commandResult);
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
                eventManager.publish(new CommandCompletedEvent(this, recipeId, commandResult));
            }
            return true;
        }
        finally
        {
            context.popScope();
        }
    }

    private void pushCommandContext(ExecutionContext context, File commandOutput)
    {
        context.pushScope();
        context.addString(BuildProperties.PROPERTY_OUTPUT_DIR, commandOutput.getAbsolutePath());
    }

    private void processArtifacts(Command command, ExecutionContext context, CommandResult result)
    {
        for (Artifact artifact : command.getArtifacts())
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
