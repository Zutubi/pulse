package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.NamedEntityComparator;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;

import java.util.*;

/**
 * Utilities for forms that deal with projects.
 */
public class ProjectFormHelper
{
    private ProjectManager projectManager;

    public ProjectFormHelper(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    Map<Long, String> getAllProjects()
    {
        List<Project> all = projectManager.getAllProjects();
        Collections.sort(all, new NamedEntityComparator());

        Map<Long, String> result = new LinkedHashMap<Long, String>();
        for(Project p: all)
        {
            result.put(p.getId(), p.getName());
        }

        return result;
    }

    public void convertFromIds(List<Long> projects, List<Project> destination)
    {
        destination.clear();
        for(Long id: projects)
        {
            Project p = projectManager.getProject(id);
            if(p != null)
            {
                destination.add(p);
            }
        }
    }

    public List<Long> convertToIds(List<Project> projects)
    {
        List<Long> result = new ArrayList<Long>(projects.size());
        for(Project p: projects)
        {
            result.add(p.getId());
        }

        return result;
    }
}
