package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.TreeNode;

import java.util.Map;
import java.util.HashMap;

/**
 * The duplicate filter trims the children of duplicate nodes from the
 * tree.
 * <p/>
 * Note that it does not remove the duplicates themselves.
 */
public class DuplicateFilter extends GraphFilter
{
    Map<Project, TreeNode<GraphData>> seenProjects = new HashMap<Project, TreeNode<GraphData>>();

    public void process(TreeNode<GraphData> node)
    {
        TreeNode<GraphData> lastSeen = seenProjects.get(node.getData().getProject());
        if (lastSeen != null)
        {
            toTrim.addAll(lastSeen.getChildren());
        }

        seenProjects.put(node.getData().getProject(), node);
    }
}

