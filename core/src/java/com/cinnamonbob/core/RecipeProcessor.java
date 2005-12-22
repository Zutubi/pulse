package com.cinnamonbob.core;

import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.RecipeResult;
import com.cinnamonbob.events.build.CommandCommencedEvent;
import com.cinnamonbob.events.build.CommandCompletedEvent;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class RecipeProcessor
{
    private EventManager eventManager;
    private ResourceRepository resourceRepository;

    public RecipeProcessor()
    {
        // For use with Spring
    }

    public static String getCommandDirName(int i, CommandResult result)
    {
        // Use the command name because:
        // a) we do not have an id for the command model
        // b) for local builds, this is a lot friendlier for the developer
        return String.format("%08d-%s", i, result.getCommandName());
    }

    public void build(RecipePaths paths, Bootstrapper bootstrapper, String bobFileName, String recipeName, RecipeResult recipeResult) throws BuildException
    {
        bootstrapper.bootstrap(paths);

        BobFile bobFile = loadBobFile(paths.getWorkDir(), bobFileName);
        Recipe recipe;

        if (recipeName == null)
        {
            recipeName = bobFile.getDefaultRecipe();
        }

        recipe = bobFile.getRecipe(recipeName);
        if (recipe == null)
        {
            throw new BuildException("Undefined recipe '" + recipeName + "'");
        }
        build(recipe, recipeResult, paths.getOutputDir());
    }

    public void build(Recipe recipe, RecipeResult recipeResult, File outputDir) throws BuildException
    {

        // TODO: support continuing build when errors occur. Take care: exceptions.
        int i = 0;
        for (Command command : recipe.getCommands())
        {
            CommandResult result = new CommandResult(command.getName());

            recipeResult.add(result);

            File commandOutput = new File(outputDir, getCommandDirName(i, result));
            executeCommand(result, commandOutput, recipeResult, command);

            switch (result.getState())
            {
                case FAILURE:
                    recipeResult.failure();
                    return;
                case ERROR:
                    recipeResult.commandError();
                    return;
            }
            i++;
        }
    }

    private void executeCommand(CommandResult result, File commandOutput, RecipeResult recipeResult, Command command)
    {
        result.commence(commandOutput);
        eventManager.publish(new CommandCommencedEvent(this, recipeResult));

        try
        {
            if (!commandOutput.mkdir())
            {
                throw new BuildException("Could not create command output directory '" + commandOutput.getAbsolutePath() + "'");
            }

            command.execute(commandOutput, result);
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
            eventManager.publish(new CommandCompletedEvent(this, recipeResult));
        }
    }

    private BobFile loadBobFile(File workDir, String bobFileName) throws BuildException
    {
        List<Reference> properties = new LinkedList<Reference>();
        Property property = new Property("work.dir", workDir.getAbsolutePath());
        properties.add(property);

        try
        {
            File bob = new File(workDir, bobFileName);
            FileInputStream stream = new FileInputStream(bob);

            return BobFileLoader.load(stream, resourceRepository, properties);
        }
        catch (Exception e)
        {
            throw new BuildException(e);
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
