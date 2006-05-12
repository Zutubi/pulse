/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.pulse.util.logging.Logger;

/**
 *
 *
 */
public class DeleteTriggerAction extends ProjectActionSupport
{
    private static final Logger LOG = Logger.getLogger(DeleteTriggerAction.class);

    private long id;
    private Trigger trigger;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Trigger getTrigger()
    {
        return trigger;
    }

    public void validate()
    {
        trigger = getScheduler().getTrigger(id);
        if(trigger == null)
        {
            addActionError("Unknown trigger [" + id + "]");
        }

        projectId = trigger.getProject();
    }

    public String execute()
    {
        try
        {
            getScheduler().unschedule(trigger);
            return SUCCESS;
        }
        catch (SchedulingException e)
        {
            LOG.severe("Scheduling exception when deleting trigger '" + trigger + "'", e);
            return ERROR;
        }
    }
}
