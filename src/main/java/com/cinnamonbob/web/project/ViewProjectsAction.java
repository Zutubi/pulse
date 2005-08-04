package com.cinnamonbob.web.project;

import java.util.LinkedList;
import java.util.List;

import com.cinnamonbob.core2.BuildResult;
import com.cinnamonbob.model.Project;

/**
 * 
 *
 */
public class ViewProjectsAction extends ProjectActionSupport
{
    private List<Project> projects;
    private List<BuildResult> latestBuilds;

    public List<Project> getProjects()
    {
        return projects;
    }
    
    public List<BuildResult> getLatestBuilds()
    {
        return latestBuilds;
    }

    public void validate()
    {
        
    }
    
    public String execute()
    {
        projects = getProjectManager().getAllProjects();
        latestBuilds = new LinkedList<BuildResult>();
        
        for(Project p: projects)
        {
            List<BuildResult> build = getBuildManager().getLatestBuildResultsForProject(p.getName(), 1);
            if(build.size() == 0)
            {
                latestBuilds.add(null);
            }
            else
            {
                latestBuilds.add(build.get(0));
            }
        }
        return SUCCESS;
    }
}
