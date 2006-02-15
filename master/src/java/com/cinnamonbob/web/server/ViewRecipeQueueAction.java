package com.cinnamonbob.web.server;

import com.cinnamonbob.FatController;
import com.cinnamonbob.RecipeDispatchRequest;
import com.cinnamonbob.RecipeQueue;
import com.cinnamonbob.web.ActionSupport;

import java.util.List;

/**
 */
public class ViewRecipeQueueAction extends ActionSupport
{
    private FatController fatController;
    private RecipeQueue queue;
    private List<RecipeDispatchRequest> snapshot;

    public List<RecipeDispatchRequest> getSnapshot()
    {
        return snapshot;
    }

    public String execute() throws Exception
    {
        snapshot = queue.takeSnapshot();
        return SUCCESS;
    }

    public void setFatController(FatController fatController)
    {
        this.fatController = fatController;
    }

    public void setRecipeQueue(RecipeQueue queue)
    {
        this.queue = queue;
    }

}
