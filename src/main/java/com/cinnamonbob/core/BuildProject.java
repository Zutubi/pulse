package com.cinnamonbob.core;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.cinnamonbob.BobServer;

import java.util.Map;

/**
 * 
 *
 */
public class BuildProject implements Job
{
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
    {
        Map dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        Project project = (Project) dataMap.get("project");
        BobServer.build(project.getName());
    }
}
