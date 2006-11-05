package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.ProjectGroup;
import com.zutubi.pulse.model.ProjectManager;

import java.util.List;

/**
 * Utilities for forms that deal with projects.
 */
public class ProjectGroupFormHelper extends NamedEntityFormHelper<ProjectGroup>
{
    private ProjectManager projectManager;

    public ProjectGroupFormHelper(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    protected ProjectGroup get(long id)
    {
        return projectManager.getProjectGroup(id);
    }

    protected List<ProjectGroup> getAll()
    {
        return projectManager.getAllProjectGroups();
    }
}
