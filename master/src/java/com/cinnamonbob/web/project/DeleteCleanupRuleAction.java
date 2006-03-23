package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Project;

/**
 */
public class DeleteCleanupRuleAction extends ProjectActionSupport
{
    private long projectId;
    private long id;

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String execute() throws Exception
    {
        Project project = getProjectManager().getProject(projectId);
        if(project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return ERROR;
        }

        project.removeCleanupRule(id);
        getProjectManager().save(project);
        
        return SUCCESS;
    }
}
