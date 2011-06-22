package com.zutubi.pulse.master.xwork.actions.server;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.build.queue.RecipeQueue;
import com.zutubi.pulse.master.build.queue.SchedulingController;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.ajax.SimpleResult;
import com.zutubi.util.StringUtils;

/**
 * Toggles the state of the build or recipe queue.
 */
public class ToggleQueueAction extends ActionSupport
{
    private static final Messages I18N = Messages.getInstance(ToggleQueueAction.class);

    private static final String QUEUE_BUILD = "build";
    private static final String QUEUE_STAGE = "stage";
    
    private String queueName;
    private RecipeQueue recipeQueue;
    private SchedulingController schedulingController;
    private SimpleResult result;

    public void setQueueName(String queueName)
    {
        this.queueName = queueName;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    public String execute() throws Exception
    {
        if (accessManager.hasPermission(ServerPermission.ADMINISTER.name(), null))
        {
            if (QUEUE_STAGE.equals(queueName))
            {
                if(recipeQueue.isRunning())
                {
                    recipeQueue.stop();
                    result = new SimpleResult(true, I18N.format("queue.paused", StringUtils.capitalise(queueName)));
                }
                else
                {
                    recipeQueue.start();
                    result = new SimpleResult(true, I18N.format("queue.resumed", StringUtils.capitalise(queueName)));
                }
            }
            else if (QUEUE_BUILD.equals(queueName))
            {
                if (schedulingController.isRunning())
                {
                    schedulingController.pause();
                    result = new SimpleResult(true, I18N.format("queue.paused", StringUtils.capitalise(queueName)));
                }
                else
                {
                    schedulingController.resume();
                    result = new SimpleResult(true, I18N.format("queue.resumed", StringUtils.capitalise(queueName)));
                }
            }
            // pause for effect.
            Thread.sleep(500);
        }
        else
        {
            result = new SimpleResult(false, I18N.format("toggle.not.permitted", queueName));
        }
        
        return SUCCESS;
    }

    public void setRecipeQueue(RecipeQueue recipeQueue)
    {
        this.recipeQueue = recipeQueue;
    }

    public void setSchedulingController(SchedulingController schedulingController)
    {
        this.schedulingController = schedulingController;
    }
}
