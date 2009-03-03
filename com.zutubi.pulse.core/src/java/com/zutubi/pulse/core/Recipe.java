package com.zutubi.pulse.core;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.commands.api.*;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.events.CommandCommencedEvent;
import com.zutubi.pulse.core.events.CommandCompletedEvent;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorFactory;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 */
public class Recipe
{
    private static final Logger LOG = Logger.getLogger(Recipe.class);

    private static final String LABEL_EXECUTE = "execute";

    private RecipeConfiguration config;

    /**
     * The systems event manager.
     */
    private EventManager eventManager;
    private CommandFactory commandFactory;
    private PostProcessorFactory postProcessorFactory;

    //---( support for the termination of the recipe )---

    /**
     * The currently executing command.
     */
    private Command runningCommand;

    /**
     * Flag indicating whether or not this recipe is terminating.
     */
    private volatile boolean terminating = false;

    /**
     * A lock used to control access to the running state of the recipe.
     */
    private Lock runningLock = new ReentrantLock();

    public Recipe(RecipeConfiguration config)
    {
        this.config = config;
    }

    public String getName()
    {
        return config.getName();
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

    public void execute(PulseExecutionContext context)
    {
        context.push();
        context.setLabel(LABEL_EXECUTE);
        try
        {
            boolean success = true;
            File outputDir = context.getValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, RecipePaths.class).getOutputDir();
            int i = 0;
            for (CommandConfiguration commandConfig: config.getCommands().values())
            {
                if (success || commandConfig.isForce())
                {
                    CommandResult result = new CommandResult(commandConfig.getName());

                    File commandOutput = new File(outputDir, getCommandDirName(i, result));
                    if (!commandOutput.mkdirs())
                    {
                        throw new BuildException("Could not create command output directory '" + commandOutput.getAbsolutePath() + "'");
                    }

                    pushCommandContext(context, commandOutput);
                    boolean recipeTerminated = !executeCommand(context, commandOutput, result, commandConfig);
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

                i++;
            }
        }
        finally
        {
            context.pop();
            RecipeVersionConfiguration version = config.getVersion();
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

    private void pushCommandContext(PulseExecutionContext context, File commandOutput)
    {
        context.push();
        context.addString(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, commandOutput.getAbsolutePath());
    }

    private boolean executeCommand(ExecutionContext context, File commandOutput, CommandResult commandResult, CommandConfiguration commandConfig)
    {
        runningLock.lock();
        if (terminating)
        {
            runningLock.unlock();
            return false;
        }

        Command command = commandFactory.create(commandConfig);
        runningCommand = command;
        runningLock.unlock();

        commandResult.commence();
        commandResult.setOutputDir(commandOutput.getPath());
        long recipeId = context.getLong(NAMESPACE_INTERNAL, PROPERTY_RECIPE_ID, 0);
        eventManager.publish(new CommandCommencedEvent(this, recipeId, commandResult.getCommandName(), commandResult.getStartTime()));

        DefaultCommandContext commandContext = new DefaultCommandContext(context, commandResult, postProcessorFactory);
        try
        {
            executeAndProcess(commandContext, command, commandConfig);
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
            commandContext.addArtifactsToResult();

            runningLock.lock();
            runningCommand = null;
            runningLock.unlock();

            flushOutput(context);
            commandResult.complete();
            eventManager.publish(new CommandCompletedEvent(this, recipeId, commandResult));
        }

        return true;
    }

    private void executeAndProcess(DefaultCommandContext commandContext, Command command, CommandConfiguration commandConfig)
    {
        try
        {
            command.execute(commandContext);
        }
        finally
        {
            // still need to process any available artifacts, even in the event of an error.
            captureOutputs(commandConfig, commandContext);
            commandContext.processOutputs();
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

    private void captureOutputs(CommandConfiguration commandConfig, CommandContext context)
    {
        for (OutputConfiguration outputConfiguration: commandConfig.getOutputs().values())
        {
            Output output = outputConfiguration.createOutput();
            output.capture(context);
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setCommandFactory(CommandFactory commandFactory)
    {
        this.commandFactory = commandFactory;
    }

    public void setPostProcessorFactory(PostProcessorFactory postProcessorFactory)
    {
        this.postProcessorFactory = postProcessorFactory;
    }
}
