package com.cinnamonbob.web.project;

import com.cinnamonbob.core.model.Feature;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.scheduling.Trigger;

import java.util.List;

/**
 * 
 *
 */
public class ManageProjectAction extends ProjectActionSupport
{
    private long id;
    private Project project;
    private BuildResult currentBuild;
    private List<BuildResult> history;
    private List<Trigger> triggers;

    public long getId()
    {
        return id;
    }

    public Project getProject()
    {
        return project;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public void validate()
    {
        project = getProjectManager().getProject(id);
        if (project == null)
        {
            addActionError("Unknown project '" + id + "'");
        }
    }

    public String execute()
    {
        history = getBuildManager().getLatestBuildResultsForProject(project, 11);
        if (history.size() > 0)
        {
            currentBuild = history.remove(0);
        }

        triggers = getScheduler().getTriggers(project.getId());
        return SUCCESS;
    }

    public BuildResult getCurrentBuild()
    {
        return currentBuild;
    }

    public List<BuildResult> getHistory()
    {
        return history;
    }

    public List<Trigger> getTriggers()
    {
        return triggers;
    }

    public Feature.Level getErrorLevel()
    {
        return Feature.Level.ERROR;
    }

    public Feature.Level getWarningLevel()
    {
        return Feature.Level.WARNING;
    }
}
