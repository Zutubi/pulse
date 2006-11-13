package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.PostBuildAction;
import com.zutubi.pulse.model.Project;

/**
 *
 */
public class EditPostBuildActionAction extends ProjectActionSupport
{
    private long id;
    private Project project;
    private PostBuildAction action;

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

    public PostBuildAction getAction()
    {
        return action;
    }

    public String doInput()
    {
        project = getProjectManager().getProject(projectId);
        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return ERROR;
        }

        action = project.getPostBuildAction(id);
        if (action == null)
        {
            addActionError("Unknown post build action [" + id + "]");
            return ERROR;
        }

        return action.getType();
    }
}
