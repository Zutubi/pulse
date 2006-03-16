package com.cinnamonbob;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.util.Sort;

import java.util.Comparator;

/**
 * A comparator that orders projects lexically according to their names.
 */
public class ProjectNameComparator implements Comparator<Project>
{
    private Sort.StringComparator sc = new Sort.StringComparator();

    public int compare(Project p1, Project p2)
    {
        return sc.compare(p1.getName(), p2.getName());
    }
}
