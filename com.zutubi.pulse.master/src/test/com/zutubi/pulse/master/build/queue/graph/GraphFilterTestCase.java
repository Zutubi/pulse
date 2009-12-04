package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.util.TreeNode;

public abstract class GraphFilterTestCase extends BaseGraphTestCase
{
    protected void applyFilter(GraphFilter filter, TreeNode<GraphData> node)
    {
        node.breadthFirstWalk(filter);
    }
}
