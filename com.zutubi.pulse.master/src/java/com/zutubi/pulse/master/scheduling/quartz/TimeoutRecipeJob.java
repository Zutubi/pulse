package com.zutubi.pulse.master.scheduling.quartz;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.FatController;
import com.zutubi.pulse.master.events.build.RecipeTimeoutEvent;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * This job is used to trigger a recipe timeout event.
 *
 * It is scheduled at the start of a build and will generate a recipe timeout event is it is not unscheduled
 * before its time is up.
 */
public class TimeoutRecipeJob implements Job
{
    /**
     * The job data map parameter used to access the build id this job relates.
     */
    public static final String PARAM_BUILD_ID = "BUILD_ID";
    /**
     * The job data map parameter used to access the recipe id this job relates.
     */
    public static final String PARAM_RECIPE_ID = "RECIPE_ID";

    private EventManager eventManager;

    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        long buildId = (Long) context.getTrigger().getJobDataMap().get(PARAM_BUILD_ID);
        long recipeId = (Long) context.getTrigger().getJobDataMap().get(PARAM_RECIPE_ID);

        eventManager.publish(new RecipeTimeoutEvent(this, buildId, recipeId));
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
