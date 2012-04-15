package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.util.adt.TreeNode;

public abstract class GraphFilterTestCase extends BaseGraphTestCase
{
    protected void applyFilter(GraphFilter filter, TreeNode<BuildGraphData> node)
    {
        node.breadthFirstWalk(filter);
    }
}
