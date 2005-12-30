package com.cinnamonbob;

import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.Bootstrapper;
import com.cinnamonbob.core.RecipeProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 */
public class MasterRecipeProcessor
{
    private ExecutorService executor;
    private RecipeProcessor recipeProcessor;
    private ConfigurationManager configurationManager;

    public MasterRecipeProcessor()
    {
        executor = Executors.newSingleThreadExecutor();
    }

    public void processRecipe(final RecipeRequest request)
    {
        executor.execute(new Runnable()
        {
            public void run()
            {
                Bootstrapper bootstrapper = new ChainBootstrapper(new ServerBootstrapper(), request.getBootstrapper());
                ServerRecipePaths recipePaths = new ServerRecipePaths(request.getId(), configurationManager);
                recipeProcessor.build(request.getId(), recipePaths, bootstrapper, request.getBobFile(), request.getRecipeName());
            }
        });
    }

    public void setRecipeProcessor(RecipeProcessor recipeProcessor)
    {
        this.recipeProcessor = recipeProcessor;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
