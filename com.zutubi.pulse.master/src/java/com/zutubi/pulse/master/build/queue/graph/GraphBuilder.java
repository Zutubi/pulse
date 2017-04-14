/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.build.queue.graph;

import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.find;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.adt.TreeNode;

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
        return find(fromProject.getDependencies().getDependencies(), new Predicate<DependencyConfiguration>()
        {
            public boolean apply(DependencyConfiguration dependency)
            {
                return toProject.equals(dependency.getProject());
            }
        }, null);
    }

    private void applyFilters(TreeNode<BuildGraphData> root, GraphFilter... filters)
    {
        for (final GraphFilter filter : filters)
        {
            root.breadthFirstWalk(filter);
            root.filteringWalk(new Predicate<TreeNode<BuildGraphData>>()
            {
                public boolean apply(TreeNode<BuildGraphData> node)
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
