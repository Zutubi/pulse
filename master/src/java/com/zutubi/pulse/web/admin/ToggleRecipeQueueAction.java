/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.RecipeQueue;
import com.zutubi.pulse.web.ActionSupport;

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

        Thread.sleep(500);
        return SUCCESS;
    }

    public void setRecipeQueue(RecipeQueue recipeQueue)
    {
        this.recipeQueue = recipeQueue;
    }
}
