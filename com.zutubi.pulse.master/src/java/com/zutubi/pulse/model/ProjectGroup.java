package com.zutubi.pulse.model;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * A logical grouping of projects, used to organise projects in the UI.
 * Assembled by observing project labels.
 */
public class ProjectGroup
{
    /**
     * A descriptive name for the group.
     */
    private String name;
    /**
     * Projects in the group, sorted by name.  Projects are looked up and
     * populated on demand.
     */
    private Set<Project> projects;

    public ProjectGroup(String name)
    {
        this.name = name;
        projects = new TreeSet<Project>(new NamedEntityComparator());
    }

    public String getName()
    {
        return name;
    }

    public Collection<Project> getProjects()
    {
        return projects;
    }

    void add(Project project)
    {
        projects.add(project);
    }

    void addAll(Collection<Project> projects)
    {
        for(Project p: projects)
        {
            add(p);
        }
    }

    boolean remove(Project project)
    {
        return projects.remove(project);
    }
}
