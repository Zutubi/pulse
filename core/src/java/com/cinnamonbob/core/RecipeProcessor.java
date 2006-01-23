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

/**
 */
public class RecipeProcessor
{
    private static final Logger LOG = Logger.getLogger(RecipeProcessor.class);

    private EventManager eventManager;
    private ResourceRepository resourceRepository;

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
}
