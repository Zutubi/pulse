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

public class TransitiveFilterTest extends GraphFilterTestCase
{
    private TransitiveFilter filter;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        filter = new TransitiveFilter();
    }

    public void testNoneTransitiveDependenciesFilterChildren()
    {
        ProjectConfiguration client = project("client");
        ProjectConfiguration lib = project("lib");
        ProjectConfiguration util = project("util");

        TreeNode<BuildGraphData> node = node(client, node(lib, dependency(lib, false), node(util, dependency(util))));

        applyFilter(filter, node);
        assertEquals(1, filter.getToTrim().size());
    }

    public void testTransitiveDependenciesDoNotFilter()
    {
        ProjectConfiguration client = project("client");
        ProjectConfiguration lib = project("lib");
        ProjectConfiguration util = project("util");

        TreeNode<BuildGraphData> node = node(client, node(lib, dependency(lib), node(util, dependency(util))));

        applyFilter(filter, node);
        assertEquals(0, filter.getToTrim().size());
    }
}
