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

import com.zutubi.pulse.core.dependency.ivy.IvyLatestRevisionMatcher;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.util.adt.TreeNode;

/**
 * The status filter trims the nodes in a dependency tree that
 * the dependency system would not reach.
 *
 * For instance, project A depends on latest.milestone revision
 * of project B.  If the status being built of project B is
 * integration, then the dependency from project A to project B
 * would not be traversed and project B can be filtered.
 *
 * Note: Filtering only occurs for revisions that match ivys'
 * latest revision matcher.  It currently ignores all other
 * cases.
 */
public class StatusFilter extends GraphFilter
{
    private String status;

    public StatusFilter(String status)
    {
        this.status = status;
    }

    public void run(TreeNode<BuildGraphData> node)
    {
        if (!node.isRoot())
        {
            DependencyConfiguration dependency = node.getData().getDependency();
            String revision = dependency.getRevision();

            IvyLatestRevisionMatcher ivyMatcher = new IvyLatestRevisionMatcher();
            if (ivyMatcher.isApplicable(revision) && !ivyMatcher.accept(revision, status))
            {
                toTrim.add(node);
            }
        }
    }
}