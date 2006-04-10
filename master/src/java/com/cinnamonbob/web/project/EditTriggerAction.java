package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.scheduling.Trigger;

/**
 *
 */
public class EditTriggerAction extends ProjectActionSupport
{
    private long id;
    private long projectId;
    private Project project;
    private Trigger trigger;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public Project getProject()
    {
        return project;
    }

    public Trigger getTrigger()
    {
        return trigger;
    }

    public String doInput()
    {
        project = getProjectManager().getProject(projectId);
        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return ERROR;
        }

        trigger = getScheduler().getTrigger(id);
        if (trigger == null)
        {
            addActionError("Unknown trigger [" + id + "]");
            return ERROR;
        }

        if (trigger.canEdit())
        {
            return trigger.getType();
        }

        addActionError("Unable to edit trigger of type '" + trigger.getType() + "'");
        return ERROR;
    }
}
