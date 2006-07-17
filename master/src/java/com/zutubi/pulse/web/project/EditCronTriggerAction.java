package com.zutubi.pulse.web.project;

import com.zutubi.pulse.scheduling.CronTrigger;
import com.zutubi.pulse.scheduling.Trigger;

/**
 * 
 */
public class EditCronTriggerAction extends AbstractEditTriggerAction
{
    // Create it so webwork doesn't try to
    private CronTrigger trigger = new CronTrigger();

    public void prepare() throws Exception
    {
        Trigger t = getScheduler().getTrigger(getId());
        if (t == null)
        {
            addActionError("Unknown trigger [" + getId() + "]");
            return;
        }

        if (!(t instanceof CronTrigger))
        {
            addActionError("Invalid trigger type '" + t.getType() + "'");
            return;
        }

        trigger = (CronTrigger) t;

        // Must set trigger before calling super
        super.prepare();
    }

    public CronTrigger getTrigger()
    {
        return trigger;
    }
}
