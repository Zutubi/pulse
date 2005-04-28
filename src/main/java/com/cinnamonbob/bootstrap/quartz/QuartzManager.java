package com.cinnamonbob.bootstrap.quartz;

import com.cinnamonbob.bootstrap.StartupManager;
import org.quartz.Scheduler;

/**
 * 
 *
 */
public class QuartzManager
{

    private static final String BEAN_NAME = "quartzScheduler";

    /**
     * Convenience method for accessing the systems Quartz Scheduler resource.
     * @return
     */
    public static Scheduler getScheduler()
    {
        return (Scheduler) StartupManager.getBean(BEAN_NAME);
    }
}
