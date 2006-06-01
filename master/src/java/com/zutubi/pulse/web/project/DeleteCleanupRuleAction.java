package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;

/**
 */
public class DeleteCleanupRuleAction extends ProjectActionSupport
{
    private long id;

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
