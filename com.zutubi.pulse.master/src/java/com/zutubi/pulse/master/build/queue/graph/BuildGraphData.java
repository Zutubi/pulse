package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;

/**
 * The data contained by the dependency graph node.
 */
public class BuildGraphData
{
    /**
     * The project represented by the node itself.
     */
    private Project project;

    /**
     * The dependency that was traversed to get to this node.
     * In the upstream case, the dependency references this nodes
     * project.  In the downstream case, the dependency references
     * the project that lead to this node.
     */
    private DependencyConfiguration dependency;

    public BuildGraphData(Project project)
    {
        this.project = project;
    }

    public Project getProject()
    {
        return project;
    }

    public DependencyConfiguration getDependency()
    {
        return dependency;
    }

    public void setDependency(DependencyConfiguration dependency)
    {
        this.dependency = dependency;
    }
}
