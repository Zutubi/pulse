package com.cinnamonbob.scheduling.quartz;

import com.cinnamonbob.FatController;
import com.cinnamonbob.events.EventManager;
import com.cinnamonbob.events.build.BuildTimeoutEvent;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 */
public class TimeoutBuildJob implements Job
{
    public static final String PARAM_ID = "BUILD_ID";

    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        long buildId = (Long) context.getTrigger().getJobDataMap().get(PARAM_ID);
        EventManager eventManager = (EventManager) context.getJobDetail().getJobDataMap().get(FatController.PARAM_EVENT_MANAGER);
        eventManager.publish(new BuildTimeoutEvent(this, buildId));
    }
}
