package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.NamedEntityComparator;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectGroup;

import java.util.Collections;
import java.util.List;

/**
 * 
 *
 */
public class ViewProjectsAction extends ProjectActionSupport
{
    private List<ProjectGroup> groups;
    private List<Project> projects;

    public List<Project> getProjects()
    {
        return projects;
    }

    public List<ProjectGroup> getGroups()
    {
        return groups;
    }

    public BuildResult getLatestBuild(Project project)
    {
        List<BuildResult> build = getBuildManager().getLatestBuildResultsForProject(project, 1);
        if (build.size() == 0)
        {
            return null;
        }
        else
        {
            return build.get(0);
        }
    }

    public String execute()
    {
        groups = projectManager.getAllProjectGroups();
        Collections.sort(groups, new NamedEntityComparator());

        projects = getProjectManager().getAllProjects();
        for(ProjectGroup g: groups)
        {
            projects.removeAll(g.getProjects());
        }
        
        Collections.sort(projects, new NamedEntityComparator());
        return SUCCESS;
    }
}
