package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.ChangelistComparator;
import com.zutubi.util.adt.DAGraph;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;

public class DefaultChangelistManagerTest extends BuildRelatedManagerTestCase
{
    public void testGetUpstreamChangesNoUpstreamBuilds()
    {
        BuildResult build1_2 = createBuild(project1, 2);

        assertEquals(0, changelistManager.getUpstreamChangelists(build1_2, build1_1).size());
    }

    public void testGetUpstreamChangesNoSinceBuild()
    {
        assertEquals(0, changelistManager.getUpstreamChangelists(build1_1, null).size());
    }

    public void testGetUpstreamChangesFirstUpstreamBuild()
    {
        // Since build graph:
        // 1_1
        //
        // This build graph
        // 2_1 - 1_2
        BuildResult build1_2 = createBuild(project1, 2);
        link(build2_1, build1_2);

        assertEquals(0, changelistManager.getUpstreamChangelists(build1_2, build1_1).size());
    }

    public void testGetUpstreamChangesSameUpstreamBuild()
    {
        // Since build graph:
        // 2_1 - 1_1
        //
        // This build graph
        // 2_1 - 1_2
        BuildResult build1_2 = createBuild(project1, 2);
        link(build2_1, build1_1);
        link(build2_1, build1_2);

        assertEquals(0, changelistManager.getUpstreamChangelists(build1_2, build1_1).size());
    }

    public void testGetUpstreamChangesNewUpstreamBuild()
    {
        // Since build graph:
        // 2_1 - 1_1
        //
        // This build graph
        // 2_2 - 1_2
        BuildResult build1_2 = createBuild(project1, 2);
        BuildResult build2_2 = createBuild(project2, 2);
        link(build2_1, build1_1);
        link(build2_2, build1_2);

        List<UpstreamChangelist> expected = asList(new UpstreamChangelist(createChangelist(project2, 2), new BuildPath(build2_2)));
        assertEquals(expected, changelistManager.getUpstreamChangelists(build1_2, build1_1));
    }

    public void testGetUpstreamChangesNewUpstreamBuildViaDifferentPath()
    {
        // Since build graph:
        // 2_1 - 1_1
        //
        // This build graph
        // 2_2 - 3_1 - 1_2
        BuildResult build1_2 = createBuild(project1, 2);
        BuildResult build2_2 = createBuild(project2, 2);
        link(build2_1, build1_1);
        link(build2_2, build3_1);
        link(build3_1, build1_2);

        assertEquals(0, changelistManager.getUpstreamChangelists(build1_2, build1_1).size());
    }

    public void testGetUpstreamChangesDiamond()
    {
        // Since build graph:
        //       2_1
        // 4_1 <     > 1_1
        //       3_1
        //
        // This build graph:
        //       2_2
        // 4_2 <     > 1_2
        //       3_2
        BuildResult build1_2 = createBuild(project1, 2);
        BuildResult build2_2 = createBuild(project2, 2);
        BuildResult build3_2 = createBuild(project3, 2);
        BuildResult build4_2 = createBuild(project4, 2);
        link(build4_1, build2_1);
        link(build2_1, build1_1);
        link(build4_1, build3_1);
        link(build3_1, build1_1);
        link(build4_2, build2_2);
        link(build2_2, build1_2);
        link(build4_2, build3_2);
        link(build3_2, build1_2);

        // The change to project4 is reachable via two paths, we need to get them in order.
        BuildGraph upstream1_2 = dependencyManager.getUpstreamDependencyGraph(build1_2);
        DAGraph.Node<BuildResult> node4_2 = upstream1_2.findNodeByBuildId(build4_2.getId());
        Iterator<BuildPath> pathsIt = upstream1_2.getBuildPaths(node4_2).iterator();
        UpstreamChangelist change4 = new UpstreamChangelist(createChangelist(project4, 2), pathsIt.next());
        change4.addUpstreamContext(pathsIt.next());

        List<UpstreamChangelist> expected = asList(
                change4,
                new UpstreamChangelist(createChangelist(project3, 2), new BuildPath(build3_2)),
                new UpstreamChangelist(createChangelist(project2, 2), new BuildPath(build2_2))
        );

        List<UpstreamChangelist> got = changelistManager.getUpstreamChangelists(build1_2, build1_1);
        sortChangelists(got);
        assertEquals(expected, got);
    }

    private void sortChangelists(List<UpstreamChangelist> changelists)
    {
        final ChangelistComparator changelistComparator = new ChangelistComparator();
        Collections.sort(changelists, new Comparator<UpstreamChangelist>()
        {
            public int compare(UpstreamChangelist o1, UpstreamChangelist o2)
            {
                return changelistComparator.compare(o1.getChangelist(), o2.getChangelist());
            }
        });
    }
}
