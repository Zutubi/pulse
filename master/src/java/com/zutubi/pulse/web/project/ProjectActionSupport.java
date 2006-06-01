package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.ScmManager;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.web.ActionSupport;
import com.opensymphony.util.TextUtils;

import java.util.LinkedList;
import java.util.List;

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

    private static final long NONE_SPECIFIED = -1;

    protected long projectId = NONE_SPECIFIED;

    protected String projectName = null;

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

    public Feature.Level getErrorLevel()
    {
        return Feature.Level.ERROR;
    }

    public Feature.Level getWarningLevel()
    {
        return Feature.Level.WARNING;
    }

    public List<Feature.Level> getFeatureLevels()
    {
        List<Feature.Level> levels = new LinkedList<Feature.Level>();
        levels.add(Feature.Level.ERROR);
        levels.add(Feature.Level.WARNING);
        levels.add(Feature.Level.INFO);
        return levels;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public Project getProject()
    {
        if (projectId != NONE_SPECIFIED)
        {
            return getProject(projectId);
        }
        else if (TextUtils.stringSet(projectName))
        {
            return getProject(projectName);
        }
        return null;
    }

    protected Project getProject(long id)
    {
        return projectManager.getProject(id);
    }

    protected Project getProject(String projectName)
    {
        return getProjectManager().getProject(projectName);
    }

    public void addUnknownProjectActionError()
    {
        if (projectId != NONE_SPECIFIED)
        {
            addActionError("Unknown project [" + projectId + "]");
        }
        else if (TextUtils.stringSet(projectName))
        {
            addActionError("Unknown project [" + projectName + "]");
        }
        else
        {
            addActionError("Require either a project name or id.");
        }
    }

    public void addUnknownProjectFieldError()
    {
        if (projectId != NONE_SPECIFIED)
        {
            addFieldError("projectId", "Unknown project [" + projectId + "]");
        }
        else if (TextUtils.stringSet(projectName))
        {
            addFieldError("projectName", "Unknown project [" + projectName + "]");
        }
        else
        {
            addActionError("Require either a project name or id.");
        }
    }

    public Project lookupProject(long id)
    {
        Project p = projectManager.getProject(id);
        if(p == null)
        {
            addActionError("Unknown project [" + id + "]");
        }

        return p;
    }
}
