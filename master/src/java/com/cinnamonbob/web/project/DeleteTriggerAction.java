package com.cinnamonbob.web.project;

import com.cinnamonbob.scheduling.Trigger;
import com.cinnamonbob.scheduling.SchedulingException;
import com.cinnamonbob.util.logging.Logger;

/**
 *
 *
 */
public class DeleteTriggerAction extends ProjectActionSupport
{
    private static final Logger LOG = Logger.getLogger(DeleteTriggerAction.class.getName());

    private long id;
    private Trigger trigger;
    private long projectId;

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

    public long getProjectId()
    {
        return projectId;
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
