package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.util.Grid;

/**
 * JSON structure for the project dependencies page.
 */
public class ProjectDepenenciesModel
{
    private String transitiveMode;
    private Grid<ProjectDependencyData> upstream;
    private Grid<ProjectDependencyData> downstream;

    public ProjectDepenenciesModel(String transitiveMode, Grid<ProjectDependencyData> upstream, Grid<ProjectDependencyData> downstream)
    {
        this.transitiveMode = transitiveMode;
        this.upstream = upstream;
        this.downstream = downstream;
    }

    public String getTransitiveMode()
    {
        return transitiveMode;
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
