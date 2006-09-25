package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.RecipeProcessor;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.util.logging.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 */
public class MasterRecipeProcessor implements Stoppable
{
    private static final Logger LOG = Logger.getLogger(MasterRecipeProcessor.class);

    private ExecutorService executor;
    private RecipeProcessor recipeProcessor;
    private MasterConfigurationManager configurationManager;
    private EventManager eventManager;
    private ResourceManager resourceManager;

    public MasterRecipeProcessor()
    {
        executor = Executors.newSingleThreadExecutor();
    }

    public void processRecipe(RecipeRequest request, BuildContext context)
    {
        executor.execute(new MasterRecipeRunner(request, recipeProcessor, eventManager, configurationManager, resourceManager.getMasterRepository(), context));
    }

    public void setRecipeProcessor(RecipeProcessor recipeProcessor)
    {
        this.recipeProcessor = recipeProcessor;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void terminateRecipe(long id)
    {
        try
        {
            recipeProcessor.terminateRecipe(id);
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

    public long getBuildingRecipe()
    {
        return recipeProcessor.getBuildingRecipe(); 
    }

    /**
     * Required resource
     *
     * @param resourceManager
     */
    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    /**
     * Required resource.
     *
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

}
