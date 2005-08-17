package com.cinnamonbob.web.project;

import java.util.List;

import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.Project;

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

    }

    public String execute()
    {
        project = getProjectManager().getProject(id);
        history = getBuildManager().getLatestBuildResultsForProject(project.getName(), 11);
        if(history.size() > 0)
        {
            currentBuild = history.remove(0);
        }
        
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
}
