package com.cinnamonbob;

import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.Bootstrapper;
import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.RecipeProcessor;
import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.events.build.RecipeErrorEvent;

/**
 */
public class MasterRecipeRunner implements Runnable
{
    private RecipeRequest request;
    private RecipeProcessor recipeProcessor;
    private EventManager eventManager;
    private ConfigurationManager configurationManager;

    public MasterRecipeRunner(RecipeRequest request, RecipeProcessor recipeProcessor, EventManager eventManager, ConfigurationManager configurationManager)
    {
        this.request = request;
        this.recipeProcessor = recipeProcessor;
        this.eventManager = eventManager;
        this.configurationManager = configurationManager;
    }

    public void run()
    {
        Bootstrapper bootstrapper = new ChainBootstrapper(new ServerBootstrapper(), request.getBootstrapper());
        ServerRecipePaths recipePaths = new ServerRecipePaths(request.getId(), configurationManager);

        try
        {
            recipeProcessor.build(request.getId(), recipePaths, bootstrapper, request.getBobFile(), request.getRecipeName());
        }
        catch (BuildException e)
        {
            eventManager.publish(new RecipeErrorEvent(this, request.getId(), e.getMessage()));
        }
        catch (Exception e)
        {
            eventManager.publish(new RecipeErrorEvent(this, request.getId(), "Unexpected error: " + e.getMessage()));
        }
    }

}
