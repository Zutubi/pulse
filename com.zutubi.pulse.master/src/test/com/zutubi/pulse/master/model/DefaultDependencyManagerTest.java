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

package com.zutubi.pulse.master.model;

import com.zutubi.util.adt.DAGraph;

public class DefaultDependencyManagerTest extends BuildRelatedManagerTestCase
{
    public void testUpstreamGraphTrivial()
    {
        // 1_1
        BuildGraph expected = new BuildGraph(new DAGraph.Node<BuildResult>(build1_1));
        assertEquals(expected, dependencyManager.getUpstreamDependencyGraph(build1_1));
    }

    public void testUpstreamGraphSingleUpstream()
    {
        // 1_1 - 2_1
        link(build1_1, build2_1);

        DAGraph.Node<BuildResult> root = new DAGraph.Node<BuildResult>(build2_1);
        root.connectNode(new DAGraph.Node<BuildResult>(build1_1));
        BuildGraph expected = new BuildGraph(root);

        assertEquals(expected, dependencyManager.getUpstreamDependencyGraph(build2_1));
    }

    public void testUpstreamGraphMultipleUpstream()
    {
        // 1_1
        //     > 3_1
        // 2_1
        link(build1_1, build3_1);
        link(build2_1, build3_1);

        DAGraph.Node<BuildResult> root = new DAGraph.Node<BuildResult>(build3_1);
        root.connectNode(new DAGraph.Node<BuildResult>(build1_1));
        root.connectNode(new DAGraph.Node<BuildResult>(build2_1));
        BuildGraph expected = new BuildGraph(root);

        assertEquals(expected, dependencyManager.getUpstreamDependencyGraph(build3_1));
    }

    public void testUpstreamGraphDiamond()
    {
        //       2_1
        // 1_1 <     > 4_1
        //       3_1
        link(build1_1, build2_1);
        link(build1_1, build3_1);
        link(build2_1, build4_1);
        link(build3_1, build4_1);

        DAGraph.Node<BuildResult> n1 = new DAGraph.Node<BuildResult>(build1_1);
        DAGraph.Node<BuildResult> n2 = new DAGraph.Node<BuildResult>(build2_1);
        DAGraph.Node<BuildResult> n3 = new DAGraph.Node<BuildResult>(build3_1);
        DAGraph.Node<BuildResult> n4 = new DAGraph.Node<BuildResult>(build4_1);
        n2.connectNode(n1);
        n3.connectNode(n1);
        n4.connectNode(n2);
        n4.connectNode(n3);

        BuildGraph expected = new BuildGraph(n4);

        assertEquals(expected, dependencyManager.getUpstreamDependencyGraph(build4_1));
    }
}
