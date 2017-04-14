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

package com.zutubi.pulse.master.model;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.util.adt.DAGraph;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * A build dependency graph, linking a root build to all of its dependencies in one direction
 * (either upstream or downstream) transitively.  This is a DAG (Directed Acyclic Graph).
 */
public class BuildGraph extends DAGraph<BuildResult>
{
    /**
     * Creates a new graph rooted at the given node.
     *
     * @param root the root of the graph
     */
    public BuildGraph(Node<BuildResult> root)
    {
        super(root);
    }

    /**
     * Returns the node representing the given build, if any.
     * 
     * @param buildId id of the build to find the node for
     * @return the node for the given build, or null if there is no such node
     */
    public Node<BuildResult> findNodeByBuildId(final long buildId)
    {
        return findNodeByPredicate(new Predicate<Node<BuildResult>>()
        {
            public boolean apply(DAGraph.Node<BuildResult> node)
            {
                return node.getData().getId() == buildId;
            }
        });
    }

    /**
     * Returns all possible build paths for a given node.  Each path is a sequence of build results
     * found on nodes between the root (excluded) and the given node (included).
     * 
     * @param node the node to get the path for
     * @return all possible paths of build results from the root to the given node, empty if the
     *         node is not found in this graph (or is the root itself).
     */
    public Set<BuildPath> getBuildPaths(Node<BuildResult> node)
    {
        Set<List<Node<BuildResult>>> paths = getAllPathsTo(node);
        return newHashSet(Iterables.transform(paths, new Function<List<Node<BuildResult>>, BuildPath>()
        {
            public BuildPath apply(List<Node<BuildResult>> path)
            {
                return new BuildPath(path);
            }
        }));
    }

    /**
     * Finds a node by a given build path, using the projects of the builds to walk the graph.  This
     * can be used to find equivalent nodes in two graphs rooted at builds of the same project.
     * Note that the shape of the graph can change between builds, so sometimes there is no
     * equivalent build to find.
     * 
     * @param buildPath build path from which the projects are extracted and used to traverse from
     *                  the root of this graph
     * @return the node found by traversing the full path, or null if no such node could be found
     */
    public Node<BuildResult> findNodeByProjects(BuildPath buildPath)
    {
        Node<BuildResult> node = getRoot();
        for (BuildResult build: buildPath)
        {
            node = nextByProjectId(node, build.getProject().getId());
            if (node == null)
            {
                break;
            }
        }
        
        return node;
    }

    private Node<BuildResult> nextByProjectId(Node<BuildResult> node, long projectId)
    {
        for (Node<BuildResult> connected: node.getConnected())
        {
            if (connected.getData().getProject().getId() == projectId)
            {
                return connected;
            }
        }

        return null;
    }

}
