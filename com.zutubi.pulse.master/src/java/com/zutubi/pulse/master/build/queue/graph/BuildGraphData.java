package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * The data contained by the dependency graph node.
 */
public class BuildGraphData
{
    /**
     * Configuration of the project represented by the node itself.
     */
    private ProjectConfiguration projectConfig;

    /**
     * The dependency that was traversed to get to this node.
     * In the upstream case, the dependency references this nodes
     * project.  In the downstream case, the dependency references
     * the project that lead to this node.
     */
    private DependencyConfiguration dependency;

    public BuildGraphData(ProjectConfiguration projectConfig)
    {
        this.projectConfig = projectConfig;
    }

    public ProjectConfiguration getProjectConfig()
    {
        return projectConfig;
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
