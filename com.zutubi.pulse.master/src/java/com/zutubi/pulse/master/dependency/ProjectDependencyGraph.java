package com.zutubi.pulse.master.dependency;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.TreeNode;

/**
 * Holds trees with upstream and downstream dependencies for a project.  Note
 * that trees may contain duplicated projects (i.e. the same project may be
 * reached by multiple paths).
 */
public class ProjectDependencyGraph
{
    private TreeNode<Project> upstreamRoot;
    private TreeNode<Project> downstreamRoot;

    /**
     * Create a graph holding the given trees.  The project of interest is the
     * root of each tree.
     *
     * @param upstreamRoot   tree of projects that the project of interest
     *                       depends on
     * @param downstreamRoot tree of projects that depend on the project of
     *                       interest
     */
    public ProjectDependencyGraph(TreeNode<Project> upstreamRoot, TreeNode<Project> downstreamRoot)
    {
        this.upstreamRoot = upstreamRoot;
        this.downstreamRoot = downstreamRoot;
    }

    public TreeNode<Project> getUpstreamRoot()
    {
        return upstreamRoot;
    }

    public TreeNode<Project> getDownstreamRoot()
    {
        return downstreamRoot;
    }
}
