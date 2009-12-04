package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.util.TreeNode;

/**
 * The transitive filter trims the children of dependencies where
 * the transitive flag is set to false.
 */
public class TransitiveFilter extends GraphFilter
{
    public void process(TreeNode<GraphData> node)
    {
        if (!node.isRoot())
        {
            DependencyConfiguration dependency = node.getData().getDependency();
            if (!dependency.isTransitive())
            {
                toTrim.addAll(node.getChildren());
            }
        }
    }

}
