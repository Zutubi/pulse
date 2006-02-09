package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.scheduling.Trigger;

import java.util.List;

/**
 */
public class ConfigureProjectAction extends ProjectActionSupport
{
    private long id;
    private Project project;
    private List<Trigger> triggers;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Project getProject()
    {
        return project;
    }

    public List<Trigger> getTriggers()
    {
        return triggers;
    }

    public void validate()
    {
        project = getProjectManager().getProject(id);
        if (project == null)
        {
            addActionError("Unknown project '" + id + "'");
        }
    }

    public String execute()
    {
        triggers = getScheduler().getTriggers(id);
        return SUCCESS;
    }
}
