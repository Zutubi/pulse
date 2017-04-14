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

import static com.zutubi.pulse.core.dependency.ivy.IvyStatus.*;

public class StatusFilterTest extends GraphFilterTestCase
{
    public void testDownstreamStatusFiltering()
    {
        ProjectConfiguration util = project("util");
        ProjectConfiguration lib = project("lib");

        TreeNode<BuildGraphData> root = node(util, node(lib, dependency(util, "latest.milestone")));

        assertFilters(1, STATUS_INTEGRATION, root);
        assertFilters(0, STATUS_MILESTONE, root);
        assertFilters(0, STATUS_RELEASE, root);
    }

    public void testDownstreamFixedRevision()
    {
        ProjectConfiguration util = project("util");
        ProjectConfiguration lib = project("lib");

        TreeNode<BuildGraphData> root = node(util, node(lib, dependency(util, "FIXED")));

        assertFilters(0, STATUS_INTEGRATION, root);
    }

    public void testUpstreamStatusFiltering()
    {
        ProjectConfiguration util = project("util");
        ProjectConfiguration lib = project("lib");

        TreeNode<BuildGraphData> root = node(lib, node(util, dependency(util, "latest.milestone")));

        assertFilters(1, STATUS_INTEGRATION, root);
        assertFilters(0, STATUS_MILESTONE, root);
        assertFilters(0, STATUS_RELEASE, root);
    }

    public void testUpstreamFixedRevision()
    {
        ProjectConfiguration util = project("util");
        ProjectConfiguration lib = project("lib");

        TreeNode<BuildGraphData> root = node(lib, node(util, dependency(util, "FIXED")));

        assertFilters(0, STATUS_INTEGRATION, root);
    }

    private void assertFilters(int filterCount, String status, TreeNode<BuildGraphData> node)
    {
        StatusFilter filter = new StatusFilter(status);
        applyFilter(filter, node);
        assertEquals(filterCount, filter.getToTrim().size());
    }
}
