package com.cinnamonbob.web.project;

import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.Project;

import java.util.List;

/**
 */
public class HistoryAction extends ProjectActionSupport
{
    private long id;
    private Project project;
    private List<BuildResult> history;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Project getProject()
    {
        return project;
    }

    public List<BuildResult> getHistory()
    {
        return history;
    }

    public void validate()
    {
        project = getProjectManager().getProject(id);
        if (project == null)
        {
            addActionError("Unknown project [" + id + "]");
        }
    }

    public String execute()
    {
        if (project != null)
        {
            history = getBuildManager().getLatestBuildResultsForProject(project, 10);
        }

        return SUCCESS;
    }
}
