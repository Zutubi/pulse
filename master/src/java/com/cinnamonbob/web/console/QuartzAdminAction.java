package com.cinnamonbob.web.console;

import com.cinnamonbob.web.ActionSupport;
import org.quartz.Scheduler;

/**
 * <class-comment/>
 */
public class QuartzAdminAction extends ActionSupport
{
    private Scheduler scheduler;

    public Scheduler getScheduler()
    {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

}
