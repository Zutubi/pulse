package com.zutubi.pulse.master;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.RecipeProcessor;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.servercore.services.SlaveStatus;
import com.zutubi.util.logging.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 */
public class MasterRecipeProcessor implements Stoppable
{
    private static final Logger LOG = Logger.getLogger(MasterRecipeProcessor.class);

    private AtomicLong buildingRecipe = new AtomicLong(SlaveStatus.NO_RECIPE);

    private ExecutorService executor;
    private RecipeProcessor recipeProcessor;
    private MasterConfigurationManager configurationManager;
    private EventManager eventManager;

    public MasterRecipeProcessor()
    {
        executor = Executors.newSingleThreadExecutor();
    }

    public void processRecipe(final RecipeRequest request, final ResourceRepository agentRepository)
    {
        executor.submit(new Runnable()
        {
            public void run()
            {
                try
                {
                    buildingRecipe.set(request.getId());
                    MasterRecipeRunner recipeRunner = new MasterRecipeRunner(request, recipeProcessor, eventManager, configurationManager, agentRepository);
                    recipeRunner.run();
                }
                finally
                {
                    buildingRecipe.set(SlaveStatus.NO_RECIPE);
                }
            }
        });
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
        return buildingRecipe.get();
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
