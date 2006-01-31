package com.cinnamonbob;

import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.RecipeProcessor;
import com.cinnamonbob.events.EventManager;
import com.cinnamonbob.core.Stoppable;
import com.cinnamonbob.util.logging.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 */
public class MasterRecipeProcessor implements Stoppable
{
    private static final Logger LOG = Logger.getLogger(MasterRecipeProcessor.class);

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

    public void terminateCurrentRecipe()
    {
        try
        {
            recipeProcessor.terminateRecipe();
        }
        catch (InterruptedException e)
        {
            LOG.warning("Interrupted while terminating recipe", e);
        }
    }

    public void stop(boolean force)
    {
        // We do not take responsibility for shutting down the running
        // recipe, that is controlled at a higher level
        executor.shutdownNow();
    }
}
