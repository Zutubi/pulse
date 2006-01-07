package com.cinnamonbob;

import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.RecipeProcessor;
import com.cinnamonbob.core.event.EventManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 */
public class MasterRecipeProcessor
{
    private ExecutorService executor;
    private RecipeProcessor recipeProcessor;
    private ConfigurationManager configurationManager;
    private EventManager eventManager;

    public MasterRecipeProcessor()
    {
        executor = Executors.newSingleThreadExecutor();
    }

    public void processRecipe(RecipeRequest request)
    {
        executor.execute(new MasterRecipeRunner(request, recipeProcessor, eventManager, configurationManager));
    }

    public void setRecipeProcessor(RecipeProcessor recipeProcessor)
    {
        this.recipeProcessor = recipeProcessor;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
