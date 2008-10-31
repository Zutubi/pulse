package com.zutubi.pulse.slave.command;

import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.slave.SlaveRecipeProcessor;

/**
 */
public class RecipeCommand implements Runnable
{
    /**
     * The URL of the master that made the request.
     */
    private String master;
    private long handle;
    private RecipeRequest request;
    private SlaveRecipeProcessor recipeProcessor;

    public RecipeCommand(String master, long handle, RecipeRequest request)
    {
        this.master = master;
        this.handle = handle;
        this.request = request;
    }

    public void run()
    {
        recipeProcessor.processRecipe(master, handle, request);
    }

    public void setSlaveRecipeProcessor(SlaveRecipeProcessor recipeProcessor)
    {
        this.recipeProcessor = recipeProcessor;
    }

    public String getMaster()
    {
        return master;
    }

    public RecipeRequest getRequest()
    {
        return request;
    }
}
