/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.ScmManager;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.web.ActionSupport;

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
