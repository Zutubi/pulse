package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.*;
import org.hibernate.SessionFactory;

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
    private BuildColumns buildColumns;
    private SessionFactory sessionFactory;

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

    public BuildColumns getColumns()
    {
        return buildColumns;
    }

    public String execute()
    {
        groups = projectManager.getAllProjectGroupsCached();
        Collections.sort(groups, new NamedEntityComparator());

        projects = getProjectManager().getAllProjectsCached();
        for(ProjectGroup g: groups)
        {
            projects.removeAll(g.getProjects());
        }
        
        Collections.sort(projects, new NamedEntityComparator());

        User user = getLoggedInUser();
        buildColumns = new BuildColumns(user == null ? User.getDefaultAllProjectsColumns() : user.getAllProjectsColumns(), projectManager);

        return SUCCESS;
    }

    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }
}
