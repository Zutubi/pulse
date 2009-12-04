package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.TreeNode;

import java.util.List;

/**
 * A dependency graph builder implementation.
 *
 * @see com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder
 */
// somewhat rather like the already existing ProjectDependencyGraphBuilder, but annoying
// enough in its differences during implementation that i have created a separate version
// here.  At some point the two (now complete) implementations need to be reviewed and
// merged.
public class GraphBuilder
{
    private ProjectManager projectManager;

    public TreeNode<GraphData> buildUpstreamGraph(Project project, GraphFilter... filters)
    {
        TreeNode<GraphData> node = new TreeNode<GraphData>(new GraphData(project));

        buildUpstreamGraph(project, node);

        applyFilters(node, filters);

        return node;
    }

    private void buildUpstreamGraph(Project project, TreeNode<GraphData> node)
    {
        List<DependencyConfiguration> dependencies = project.getConfig().getDependencies().getDependencies();
        for (DependencyConfiguration dependency : dependencies)
        {
            Project dependentProject = projectManager.getProject(dependency.getProject().getProjectId(), false);
            TreeNode<GraphData> child = new TreeNode<GraphData>(new GraphData(dependentProject));
            buildUpstreamGraph(dependentProject, child);
            child.getData().setDependency(dependency);
            node.add(child);
        }
    }

    public TreeNode<GraphData> buildDownstreamGraph(Project project, GraphFilter... filters)
    {
        TreeNode<GraphData> node = new TreeNode<GraphData>(new GraphData(project));

        buildDownstreamGraph(project, node);

        applyFilters(node, filters);

        return node;
    }

    private void buildDownstreamGraph(Project project, TreeNode<GraphData> node)
    {
        List<ProjectConfiguration> downstreamProjectConfigs = projectManager.getDownstreamDependencies(project.getConfig());
        List<Project> downstreamProjects = projectManager.mapConfigsToProjects(downstreamProjectConfigs);

        for (Project downstream: downstreamProjects)
        {
            TreeNode<GraphData> child = new TreeNode<GraphData>(new GraphData(downstream));
            buildDownstreamGraph(downstream, child);
            child.getData().setDependency(findDependency(downstream,  project));
            node.add(child);
        }
    }

    private DependencyConfiguration findDependency(Project fromProject, final Project toProject)
    {
        return CollectionUtils.find(fromProject.getConfig().getDependencies().getDependencies(), new Predicate<DependencyConfiguration>()
        {
            public boolean satisfied(DependencyConfiguration dependency)
            {
                return toProject.getConfig().equals(dependency.getProject()) ;
            }
        });
    }

    private void applyFilters(TreeNode<GraphData> root, GraphFilter... filters)
    {
        for (final GraphFilter filter : filters)
        {
            root.breadthFirstWalk(filter);
            root.filteringWalk(new Predicate<TreeNode<GraphData>>()
            {
                public boolean satisfied(TreeNode<GraphData> node)
                {
                    return !filter.contains(node);
                }
            });
        }
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
