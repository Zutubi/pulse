package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.model.NamedEntityComparator;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.web.ActionSupport;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class CommitMessageTransformerActionSupport extends ActionSupport
{
    private long id;
    private ProjectManager projectManager;
    private Map<Long, String> allProjects;
    private List<Long> selectedProjects;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Map<Long, String> getAllProjects()
    {
        if(allProjects == null)
        {
            List<Project> projects = projectManager.getAllProjects();
            Collections.sort(projects, new NamedEntityComparator());
            allProjects = new LinkedHashMap<Long, String>();
            for(Project p: projects)
            {
                allProjects.put(p.getId(), p.getName());
            }
        }
        return allProjects;
    }

    public List<Long> getSelectedProjects()
    {
        return selectedProjects;
    }

    public void setSelectedProjects(List<Long> selectedProjects)
    {
        this.selectedProjects = selectedProjects;
    }

    public ProjectManager getProjectManager()
    {
        return projectManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
