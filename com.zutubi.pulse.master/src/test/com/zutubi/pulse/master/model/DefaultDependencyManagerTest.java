package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.ChangelistComparator;
import com.zutubi.pulse.core.model.EntityWithIdPredicate;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.model.PersistentFileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.persistence.BuildDependencyLinkDao;
import com.zutubi.pulse.master.model.persistence.InMemoryEntityDao;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.DisjunctivePredicate;
import com.zutubi.util.Predicate;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

public class DefaultDependencyManagerTest extends PulseTestCase
{
    private DefaultDependencyManager dependencyManager;
    private InMemoryBuildDependencyLinkDao buildDependencyLinkDao;

    private Project project1;
    private Project project2;
    private Project project3;
    private Project project4;
    private List<BuildResult> builds = new LinkedList<BuildResult>();
    private BuildResult build1_1;
    private BuildResult build2_1;
    private BuildResult build3_1;
    private BuildResult build4_1;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        dependencyManager = new DefaultDependencyManager();
        buildDependencyLinkDao = new InMemoryBuildDependencyLinkDao();
        BuildManager buildManager = mock(BuildManager.class);
        stub(buildManager.getBuildResult(anyLong())).toAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return CollectionUtils.find(builds, new EntityWithIdPredicate<BuildResult>((Long) invocationOnMock.getArguments()[0]));
            }
        });
        
        stub(buildManager.getChangesForBuild(Mockito.<BuildResult>anyObject(), anyLong(), anyBoolean())).toAnswer(new Answer<List<PersistentChangelist>>()
        {
            public List<PersistentChangelist> answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                BuildResult build = (BuildResult) invocationOnMock.getArguments()[0];
                Long since = (Long) invocationOnMock.getArguments()[1];
                List<PersistentChangelist> result = new LinkedList<PersistentChangelist>();
                if (since > 0)
                {
                    for (long number = since + 1; number < build.getNumber(); number++)
                    {
                        result.add(createChangelist(build.getProject(), number));
                    }
                }
                
                result.add(createChangelist(build.getProject(), build.getNumber()));
                return result; 
            }
        });

        dependencyManager.setBuildDependencyLinkDao(buildDependencyLinkDao);
        dependencyManager.setBuildManager(buildManager);

        project1 = createProject(1);
        project2 = createProject(2);
        project3 = createProject(3);
        project4 = createProject(4);

        build1_1 = createBuild(project1, 1);
        build2_1 = createBuild(project2, 1);
        build3_1 = createBuild(project3, 1);
        build4_1 = createBuild(project4, 1);
    }

    public void testUpstreamGraphTrivial()
    {
        // 1_1
        BuildGraph expected = new BuildGraph(new BuildGraph.Node(build1_1));
        assertEquals(expected, dependencyManager.getUpstreamDependencyGraph(build1_1));
    }

    public void testUpstreamGraphSingleUpstream()
    {
        // 1_1 - 2_1
        link(build1_1, build2_1);

        BuildGraph.Node root = new BuildGraph.Node(build2_1);
        root.connectNode(new BuildGraph.Node(build1_1));
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

        BuildGraph.Node root = new BuildGraph.Node(build3_1);
        root.connectNode(new BuildGraph.Node(build1_1));
        root.connectNode(new BuildGraph.Node(build2_1));
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

        BuildGraph.Node n1 = new BuildGraph.Node(build1_1);
        BuildGraph.Node n2 = new BuildGraph.Node(build2_1);
        BuildGraph.Node n3 = new BuildGraph.Node(build3_1);
        BuildGraph.Node n4 = new BuildGraph.Node(build4_1);
        n2.connectNode(n1);
        n3.connectNode(n1);
        n4.connectNode(n2);
        n4.connectNode(n3);

        BuildGraph expected = new BuildGraph(n4);

        assertEquals(expected, dependencyManager.getUpstreamDependencyGraph(build4_1));
    }

    public void testGetUpstreamChangesNoUpstreamBuilds()
    {
        BuildResult build1_2 = createBuild(project1, 2);

        assertEquals(0, dependencyManager.getUpstreamChangelists(build1_2, build1_1).size());
    }

    public void testGetUpstreamChangesNoSinceBuild()
    {
        assertEquals(0, dependencyManager.getUpstreamChangelists(build1_1, null).size());
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

        assertEquals(0, dependencyManager.getUpstreamChangelists(build1_2, build1_1).size());
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

        assertEquals(0, dependencyManager.getUpstreamChangelists(build1_2, build1_1).size());
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

        List<UpstreamChangelist> expected = asList(new UpstreamChangelist(createChangelist(project2, 2), asList(build2_2)));
        assertEquals(expected, dependencyManager.getUpstreamChangelists(build1_2, build1_1));
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

        assertEquals(0, dependencyManager.getUpstreamChangelists(build1_2, build1_1).size());
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
        BuildGraph.Node node4_2 = upstream1_2.findNodeByBuildId(build4_2.getId());
        Iterator<List<BuildResult>> pathsIt = upstream1_2.getBuildPaths(node4_2).iterator();
        UpstreamChangelist change4 = new UpstreamChangelist(createChangelist(project4, 2), pathsIt.next());
        change4.addUpstreamContext(pathsIt.next());
        
        List<UpstreamChangelist> expected = asList(
                change4,
                new UpstreamChangelist(createChangelist(project3, 2), asList(build3_2)),
                new UpstreamChangelist(createChangelist(project2, 2), asList(build2_2))
        );

        List<UpstreamChangelist> got = dependencyManager.getUpstreamChangelists(build1_2, build1_1);
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

    private Project createProject(long id)
    {
        Project project = new Project();
        project.setId(id);
        project.setConfig(new ProjectConfiguration("project-" + id));
        return project;
    }

    public BuildResult createBuild(Project project, long number)
    {
        BuildResult build = new BuildResult(new UnknownBuildReason(), project, number, false);
        build.setId(project.getId() * 100 + number);
        build.complete();
        builds.add(build);
        return build;
    }

    private PersistentChangelist createChangelist(Project project, long buildNumber)
    {
        // Changes are created in a particular manner so we can map them back to the builds they
        // came from for later verification.  Ids sort by project then number, also used as time
        // stamps.
        long id = project.getId() * 10000 + buildNumber;
        PersistentChangelist changelist = new PersistentChangelist(new Revision(buildNumber), id, project.getName(), "", Collections.<PersistentFileChange>emptyList());
        changelist.setId(id);
        return changelist;
    }

    public void link(BuildResult upstream, BuildResult downstream)
    {
        BuildDependencyLink link = new BuildDependencyLink(upstream.getId(), downstream.getId());
        buildDependencyLinkDao.save(link);
    }

    public static class InMemoryBuildDependencyLinkDao extends InMemoryEntityDao<BuildDependencyLink> implements BuildDependencyLinkDao
    {
        public List<BuildDependencyLink> findAllDependencies(long buildId)
        {
            return findByPredicate(new HasId(buildId));
        }

        public List<BuildDependencyLink> findAllUpstreamDependencies(long buildId)
        {
            return findByPredicate(new HasDownstreamId(buildId));
        }

        public List<BuildDependencyLink> findAllDownstreamDependencies(long buildId)
        {
            return findByPredicate(new HasUpstreamId(buildId));

        }

        public int deleteDependenciesByBuild(long buildId)
        {
            return deleteByPredicate(new HasId(buildId));
        }
    }

    public static class HasDownstreamId implements Predicate<BuildDependencyLink>
    {
        private long buildId;

        public HasDownstreamId(long buildId)
        {
            this.buildId = buildId;
        }

        public boolean satisfied(BuildDependencyLink buildDependencyLink)
        {
            return buildDependencyLink.getDownstreamBuildId() == buildId;
        }
    }

    public static class HasUpstreamId implements Predicate<BuildDependencyLink>
    {
        private long buildId;

        public HasUpstreamId(long buildId)
        {
            this.buildId = buildId;
        }

        public boolean satisfied(BuildDependencyLink buildDependencyLink)
        {
            return buildDependencyLink.getUpstreamBuildId() == buildId;
        }
    }

    public static class HasId extends DisjunctivePredicate<BuildDependencyLink>
    {
        public HasId(long buildId)
        {
            super(new HasDownstreamId(buildId), new HasUpstreamId(buildId));
        }
    }
}
