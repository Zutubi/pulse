package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.util.Grid;

/**
 * JSON structure for the project dependencies page.
 */
public class ProjectDepenenciesModel
{
    private Grid<ProjectDependencyData> upstream;
    private Grid<ProjectDependencyData> downstream;

    public ProjectDepenenciesModel(Grid<ProjectDependencyData> upstream, Grid<ProjectDependencyData> downstream)
    {
        this.upstream = upstream;
        this.downstream = downstream;
    }

    public Grid<ProjectDependencyData> getUpstream()
    {
        return upstream;
    }

    public Grid<ProjectDependencyData> getDownstream()
    {
        return downstream;
    }
}
