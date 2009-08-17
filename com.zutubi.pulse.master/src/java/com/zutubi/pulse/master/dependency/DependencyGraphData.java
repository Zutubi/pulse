package com.zutubi.pulse.master.dependency;

import com.zutubi.pulse.master.model.Project;

/**
 * Data holding class for the information in tree nodes in a project dependency
 * graph.
 */
public class DependencyGraphData
{
    private Project project;
    private boolean subtreeFiltered = false;

    public DependencyGraphData(Project project)
    {
        this.project = project;
    }

    public Project getProject()
    {
        return project;
    }

    public boolean isSubtreeFiltered()
    {
        return subtreeFiltered;
    }

    public void markSubtreeFiltered()
    {
        subtreeFiltered = true;
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

        DependencyGraphData that = (DependencyGraphData) o;
        if (subtreeFiltered != that.subtreeFiltered)
        {
            return false;
        }
        if (!project.equals(that.project))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = project.hashCode();
        result = 31 * result + (subtreeFiltered ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "(" + project.getName() + ", "  + subtreeFiltered + ")";
    }
}
