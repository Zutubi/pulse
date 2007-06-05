package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.web.LookupErrorException;

/**
 */
public class ProjectActionBase extends ActionSupport
{
    private String projectName;
    private Project project;
    private ProjectConfiguration projectConfig;
    private long projectId;

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public Project getProject()
    {
        if(project == null)
        {
            ProjectConfiguration config = getProjectConfig();
            project = projectManager.getProject(config.getProjectId());
            if(project == null)
            {
                throw new LookupErrorException("Unknown project '" + projectName + "'");
            }
            projectId = project.getId();
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
            projectId = projectConfig.getProjectId();
        }

        return projectConfig;
    }
}
