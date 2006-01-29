package com.cinnamonbob.core;

import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.Property;
import com.cinnamonbob.core.model.RecipeResult;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.events.build.CommandCommencedEvent;
import com.cinnamonbob.events.build.CommandCompletedEvent;
import com.cinnamonbob.events.build.RecipeCommencedEvent;
import com.cinnamonbob.events.build.RecipeCompletedEvent;
import com.cinnamonbob.util.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 */
public class RecipeProcessor
{
    private static final Logger LOG = Logger.getLogger(RecipeProcessor.class);

    private EventManager eventManager;
    private ResourceRepository resourceRepository;
    private Lock runningLock = new ReentrantLock();
    private Condition runningCondition = runningLock.newCondition();
    private Command runningCommand = null;
    private boolean terminating = false;

    public RecipeProcessor()
    {
        // For use with Spring
    }

    private String getCommandDirName(int i, CommandResult result)
    {
        // Use the command name because:
        // a) we do not have an id for the command model
        // b) for local builds, this is a lot friendlier for the developer
        return String.format("%08d-%s", i, result.getCommandName());
    }

    public void build(long recipeId, RecipePaths paths, Bootstrapper bootstrapper, String bobFileSource, String recipeName)
    {
        // This result holds only the recipe details (stamps, state etc), not
        // the command results.  A full recipe result with command results is
        // assembled elsewhere.
        RecipeResult result = new RecipeResult(recipeName);
        result.setId(recipeId);
        result.commence(paths.getOutputDir());

        eventManager.publish(new RecipeCommencedEvent(this, recipeId, recipeName, result.getStamps().getStartTime()));

        try
        {
            bootstrapper.bootstrap(recipeId, paths);

            BobFile bobFile = loadBobFile(paths.getWorkDir(), bobFileSource);
            Recipe recipe;

            if (recipeName == null)
            {
                recipeName = bobFile.getDefaultRecipe();
                if (recipeName == null)
                {
                    throw new BuildException("Please specify a default recipe for your project.");
                }
            }

            recipe = bobFile.getRecipe(recipeName);
            if (recipe == null)
            {
                throw new BuildException("Undefined recipe '" + recipeName + "'");
            }

            build(recipeId, recipe, paths.getWorkDir(), paths.getOutputDir());
        }
        catch (BuildException e)
        {
            result.error(e);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            result.error(new BuildException("Unexpected error: " + e.getMessage(), e));
        }
        finally
        {
            result.complete();
            eventManager.publish(new RecipeCompletedEvent(this, result));

            runningLock.lock();
            if (terminating)
            {
                terminating = false;
                runningCondition.signalAll();
            }
            runningLock.unlock();
        }
    }

    public void build(long recipeId, Recipe recipe, File workDir, File outputDir) throws BuildException
    {
        // TODO: support continuing build when errors occur. Take care: exceptions.
        int i = 0;
        for (Command command : recipe.getCommands())
        {
            CommandResult result = new CommandResult(command.getName());

            File commandOutput = new File(outputDir, getCommandDirName(i, result));

            runningLock.lock();
            if (terminating)
            {
                runningLock.unlock();
                return;
            }

            runningCommand = command;
            runningLock.unlock();

            executeCommand(recipeId, result, workDir, commandOutput, command);

            switch (result.getState())
            {
                case FAILURE:
                case ERROR:
                    return;
            }
            i++;
        }
    }

    private void executeCommand(long recipeId, CommandResult result, File workDir, File commandOutput, Command command)
    {
        result.commence(commandOutput);
        eventManager.publish(new CommandCommencedEvent(this, recipeId, result.getCommandName(), result.getStamps().getStartTime()));

        try
        {
            if (!commandOutput.mkdir())
            {
                throw new BuildException("Could not create command output directory '" + commandOutput.getAbsolutePath() + "'");
            }

            command.execute(workDir, commandOutput, result);
        }
        catch (BuildException e)
        {
            result.error(e);
        }
        catch (Exception e)
        {
            result.error(new BuildException(e));
        }
        finally
        {
            result.complete();
            eventManager.publish(new CommandCompletedEvent(this, recipeId, result));
        }
    }

    private BobFile loadBobFile(File workDir, String bobFileSource) throws BuildException
    {
        List<Reference> properties = new LinkedList<Reference>();
        Property property = new Property("work.dir", workDir.getAbsolutePath());
        properties.add(property);

        InputStream stream = null;

        try
        {
            stream = new ByteArrayInputStream(bobFileSource.getBytes());
            return BobFileLoader.load(stream, resourceRepository, properties);
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
        finally
        {
            IOUtils.close(stream);
        }
    }

    public void setResourceRepository(ResourceRepository resourceRepository)
    {
        this.resourceRepository = resourceRepository;
    }

    /**
     * The event manager is a required reference.
     *
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void terminateRecipe() throws InterruptedException
    {
        // Preconditions:
        //   - this call is only made if the processor is executing or will
        //     be shortly executing (i.e. a thread is guaranteed to call
        //     build)
        // Responsibilities of this method:
        //   - after this call, no further command should be started
        //   - if a command is running during this call, it should be
        //     terminated
        //   - this call should not exit until the recipe has been
        //     terminated
        // It is acceptable for the build to commence after this call, so
        // long as no commands (which have unpredictable running time) are
        // started.
        runningLock.lock();
        try
        {
            terminating = true;
            if (runningCommand != null)
            {
                runningCommand.terminate();
            }

            while (terminating)
            {
                runningCondition.await();
            }
        }
        finally
        {
            runningLock.unlock();
        }
    }
}
