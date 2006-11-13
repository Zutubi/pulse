package com.zutubi.pulse.slave.command;

import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.slave.SlaveRecipeProcessor;
import com.zutubi.pulse.BuildContext;

/**
 */
public class RecipeCommand implements Runnable
{
    /**
     * The URL of the master that made the request.
     */
    private String master;
    private long slaveId;
    private RecipeRequest request;
    private SlaveRecipeProcessor recipeProcessor;
    private BuildContext context;

    public RecipeCommand(String master, long slaveId, RecipeRequest request, BuildContext context)
    {
        this.master = master;
        this.slaveId = slaveId;
        this.request = request;
        this.context = context;
    }

    public void run()
    {
        recipeProcessor.processRecipe(master, slaveId, request, context);
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
