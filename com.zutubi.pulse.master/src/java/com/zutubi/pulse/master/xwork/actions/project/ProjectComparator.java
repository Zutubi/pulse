package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.Project;

import java.util.Comparator;

/**
 * Compares projects by name, handling those with null names (happens if there
 * is a project in the database with no configuration).
 */
class ProjectComparator implements Comparator<Project>
{
    private final Comparator<String> comp;

    public ProjectComparator(Comparator<String> comp)
    {
        this.comp = comp;
    }

    public int compare(Project o1, Project o2)
    {
        if (o1.getName() == null)
        {
            return -1;
        }

        if (o2.getName() == null)
        {
            return 1;
        }

        return comp.compare(o1.getName(), o2.getName());
    }
}
