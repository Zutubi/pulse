package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.NamedEntityComparator;

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

    public void add(Project project)
    {
        projects.add(project);
    }

    public void addAll(Collection<Project> projects)
    {
        for(Project p: projects)
        {
            add(p);
        }
    }

    public boolean remove(Project project)
    {
        return projects.remove(project);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ProjectGroup that = (ProjectGroup) o;

        if (!name.equals(that.name))
        {
            return false;
        }

        return projects.equals(that.projects);
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + projects.hashCode();
        return result;
    }
}
