package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.util.TreeNode;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

import java.util.List;
import java.util.LinkedList;

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
public abstract class GraphFilter implements  UnaryProcedure<TreeNode<GraphData>>
{
    protected List<TreeNode<GraphData>> toTrim = new LinkedList<TreeNode<GraphData>>();

    public boolean contains(TreeNode<GraphData> node)
    {
        return toTrim.contains(node);
    }

    public List<TreeNode<GraphData>> getToTrim()
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
    protected boolean isUpstream(TreeNode<GraphData> node)
    {
        if (node.isRoot() && node.getData().getDependency() == null)
        {
            // special case for the downstream root node.
            return false;
        }

        DependencyConfiguration dependency = node.getData().getDependency();
        ProjectConfiguration project = node.getData().getProject().getConfig();
        return project.equals(dependency.getProject());
    }

    /**
     * Returns true if the node is part of a downstream graph.
     *
     * @param node  the node being checked.
     * @return true if the node is part of a downstream graph, false otherwise.
     *
     * @see #isUpstream(com.zutubi.util.TreeNode)
     */
    protected boolean isDownstream(TreeNode<GraphData> node)
    {
        return !isUpstream(node);
    }
}

