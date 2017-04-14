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

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.adt.TreeNode;

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

