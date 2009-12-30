package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.TreeNode;

import java.util.HashMap;
import java.util.Map;

/**
 * The duplicate filter trims the children of duplicate nodes from the
 * tree.
 * <p/>
 * Note that it does not remove the duplicates themselves.
 */
public class DuplicateFilter extends GraphFilter
{
    Map<ProjectConfiguration, TreeNode<BuildGraphData>> seenProjects = new HashMap<ProjectConfiguration, TreeNode<BuildGraphData>>();

    public void run(TreeNode<BuildGraphData> node)
    {
        TreeNode<BuildGraphData> lastSeen = seenProjects.get(node.getData().getProjectConfig());
        if (lastSeen != null)
        {
            toTrim.addAll(lastSeen.getChildren());
        }

        seenProjects.put(node.getData().getProjectConfig(), node);
    }
}

