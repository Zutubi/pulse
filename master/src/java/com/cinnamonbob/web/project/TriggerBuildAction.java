package com.cinnamonbob.web.project;

import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.events.build.BuildRequestEvent;
import com.cinnamonbob.model.BuildSpecification;
import com.cinnamonbob.model.Project;

import java.util.List;

public class TriggerBuildAction extends ProjectActionSupport
{
    private long id;
    private long projectId;
    private EventManager eventManager;

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

    public void validate()
    {
    }

    public String execute()
    {
        Project project = getProjectManager().getProject(projectId);

        if (project == null)
        {
            addActionError("Trigger build request for unknown project ID '" + projectId + "'");
            return ERROR;
        }

        List<BuildSpecification> specs = project.getBuildSpecifications();
        BuildSpecification spec = null;

        for (BuildSpecification s : specs)
        {
            if (s.getId() == id)
            {
                spec = s;
                break;
            }
        }

        if (spec == null)
        {
            addActionError("Request to build unknown build specification id '" + id + "' for project '" + project.getName() + "'");
            return ERROR;
        }

        BuildRequestEvent event = new BuildRequestEvent(this, project, spec.getName());
        eventManager.publish(event);

        try
        {
            // Pause for dramatic effect
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
        }

        return SUCCESS;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
