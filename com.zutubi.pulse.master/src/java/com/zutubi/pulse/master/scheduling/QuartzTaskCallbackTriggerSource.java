package com.zutubi.pulse.master.scheduling;

import com.zutubi.pulse.master.model.persistence.TriggerDao;

/**
 * 
 */
public class QuartzTaskCallbackTriggerSource
{
    private TriggerDao triggerDao;

    private Trigger trigger;

    public QuartzTaskCallbackTriggerSource(Trigger trigger)
    {
        this.trigger = trigger;
    }

    public Trigger getTrigger()
    {
        if (this.trigger.isPersistent())
        {
            return triggerDao.findById(this.trigger.getId());
        }
        else
        {
            return this.trigger;
        }
    }

    public void setTriggerDao(TriggerDao triggerDao)
    {
        this.triggerDao = triggerDao;
    }
}
