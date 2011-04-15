package com.zutubi.pulse.master.xwork.actions.server;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.build.queue.RecipeQueue;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.ajax.SimpleResult;

/**
 * Toggles the state of the recipe queue.
 */
public class ToggleRecipeQueueAction extends ActionSupport
{
    private static final Messages I18N = Messages.getInstance(ToggleRecipeQueueAction.class);
    
    private RecipeQueue recipeQueue;
    private SimpleResult result;

    public SimpleResult getResult()
    {
        return result;
    }

    public String execute() throws Exception
    {
        if (accessManager.hasPermission(ServerPermission.ADMINISTER.name(), null))
        {
            if(recipeQueue.isRunning())
            {
                recipeQueue.stop();
                result = new SimpleResult(true, I18N.format("queue.paused"));
            }
            else
            {
                recipeQueue.start();
                result = new SimpleResult(true, I18N.format("queue.resumed"));
            }
    
            // pause for effect.
            Thread.sleep(500);
        }
        else
        {
            result = new SimpleResult(false, I18N.format("toggle.not.permitted"));
        }
        
        return SUCCESS;
    }

    public void setRecipeQueue(RecipeQueue recipeQueue)
    {
        this.recipeQueue = recipeQueue;
    }
}
