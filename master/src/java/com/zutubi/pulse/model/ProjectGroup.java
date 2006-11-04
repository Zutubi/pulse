package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;

import java.util.LinkedList;
import java.util.List;

/**
 * A logical grouping of projects, used to organise projects in the UI.
 */
public class ProjectGroup extends Entity implements NamedEntity
{
    /**
     * A descriptive name for the group.
     */
    private String name;
    private List<Project> projects;

    public ProjectGroup()
    {
        projects = new LinkedList<Project>();
    }

    public ProjectGroup(String name)
    {
        this();
        this.name = name;
    }

    public ProjectGroup(String name, List<Project> projects)
    {
        this(name);
        this.projects = projects;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Project> getProjects()
    {
        return projects;
    }

    public void setProjects(List<Project> projects)
    {
        this.projects = projects;
    }

    public void add(Project project)
    {
        if(!projects.contains(project))
        {
            projects.add(project);
        }
    }

    public boolean remove(Project project)
    {
        return projects.remove(project);
    }
}
