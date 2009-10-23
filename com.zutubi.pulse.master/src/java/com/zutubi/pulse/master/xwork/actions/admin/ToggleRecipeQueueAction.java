package com.zutubi.pulse.master.xwork.actions.admin;

import com.zutubi.pulse.master.RecipeQueue;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 * Toggles the state of the recipe queue.
 */
public class ToggleRecipeQueueAction extends ActionSupport
{
    private RecipeQueue recipeQueue;

    public String execute() throws Exception
    {
        if(recipeQueue.isRunning())
        {
            recipeQueue.stop();
        }
        else
        {
            recipeQueue.start();
        }

        // pause for effect.
        Thread.sleep(500);
        
        return SUCCESS;
    }

    public void setRecipeQueue(RecipeQueue recipeQueue)
    {
        this.recipeQueue = recipeQueue;
    }
}
