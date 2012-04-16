package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.UnaryProcedure;
import static java.util.Arrays.asList;

import java.util.*;

public class BuildGraphTest extends PulseTestCase
{
    // The graph looks like:
    //     2
    // 1 <   > 4 - 5
    //     3
    private BuildGraph graph;
    private BuildGraph.Node n1;
    private BuildGraph.Node n2;
    private BuildGraph.Node n3;
    private BuildGraph.Node n4;
    private BuildGraph.Node n5;

    @Override
    protected void setUp() throws Exception
    {
        n1 = createNode(1);
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

    public void testFindByBuildIdNotFound()
    {
        assertNull(graph.findNodeByBuildId(99));
    }

    public void testFindByBuildIdRoot()
    {
        assertSame(n1, graph.findNodeByBuildId(1));
    }

    public void testFindByBuildIdChild()
    {
        assertSame(n2, graph.findNodeByBuildId(2));
    }

    public void testFindByBuildIdDiamond()
    {
        assertSame(n4, graph.findNodeByBuildId(4));
    }

    public void testFindByBuildIdDiamondChild()
    {
        assertSame(n5, graph.findNodeByBuildId(5));
    }

    public void testGetBuildPathsUnknownNode()
    {
        assertEquals(0, graph.getBuildPaths(createNode(99)).size());
    }

    public void testGetBuildPathsRoot()
    {
        assertEquals(singlePathSet(), graph.getBuildPaths(n1));
    }

    public void testGetBuildPathChild()
    {
        assertEquals(singlePathSet(n2.getBuild()), graph.getBuildPaths(n2));
    }

    public void testGetBuildPathDiamond()
    {
        Set<List<BuildResult>> expected = singlePathSet(n2.getBuild(), n4.getBuild());
        expected.add(asList(n3.getBuild(), n4.getBuild()));
        assertEquals(expected, graph.getBuildPaths(n4));
    }

    public void testGetBuildPathDiamondChild()
    {
        Set<List<BuildResult>> expected = singlePathSet(n2.getBuild(), n4.getBuild(), n5.getBuild());
        expected.add(asList(n3.getBuild(), n4.getBuild(), n5.getBuild()));
        assertEquals(expected, graph.getBuildPaths(n5));
    }

    public Set<List<BuildResult>> singlePathSet(BuildResult... builds)
    {
        HashSet<List<BuildResult>> result = new HashSet<List<BuildResult>>();
        result.add(asList(builds));
        return result;
    }

    public void testFindNodeByProjectsNoSuchProject()
    {
        assertNull(graph.findNodeByProjects(asList(createBuild(2), createBuild(99))));
    }

    public void testFindNodeByProjectsRoot()
    {
        // The root is not included in paths, and we should not find it.
        assertNull(graph.findNodeByProjects(asList(createBuild(1))));
    }

    public void testFindNodeByProjectsChild()
    {
        assertSame(n2, graph.findNodeByProjects(asList(createBuild(2))));
        assertSame(n3, graph.findNodeByProjects(asList(createBuild(3))));
    }

    public void testFindNodeByProjectsDiamond()
    {
        assertSame(n4, graph.findNodeByProjects(asList(createBuild(2), createBuild(4))));
        assertSame(n4, graph.findNodeByProjects(asList(createBuild(3), createBuild(4))));
    }

    public void testFindNodeByProjectsDiamondChild()
    {
        assertSame(n5, graph.findNodeByProjects(asList(createBuild(2), createBuild(4), createBuild(5))));
        assertSame(n5, graph.findNodeByProjects(asList(createBuild(3), createBuild(4), createBuild(5))));
    }

    public void testForEach()
    {
        final List<BuildGraph.Node> traversalOrder = new LinkedList<BuildGraph.Node>();
        graph.forEach(new UnaryProcedure<BuildGraph.Node>()
        {
            public void run(BuildGraph.Node node)
            {
                traversalOrder.add(node);
            }
        });
        
        // The order of children is arbitrary for each node, so we have two possible orders
        assertTrue(traversalOrder.equals(asList(n1, n2, n4, n5, n3)) || traversalOrder.equals(asList(n1, n3, n4, n5, n2)));
    }
    
    private BuildGraph.Node createNode(long buildId)
    {
        return new BuildGraph.Node(createBuild(buildId));
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
