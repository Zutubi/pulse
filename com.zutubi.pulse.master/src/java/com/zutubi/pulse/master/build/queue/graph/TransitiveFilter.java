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

import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.util.adt.TreeNode;

/**
 * The transitive filter trims the children of dependencies where
 * the transitive flag is set to false.
 */
public class TransitiveFilter extends GraphFilter
{
    public void run(TreeNode<BuildGraphData> node)
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
