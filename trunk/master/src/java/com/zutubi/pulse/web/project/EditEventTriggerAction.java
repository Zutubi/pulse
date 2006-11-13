package com.zutubi.pulse.web.project;

import com.zutubi.pulse.scheduling.EventTrigger;
import com.zutubi.pulse.scheduling.Trigger;

/**
 */
public class EditEventTriggerAction extends AbstractEditTriggerAction
{
    // Create a dummy one to appease webwork
    private EventTrigger trigger = new EventTrigger();

    public void prepare() throws Exception
    {
        Trigger t = getScheduler().getTrigger(getId());
        if (t == null)
        {
            addActionError("Unknown trigger [" + getId() + "]");
            return;
        }

        if (!(t instanceof EventTrigger))
        {
            addActionError("Invalid trigger type '" + t.getType() + "'");
            return;
        }

        trigger = (EventTrigger) t;

        // Must set trigger before calling super
        super.prepare();
    }

    public EventTrigger getTrigger()
    {
        return trigger;
    }
}
