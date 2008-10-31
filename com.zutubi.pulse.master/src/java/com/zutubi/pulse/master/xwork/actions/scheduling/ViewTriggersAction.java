package com.zutubi.pulse.master.xwork.actions.scheduling;

import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

import java.util.List;

/**
 * <class-comment/>
 */
public class ViewTriggersAction extends ActionSupport
{
    private Scheduler scheduler;

    public List<Trigger> triggers;

    public List<Trigger> getTriggers()
    {
        return triggers;
    }

    public String execute() throws Exception
    {
        triggers = scheduler.getTriggers();
        return SUCCESS;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
}
