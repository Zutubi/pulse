/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.ProjectNameComparator;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
        Collections.sort(projects, new ProjectNameComparator());

        latestBuilds = new LinkedList<BuildResult>();

        for (Project p : projects)
        {
            List<BuildResult> build = getBuildManager().getLatestBuildResultsForProject(p, 1);
            if (build.size() == 0)
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
