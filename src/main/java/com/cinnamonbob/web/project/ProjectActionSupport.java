package com.cinnamonbob.web.project;

import com.cinnamonbob.model.BuildManager;
import com.cinnamonbob.model.ProjectManager;
import com.cinnamonbob.web.ActionSupport;
import com.cinnamonbob.xwork.interceptor.Cancelable;

/**
 * 
 *
 */
public class ProjectActionSupport extends ActionSupport implements Cancelable
{
    private String cancel;
    private ProjectManager projectManager;
    private BuildManager buildManager;

    public boolean isCancelled()
    {
        return cancel != null;
    }

    public void setCancel(String name)
    {
        this.cancel = name;
    }

    public void setProjectManager(ProjectManager manager)
    {
        projectManager = manager;
    }

    public ProjectManager getProjectManager()
    {
        return projectManager;
    }
    
    public BuildManager getBuildManager()
    {
        return buildManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
