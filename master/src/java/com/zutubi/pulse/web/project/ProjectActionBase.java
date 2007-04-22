package com.zutubi.pulse.web.project;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.web.LookupErrorException;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;

/**
 */
public class ProjectActionBase extends ActionSupport
{
    private String projectName;
    private Project project;
    private ProjectConfiguration projectConfig;

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public Project getProject()
    {
        if(project == null)
        {
            project = projectManager.getProject(projectName);
            if(project == null)
            {
                throw new LookupErrorException("Unknown project '" + projectName + "'");
            }
        }

        return project;
    }

    public ProjectConfiguration getProjectConfig()
    {
        if(projectConfig == null)
        {
            projectConfig = projectManager.getProjectConfig(projectName);
            if(projectConfig == null)
            {
                throw new LookupErrorException("Unknown project '" + projectName + "'");
            }
        }

        return projectConfig;
    }
}
