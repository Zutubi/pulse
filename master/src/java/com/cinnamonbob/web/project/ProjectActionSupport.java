package com.cinnamonbob.web.project;

import com.cinnamonbob.model.BuildManager;
import com.cinnamonbob.model.ProjectManager;
import com.cinnamonbob.model.ScmManager;
import com.cinnamonbob.scheduling.Scheduler;
import com.cinnamonbob.web.ActionSupport;

/**
 * 
 *
 */
public class ProjectActionSupport extends ActionSupport
{
    private ProjectManager projectManager;
    private BuildManager buildManager;
    private ScmManager scmManager;
    private Scheduler scheduler;

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

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    public ScmManager getScmManager()
    {
        return scmManager;
    }

    public Scheduler getScheduler()
    {
        return this.scheduler;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
}
