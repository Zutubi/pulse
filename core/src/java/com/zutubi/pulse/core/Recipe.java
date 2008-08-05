package com.zutubi.pulse.core;

import static com.zutubi.pulse.core.BuildProperties.*;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.CommandCommencedEvent;
import com.zutubi.pulse.events.build.CommandCompletedEvent;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Pair;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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

    private static final String LABEL_EXECUTE = "execute";

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

    /**
     * Add a command to the front of the command execution list. This command will be executed first
     * unless another command is added via this method.
     *
     * @param command to be scheduled before all existing commands.
     */
    public void addFirstCommand(Command command)
    {
        commands.addFirst(new Pair<Command, Scope>(command, null));
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
        context.push();
        context.setLabel(LABEL_EXECUTE);
        try
        {
            boolean success = true;
            File outputDir = context.getValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, RecipePaths.class).getOutputDir();
            for (int i = 0; i < commands.size(); i++)
            {
                Pair<Command, Scope> pair = commands.get(i);
                Command command = pair.first;
                Scope scope = pair.second;

                if (success || command.isForce())
                {
                    CommandResult result = new CommandResult(command.getName());

                    File commandOutput = new File(outputDir, getCommandDirName(i, result));
                    if (!commandOutput.mkdirs())
                    {
                        throw new BuildException("Could not create command output directory '" + commandOutput.getAbsolutePath() + "'");
                    }

                    pushCommandContext(context, scope, commandOutput);
                    boolean recipeTerminated = !executeCommand(context, commandOutput, result, command);
                    context.popTo(LABEL_EXECUTE);

                    if(recipeTerminated)
                    {
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
        finally
        {
            context.pop();
            if (version != null)
            {
                context.setVersion(version.getValue());
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

    private void pushCommandContext(ExecutionContext context, Scope scope, File commandOutput)
    {
        context.push();
        context.addString(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, commandOutput.getAbsolutePath());

        if (scope != null)
        {
            for (Reference reference : scope.getReferences())
            {
                context.add(reference);
            }
        }
    }

    private boolean executeCommand(ExecutionContext context, File commandOutput, CommandResult commandResult, Command command)
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
        long recipeId = context.getLong(NAMESPACE_INTERNAL, PROPERTY_RECIPE_ID);
        eventManager.publish(new CommandCommencedEvent(this, recipeId, commandResult.getCommandName(), commandResult.getStartTime()));

        try
        {
            executeAndProcess(context, commandResult, command);
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

            flushOutput(context);
            commandResult.complete();
            eventManager.publish(new CommandCompletedEvent(this, recipeId, commandResult));
        }

        return true;
    }

    private void executeAndProcess(ExecutionContext context, CommandResult commandResult, Command command)
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

    private void flushOutput(ExecutionContext context)
    {
        OutputStream outputStream = context.getOutputStream();
        if(outputStream != null)
        {
            try
            {
                outputStream.flush();
            }
            catch (IOException e)
            {
                LOG.severe(e);
            }
        }
    }

    private void processArtifacts(Command command, ExecutionContext context, CommandResult result)
    {
        for (Artifact artifact : command.getArtifacts())
        {
            artifact.capture(result, context);
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
