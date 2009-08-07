package com.zutubi.pulse.master.model;

import com.zutubi.util.StringUtils;

/**
 * Stores a (label, project template) pairing used to represent a collapsible
 * row on project summary views (e.g. the dashboard and browse views).
 * <p/>
 * Note that these tuples may become stale, as there is no foreign key
 * relationship for either of the components (they are pure configuration).
 * Clients should ignore stale items (and preferrably clean them).
 */
public class LabelProjectTuple
{
    private static final int NO_PROJECT = 0;
    
    /**
     * Label, may be empty to indicate "ungrouped" projects.
     */
    private String label;
    /**
     * Handle of the project template, may be zero to indicate the label
     * (group) itself is collapsed.
     */
    private long projectHandle;

    public LabelProjectTuple()
    {
    }

    public LabelProjectTuple(String label)
    {
        this(label, NO_PROJECT);
    }

    public LabelProjectTuple(String label, long projectHandle)
    {
        this.label = label;
        this.projectHandle = projectHandle;
    }

    public boolean isUnlabelled()
    {
        return !StringUtils.stringSet(label);
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public boolean isSpecificProject()
    {
        return projectHandle != NO_PROJECT;
    }

    public long getProjectHandle()
    {
        return projectHandle;
    }

    public void setProjectHandle(long projectHandle)
    {
        this.projectHandle = projectHandle;
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

        LabelProjectTuple that = (LabelProjectTuple) o;

        if (projectHandle != that.projectHandle)
        {
            return false;
        }
        return label.equals(that.label);
    }

    @Override
    public int hashCode()
    {
        int result = label.hashCode();
        result = 31 * result + (int) (projectHandle ^ (projectHandle >>> 32));
        return result;
    }

    @Override
    public String toString()
    {
        return "(" + label + ", " + projectHandle + ")";
    }
}
