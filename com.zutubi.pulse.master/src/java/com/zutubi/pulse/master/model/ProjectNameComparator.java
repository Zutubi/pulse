package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.NamedEntity;
import com.zutubi.pulse.core.model.NamedEntityComparator;

import java.util.Comparator;

/**
 * A comparator that uses lexological ordering to order projects based on their names.
 */
public class ProjectNameComparator implements Comparator<Project>
{
    private Comparator<NamedEntity> delegate = new NamedEntityComparator();

    public int compare(Project o1, Project o2)
    {
        return delegate.compare(o1, o2);
    }
}
