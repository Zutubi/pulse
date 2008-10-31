package com.zutubi.pulse.master.xwork.actions.console;

import com.zutubi.pulse.master.xwork.actions.ActionSupport;
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
