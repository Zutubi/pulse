package com.zutubi.pulse.master.model;

import com.zutubi.util.Sort;

import java.util.Comparator;

/**
 * A comparator that uses lexological ordering to order project groups based on their names.
 */
public class ProjectGroupNameComparator implements Comparator<ProjectGroup>
{
    private Comparator<String> delegate = new Sort.StringComparator();

    public int compare(ProjectGroup o1, ProjectGroup o2)
    {
        return delegate.compare(o1.getName(), o2.getName());
    }
}
