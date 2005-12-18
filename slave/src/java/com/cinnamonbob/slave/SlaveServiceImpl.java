package com.cinnamonbob.slave;

import com.cinnamonbob.RecipeRequest;
import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.services.SlaveService;

/**
 */
public class SlaveServiceImpl implements SlaveService
{
    private SlaveThreadPool threadPool;

    public void ping()
    {
        // Nothing to actually do!
    }

    public void build(String master, RecipeRequest request)
    {
        // TODO fix this wiring
        ComponentContext.autowire(this);
        RecipeCommand command = new RecipeCommand(master, request);
        ComponentContext.autowire(command);
        threadPool.executeCommand(command);
    }

    public void cleanupResults(long recipeId)
    {
        CleanupResultsCommand command = new CleanupResultsCommand(recipeId);
        // TODO more dodgy wiring :-/
        ComponentContext.autowire(command);
        threadPool.executeCommand(command);
    }

    public void setThreadPool(SlaveThreadPool threadPool)
    {
        this.threadPool = threadPool;
    }
}
