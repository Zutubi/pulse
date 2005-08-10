package com.cinnamonbob.web.project;

import java.util.List;

import com.cinnamonbob.core.BuildResult;
import com.cinnamonbob.model.Project;

/**
 * 
 *
 */
public class ManageProjectAction extends ProjectActionSupport
{
    private long id;
    private Project project;
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
        history = getBuildManager().getLatestBuildResultsForProject(project.getName(), 10);
        return SUCCESS;
    }
    
    public List<BuildResult> getHistory()
    {
        return history;
    }
}
