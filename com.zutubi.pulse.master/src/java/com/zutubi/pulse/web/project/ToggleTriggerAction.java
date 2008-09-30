package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.util.logging.Logger;

/**
 *
 */
public class ToggleTriggerAction extends ProjectActionSupport
{
    private static final Logger LOG = Logger.getLogger(ToggleTriggerAction.class);

    private long id;
    private boolean pause;

    public void setId(long id)
    {
        this.id = id;
    }

    public void setPause(boolean pause)
    {
        this.pause = pause;
    }

    public String execute()
    {
        Project project = getProjectManager().getProject(projectId, false);
        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return ERROR;
        }

        getProjectManager().checkWrite(project);

        Trigger trigger = getScheduler().getTrigger(id);
        if (trigger == null)
        {
            addActionError("Unknown trigger [" + id + "]");
            return ERROR;
        }

        try
        {
            if(pause)
            {
                if(!trigger.isPaused())
                {
                    getScheduler().pause(trigger);
                }
            }
            else
            {
                if(trigger.isPaused())
                {
                    getScheduler().resume(trigger);
                }
            }
        }
        catch (SchedulingException e)
        {
            LOG.severe(e);
            addActionError(e.getMessage());
            return ERROR;
        }

        return SUCCESS;
    }
}
