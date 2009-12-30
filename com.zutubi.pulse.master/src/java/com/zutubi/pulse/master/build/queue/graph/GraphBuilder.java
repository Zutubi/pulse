package com.zutubi.pulse.master.build.queue.graph;

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

    public TreeNode<BuildGraphData> buildUpstreamGraph(ProjectConfiguration projectConfig, GraphFilter... filters)
    {
        TreeNode<BuildGraphData> node = new TreeNode<BuildGraphData>(new BuildGraphData(projectConfig));

        buildUpstreamGraph(projectConfig, node);

        applyFilters(node, filters);

        return node;
    }

    private void buildUpstreamGraph(ProjectConfiguration projectConfig, TreeNode<BuildGraphData> node)
    {
        List<DependencyConfiguration> dependencies = projectConfig.getDependencies().getDependencies();
        for (DependencyConfiguration dependency : dependencies)
        {
            ProjectConfiguration dependentProjectConfig = dependency.getProject();
            TreeNode<BuildGraphData> child = new TreeNode<BuildGraphData>(new BuildGraphData(dependentProjectConfig));
            buildUpstreamGraph(dependentProjectConfig, child);
            child.getData().setDependency(dependency);
            node.add(child);
        }
    }

    public TreeNode<BuildGraphData> buildDownstreamGraph(ProjectConfiguration projectConfig, GraphFilter... filters)
    {
        TreeNode<BuildGraphData> node = new TreeNode<BuildGraphData>(new BuildGraphData(projectConfig));

        buildDownstreamGraph(projectConfig, node);

        applyFilters(node, filters);

        return node;
    }

    private void buildDownstreamGraph(ProjectConfiguration projectConfig, TreeNode<BuildGraphData> node)
    {
        List<ProjectConfiguration> downstreamProjectConfigs = projectManager.getDownstreamDependencies(projectConfig);
        for (ProjectConfiguration downstream: downstreamProjectConfigs)
        {
            TreeNode<BuildGraphData> child = new TreeNode<BuildGraphData>(new BuildGraphData(downstream));
            buildDownstreamGraph(downstream, child);
            child.getData().setDependency(findDependency(downstream,  projectConfig));
            node.add(child);
        }
    }

    private DependencyConfiguration findDependency(ProjectConfiguration fromProject, final ProjectConfiguration toProject)
    {
        return CollectionUtils.find(fromProject.getDependencies().getDependencies(), new Predicate<DependencyConfiguration>()
        {
            public boolean satisfied(DependencyConfiguration dependency)
            {
                return toProject.equals(dependency.getProject()) ;
            }
        });
    }

    private void applyFilters(TreeNode<BuildGraphData> root, GraphFilter... filters)
    {
        for (final GraphFilter filter : filters)
        {
            root.breadthFirstWalk(filter);
            root.filteringWalk(new Predicate<TreeNode<BuildGraphData>>()
            {
                public boolean satisfied(TreeNode<BuildGraphData> node)
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
