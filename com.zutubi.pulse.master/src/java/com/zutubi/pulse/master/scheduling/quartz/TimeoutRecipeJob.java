package com.zutubi.pulse.master.scheduling.quartz;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.FatController;
import com.zutubi.pulse.master.events.build.RecipeTimeoutEvent;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 */
public class TimeoutRecipeJob implements Job
{
    public static final String PARAM_BUILD_ID = "BUILD_ID";
    public static final String PARAM_RECIPE_ID = "RECIPE_ID";

    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        long buildId = (Long) context.getTrigger().getJobDataMap().get(PARAM_BUILD_ID);
        long recipeId = (Long) context.getTrigger().getJobDataMap().get(PARAM_RECIPE_ID);
        EventManager eventManager = (EventManager) context.getJobDetail().getJobDataMap().get(FatController.PARAM_EVENT_MANAGER);
        eventManager.publish(new RecipeTimeoutEvent(this, buildId, recipeId));
    }
}
