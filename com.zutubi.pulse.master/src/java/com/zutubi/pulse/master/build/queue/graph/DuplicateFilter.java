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
    Map<Project, TreeNode<BuildGraphData>> seenProjects = new HashMap<Project, TreeNode<BuildGraphData>>();

    public void process(TreeNode<BuildGraphData> node)
    {
        TreeNode<BuildGraphData> lastSeen = seenProjects.get(node.getData().getProject());
        if (lastSeen != null)
        {
            toTrim.addAll(lastSeen.getChildren());
        }

        seenProjects.put(node.getData().getProject(), node);
    }
}

