package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.dependency.ProjectDependencyGraph;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.Grid;

/**
 * Action to display project home page - the latest project status.
 */
public class ProjectDependenciesAction extends ProjectActionBase
{
    private ProjectDependencyGraphBuilder projectDependencyGraphBuilder;
    private Grid<ProjectDependencyData> upstream;
    private Grid<ProjectDependencyData> downstream;

    public Grid<ProjectDependencyData> getUpstream()
    {
        return upstream;
    }

    public Grid<ProjectDependencyData> getDownstream()
    {
        return downstream;
    }

    public ProjectHealth getHealth(Project project)
    {
        return ProjectHealth.fromLatestBuilds(buildManager.getLatestCompletedBuildResults(project, 1));
    }

    public String execute()
    {
        Project project = getRequiredProject();
        ProjectDependencyGraph dependencyGraph = projectDependencyGraphBuilder.build(project);
        ProjectDependencyGraphRenderer renderer = new ProjectDependencyGraphRenderer();
        upstream = renderer.renderUpstream(dependencyGraph);
        downstream = renderer.renderDownstream(dependencyGraph);

        return SUCCESS;
    }

    public void setProjectDependencyGraphBuilder(ProjectDependencyGraphBuilder projectDependencyGraphBuilder)
    {
        this.projectDependencyGraphBuilder = projectDependencyGraphBuilder;
    }
}
