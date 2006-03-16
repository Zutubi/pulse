package com.cinnamonbob;

import com.cinnamonbob.model.Project;

import java.util.Comparator;

/**
 * A comparator that orders projects lexically according to their names.
 */
public class ProjectNameComparator implements Comparator<Project>
{
    public int compare(Project p1, Project p2)
    {
        return p1.getName().compareTo(p2.getName());
    }
}
