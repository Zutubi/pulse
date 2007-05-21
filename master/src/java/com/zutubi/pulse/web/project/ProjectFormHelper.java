package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;

import java.util.List;
import java.util.LinkedList;

/**
 * Utilities for forms that deal with projects.
 */
public class ProjectFormHelper extends NamedEntityFormHelper<ProjectConfiguration>
{
    private ProjectManager projectManager;

    public ProjectFormHelper(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    protected ProjectConfiguration get(long id)
    {
        return projectManager.getProjectConfig(id);
    }

    protected List<ProjectConfiguration> getAll()
    {
        return new LinkedList<ProjectConfiguration>(projectManager.getAllProjectConfigs());
    }
}
