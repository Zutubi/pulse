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

import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.adt.TreeNode;

import java.util.LinkedList;
import java.util.List;

/**
 * A graph filter is, as the name suggests, a class that provides some
 * form of filtering of the graph produced by the graph builder.
 *
 * A filter works by being walked over a tree and recording the nodes
 * that it encounters that should be removed.  Once the list is complete
 * and the full tree has been traversed, the GraphBuilder uses the list
 * of nodes to filter the graph it produces.
 *
 * @see com.zutubi.pulse.master.build.queue.graph.GraphBuilder
 */
public abstract class GraphFilter implements  UnaryProcedure<TreeNode<BuildGraphData>>
{
    protected List<TreeNode<BuildGraphData>> toTrim = new LinkedList<TreeNode<BuildGraphData>>();

    public boolean contains(TreeNode<BuildGraphData> node)
    {
        return toTrim.contains(node);
    }

    public List<TreeNode<BuildGraphData>> getToTrim()
    {
        return toTrim;
    }

    /**
     * Returns true if the node is part of an upstream graph.
     * <p/>
     * The upstream graph is characterised by dependencies being located on the node
     * they reference.  Downstream graph is the opposite.
     *
     * @param node the node being checked.
     * @return true if the node is part of an upstream graph, false otherwise.
     */
    protected boolean isUpstream(TreeNode<BuildGraphData> node)
    {
        if (node.isRoot() && node.getData().getDependency() == null)
        {
            // special case for the downstream root node.
            return false;
        }

        DependencyConfiguration dependency = node.getData().getDependency();
        ProjectConfiguration project = node.getData().getProjectConfig();
        return project.equals(dependency.getProject());
    }

    /**
     * Returns true if the node is part of a downstream graph.
     *
     * @param node  the node being checked.
     * @return true if the node is part of a downstream graph, false otherwise.
     *
     * @see #isUpstream(com.zutubi.util.adt.TreeNode)
     */
    protected boolean isDownstream(TreeNode<BuildGraphData> node)
    {
        return !isUpstream(node);
    }
}

