package com.cinnamonbob.web.admin;

import com.cinnamonbob.RecipeQueue;
import com.cinnamonbob.web.ActionSupport;

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

        return SUCCESS;
    }

    public void setRecipeQueue(RecipeQueue recipeQueue)
    {
        this.recipeQueue = recipeQueue;
    }
}
