package com.cinnamonbob.bootstrap.quartz;

import com.cinnamonbob.bootstrap.StartupManager;
import org.quartz.Scheduler;

/**
 * 
 *
 */
public class QuartzManager
{

    public static Scheduler getScheduler()
    {
        return (Scheduler) StartupManager.getInstance().getApplicationContext().getBean("quartzScheduler");
    }
}
