package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;

import java.util.List;

/**
 * Utilities for forms that deal with projects.
 */
public class ProjectFormHelper extends NamedEntityFormHelper<Project>
{
    private ProjectManager projectManager;

    public ProjectFormHelper(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    protected Project get(long id)
    {
        return projectManager.getProject(id);
    }

    protected List<Project> getAll()
    {
        return projectManager.getAllProjects();
    }
}
