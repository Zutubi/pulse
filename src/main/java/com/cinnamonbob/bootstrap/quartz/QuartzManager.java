package com.cinnamonbob.bootstrap.quartz;

import com.cinnamonbob.bootstrap.StartupManager;
import com.cinnamonbob.bootstrap.ComponentContext;
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
        return (Scheduler) ComponentContext.getBean(BEAN_NAME);
    }
}
