/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;

/**
 */
public class DeleteProjectAdminAction extends ProjectActionSupport
{
    private String login;

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public String execute() throws Exception
    {
        Project project = getProjectManager().getProject(projectId);
        if(project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return ERROR;
        }

        project.removeAdmin(login);
        getProjectManager().save(project);
        
        return SUCCESS;
    }
}
