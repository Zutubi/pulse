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

package com.zutubi.pulse.master.dependency;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.adt.TreeNode;

import java.util.*;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Service to yield dependency graphs for projects.  Contains the building
 * logic and cached information to ensure building graphs is fast.
 */
public class ProjectDependencyGraphBuilder
{
    public enum TransitiveMode
    {
        /**
         * Show all transitive dependencies, including duplicates.
         */
        FULL,
        /**
         * Show transitive dependencies but trim the children of duplicate
         * subtree roots and mark those roots as filtered.  A single occurrence
         * of the subtree is left intact at the deepest level found.
         */
        TRIM_DUPLICATES,
        /**
         * Show transitive dependencies but completely remove duplicate
         * subtrees.  A single occurrence of the subtree is left intact at the
         * deepest level found.
         */
        REMOVE_DUPLICATES,
        /**
         * Show only a single level of dependencies.
         */
        NONE
    }

    private ProjectManager projectManager;

    /**
     * Builds and returns the dependency graph for the given project.
     *
     * @param project project to build the graph for
     * @param mode    mode that determines if and how transitive dependencies
     *                are included in the result (see the enum constants for
     *                details)
     * @return a dependency graph for the given project
     */
    public ProjectDependencyGraph build(Project project, TransitiveMode mode)
    {
        // Although validation should prevent cycles, we add checks to the build methods just in
        return new ProjectDependencyGraph(buildUpstream(project, mode, new HashSet<Long>()),
                buildDownstream(project, mode, new HashSet<Long>()));
    }

    private TreeNode<DependencyGraphData> buildUpstream(Project project, TransitiveMode mode, Set<Long> seenProjects)
    {
        seenProjects.add(project.getId());
        TreeNode<DependencyGraphData> node = new TreeNode<DependencyGraphData>(new DependencyGraphData(project));

        List<ProjectConfiguration> upstreamProjectConfigs = getDependentProjectConfigs(project.getConfig());
        List<Project> upstreamProjects = projectManager.mapConfigsToProjects(upstreamProjectConfigs);
        for (Project upstream: upstreamProjects)
        {
            if (!seenProjects.contains(upstream.getId()))
            {            
                TreeNode<DependencyGraphData> child;
                if (mode == TransitiveMode.NONE)
                {
                    child = new TreeNode<DependencyGraphData>(new DependencyGraphData(upstream));
                }
                else
                {
                    child = buildUpstream(upstream, mode, seenProjects);
                }
    
                node.add(child);
            }
        }

        processDuplicateSubtrees(node, mode);
        seenProjects.remove(project.getId());
        return node;
    }

    private List<ProjectConfiguration> getDependentProjectConfigs(ProjectConfiguration config)
    {
        List<DependencyConfiguration> dependencies = config.getDependencies().getDependencies();
        return newArrayList(transform(dependencies, new Function<DependencyConfiguration, ProjectConfiguration>()
        {
            public ProjectConfiguration apply(DependencyConfiguration dependencyConfiguration)
            {
                return dependencyConfiguration.getProject();
            }
        }));
    }

    private TreeNode<DependencyGraphData> buildDownstream(Project project, TransitiveMode mode, Set<Long> seenProjects)
    {
        seenProjects.add(project.getId());
        TreeNode<DependencyGraphData> node = new TreeNode<DependencyGraphData>(new DependencyGraphData(project));

        List<ProjectConfiguration> downstreamProjectConfigs = projectManager.getDownstreamDependencies(project.getConfig());
        List<Project> downstreamProjects = projectManager.mapConfigsToProjects(downstreamProjectConfigs);
        for (Project downstream: downstreamProjects)
        {
            if (!seenProjects.contains(downstream.getId()))
            {
                TreeNode<DependencyGraphData> child;
                if (mode == TransitiveMode.NONE)
                {
                    child = new TreeNode<DependencyGraphData>(new DependencyGraphData(downstream));
                }
                else
                {
                    child = buildDownstream(downstream, mode, seenProjects);
                }
    
                node.add(child);
            }
        }

        processDuplicateSubtrees(node, mode);
        seenProjects.remove(project.getId());
        return node;
    }

    private void processDuplicateSubtrees(TreeNode<DependencyGraphData> root, TransitiveMode mode)
    {
        if (mode == TransitiveMode.TRIM_DUPLICATES || mode == TransitiveMode.REMOVE_DUPLICATES)
        {
            final Map<Project, TreeNode<DependencyGraphData>> seenProjects = new HashMap<Project, TreeNode<DependencyGraphData>>();

            root.breadthFirstWalk(new UnaryProcedure<TreeNode<DependencyGraphData>>()
            {
                public void run(TreeNode<DependencyGraphData> node)
                {
                    TreeNode<DependencyGraphData> lastSeen = seenProjects.get(node.getData().getProject());
                    if (lastSeen != null)
                    {
                        // Seen somewhere less (or equally as) deep, filter that
                        // previous node's children.  We can safely modify that
                        // node as the walk has already visited it (and although we
                        // may needlessly walk on its children, that does no harm).
                        lastSeen.getData().markSubtreeFiltered();
                        lastSeen.clear();
                    }

                    seenProjects.put(node.getData().getProject(), node);
                }
            });

            if (mode == TransitiveMode.REMOVE_DUPLICATES)
            {
                // Remove the marked nodes themselves too.
                root.filteringWalk(new Predicate<TreeNode<DependencyGraphData>>()
                {
                    public boolean apply(TreeNode<DependencyGraphData> node)
                    {
                        return !node.getData().isSubtreeFiltered();
                    }
                });
            }
        }
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
