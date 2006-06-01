package com.zutubi.pulse.web.console;

import com.zutubi.pulse.web.ActionSupport;
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

    public void setQuartzScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

}
