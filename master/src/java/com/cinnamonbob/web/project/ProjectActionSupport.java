package com.cinnamonbob.web.project;

import com.cinnamonbob.model.BuildManager;
import com.cinnamonbob.model.ProjectManager;
import com.cinnamonbob.model.ScmManager;
import com.cinnamonbob.web.ActionSupport;
import com.cinnamonbob.xwork.interceptor.Cancelable;
import com.cinnamonbob.scheduling.DefaultScheduler;

/**
 * 
 *
 */
public class ProjectActionSupport extends ActionSupport implements Cancelable
{
    private String cancel;
    private ProjectManager projectManager;
    private BuildManager buildManager;
    private ScmManager scmManager;
//    private ScheduleManager scheduleManager;
    private DefaultScheduler scheduler;

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

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    public ScmManager getScmManager()
    {
        return scmManager;
    }
    
    public DefaultScheduler getScheduler()
    {
        return this.scheduler;
    }

    public void setScheduler(DefaultScheduler scheduler)
    {
        this.scheduler = scheduler;
    }
}
