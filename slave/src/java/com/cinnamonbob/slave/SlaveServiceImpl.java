package com.cinnamonbob.slave;

import com.cinnamonbob.RecipeRequest;
import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.services.SlaveService;
import com.cinnamonbob.slave.command.CleanupRecipeCommand;
import com.cinnamonbob.slave.command.RecipeCommand;

/**
 */
public class SlaveServiceImpl implements SlaveService
{
    private SlaveThreadPool threadPool;
    private SlaveRecipeProcessor slaveRecipeProcessor;

    public void ping()
    {
        // Nothing to actually do!
    }

    public void build(String master, RecipeRequest request)
    {
        RecipeCommand command = new RecipeCommand(master, request);
        ComponentContext.autowire(command);
        ErrorHandlingRunnable runnable = new ErrorHandlingRunnable(master, request.getId(), command);
        ComponentContext.autowire(runnable);

        threadPool.executeCommand(runnable);
    }

    public void cleanupRecipe(long recipeId)
    {
        CleanupRecipeCommand command = new CleanupRecipeCommand(recipeId);
        // TODO more dodgy wiring :-/
        ComponentContext.autowire(command);
        threadPool.executeCommand(command);
    }

    public void terminateRecipe(long recipeId)
    {
        // Do this request synchronously
        slaveRecipeProcessor.terminateRecipe(recipeId);
    }

    public void setThreadPool(SlaveThreadPool threadPool)
    {
        this.threadPool = threadPool;
    }

    public void setSlaveRecipeProcessor(SlaveRecipeProcessor slaveRecipeProcessor)
    {
        this.slaveRecipeProcessor = slaveRecipeProcessor;
    }
}
