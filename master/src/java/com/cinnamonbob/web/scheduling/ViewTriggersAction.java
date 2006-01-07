package com.cinnamonbob.web.scheduling;

import com.cinnamonbob.web.ActionSupport;
import com.cinnamonbob.scheduling.Scheduler;
import com.cinnamonbob.scheduling.Trigger;

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
