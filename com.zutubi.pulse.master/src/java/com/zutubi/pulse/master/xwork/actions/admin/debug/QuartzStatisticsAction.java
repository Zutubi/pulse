package com.zutubi.pulse.master.xwork.actions.admin.debug;

import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import org.quartz.Scheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Admin action that exposes the quartz scheduler, allowing us to
 * generate a debug page for Quartz.
 */
public class QuartzStatisticsAction extends ActionSupport
{
    private Scheduler scheduler;
    private ThreadPoolTaskExecutor taskExecutor;

    public ThreadPoolTaskExecutor getTaskExecutor()
    {
        return taskExecutor;
    }

    public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor)
    {
        this.taskExecutor = taskExecutor;
    }

    public Scheduler getScheduler()
    {
        return scheduler;
    }

    public void setQuartzScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

}
