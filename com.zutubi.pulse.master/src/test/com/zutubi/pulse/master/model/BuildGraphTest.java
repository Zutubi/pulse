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

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.adt.DAGraph;

import java.util.HashSet;
import java.util.Set;

public class BuildGraphTest extends PulseTestCase
{
    // The graph looks like:
    //     2
    // 1 <   > 4 - 5
    //     3
    private BuildGraph graph;
    private DAGraph.Node<BuildResult> n2;
    private DAGraph.Node<BuildResult> n3;
    private DAGraph.Node<BuildResult> n4;
    private DAGraph.Node<BuildResult> n5;

    @Override
    protected void setUp() throws Exception
    {
        DAGraph.Node<BuildResult> n1 = createNode(1);
        n2 = createNode(2);
        n3 = createNode(3);
        n4 = createNode(4);
        n5 = createNode(5);
        n1.connectNode(n2);
        n1.connectNode(n3);
        n2.connectNode(n4);
        n3.connectNode(n4);
        n4.connectNode(n5);
        graph = new BuildGraph(n1);
    }

    public void testFindByBuildIdChild()
    {
        assertSame(n2, graph.findNodeByBuildId(2));
    }

    public void testFindByBuildIdDiamond()
    {
        assertSame(n4, graph.findNodeByBuildId(4));
    }

    public void testGetBuildPathChild()
    {
        assertEquals(singlePathSet(n2.getData()), graph.getBuildPaths(n2));
    }

    public void testGetBuildPathDiamond()
    {
        Set<BuildPath> expected = singlePathSet(n2.getData(), n4.getData());
        expected.add(new BuildPath(n3.getData(), n4.getData()));
        assertEquals(expected, graph.getBuildPaths(n4));
    }

    public Set<BuildPath> singlePathSet(BuildResult... builds)
    {
        HashSet<BuildPath> result = new HashSet<BuildPath>();
        result.add(new BuildPath(builds));
        return result;
    }

    public void testFindNodeByProjectsNoSuchProject()
    {
        assertNull(graph.findNodeByProjects(new BuildPath(createBuild(2), createBuild(99))));
    }

    public void testFindNodeByProjectsRoot()
    {
        // The root is not included in paths, and we should not find it.
        assertNull(graph.findNodeByProjects(new BuildPath(createBuild(1))));
    }

    public void testFindNodeByProjectsChild()
    {
        assertSame(n2, graph.findNodeByProjects(new BuildPath(createBuild(2))));
        assertSame(n3, graph.findNodeByProjects(new BuildPath(createBuild(3))));
    }

    public void testFindNodeByProjectsDiamond()
    {
        assertSame(n4, graph.findNodeByProjects(new BuildPath(createBuild(2), createBuild(4))));
        assertSame(n4, graph.findNodeByProjects(new BuildPath(createBuild(3), createBuild(4))));
    }

    public void testFindNodeByProjectsDiamondChild()
    {
        assertSame(n5, graph.findNodeByProjects(new BuildPath(createBuild(2), createBuild(4), createBuild(5))));
        assertSame(n5, graph.findNodeByProjects(new BuildPath(createBuild(3), createBuild(4), createBuild(5))));
    }

    private DAGraph.Node<BuildResult> createNode(long buildId)
    {
        return new DAGraph.Node<BuildResult>(createBuild(buildId));
    }

    private BuildResult createBuild(long buildId)
    {
        Project project = new Project();
        project.setId(100 + buildId);
        project.setConfig(new ProjectConfiguration("project-" + buildId));
        BuildResult buildResult = new BuildResult(new UnknownBuildReason(), project, 200 + buildId, false);
        buildResult.setId(buildId);
        return buildResult;
    }
}
