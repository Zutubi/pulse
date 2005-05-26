package com.cinnamonbob.core2;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import com.cinnamonbob.bootstrap.quartz.QuartzManager;

/**
 * 
 *
 */
public class ScheduleManager
{

    public void clearTriggers(String projectName)
    {
        try
        {
            Scheduler scheduler = QuartzManager.getScheduler();
            String[] jobNames = scheduler.getJobNames("");
            for (String name : jobNames)
            {
                // delete the job and you will also delete any 
                // associated triggers.
                scheduler.deleteJob(name, "");
            }
        } 
        catch (SchedulerException e)
        {
            e.printStackTrace();
        }        
    }
}
