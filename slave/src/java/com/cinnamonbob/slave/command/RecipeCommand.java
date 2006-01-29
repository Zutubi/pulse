package com.cinnamonbob.slave.command;

import com.cinnamonbob.RecipeRequest;
import com.cinnamonbob.slave.SlaveRecipeProcessor;

/**
 */
public class RecipeCommand implements Runnable
{
    /**
     * The URL of the master that made the request.
     */
    private String master;
    private RecipeRequest request;
    private SlaveRecipeProcessor recipeProcessor;

    public RecipeCommand(String master, RecipeRequest request)
    {
        this.master = master;
        this.request = request;
    }

    public void run()
    {
        recipeProcessor.processRecipe(master, request);
    }

    public void setSlaveRecipeProcessor(SlaveRecipeProcessor recipeProcessor)
    {
        this.recipeProcessor = recipeProcessor;
    }
}
