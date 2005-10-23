package com.cinnamonbob.scheduling;

import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Simple request to build a job.
 */
public class CallbackJob implements Job
{
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
    {
        Map dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        Trigger callback = (Trigger) dataMap.get("self");
        callback.trigger();
    }
}
