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

package com.zutubi.pulse.master.dependency;

import com.zutubi.util.adt.TreeNode;

/**
 * Holds trees with upstream and downstream dependencies for a project.  Note
 * that trees may contain duplicated projects (i.e. the same project may be
 * reached by multiple paths).
 */
public class ProjectDependencyGraph
{
    private TreeNode<DependencyGraphData> upstreamRoot;
    private TreeNode<DependencyGraphData> downstreamRoot;

    /**
     * Create a graph holding the given trees.  The project of interest is the
     * root of each tree.
     *
     * @param upstreamRoot   tree of projects that the project of interest
     *                       depends on
     * @param downstreamRoot tree of projects that depend on the project of
     *                       interest
     */
    public ProjectDependencyGraph(TreeNode<DependencyGraphData> upstreamRoot, TreeNode<DependencyGraphData> downstreamRoot)
    {
        this.upstreamRoot = upstreamRoot;
        this.downstreamRoot = downstreamRoot;
    }

    public TreeNode<DependencyGraphData> getUpstreamRoot()
    {
        return upstreamRoot;
    }

    public TreeNode<DependencyGraphData> getDownstreamRoot()
    {
        return downstreamRoot;
    }
}
