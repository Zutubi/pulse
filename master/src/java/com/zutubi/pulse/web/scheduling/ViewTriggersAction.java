package com.zutubi.pulse.web.scheduling;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.Trigger;

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
