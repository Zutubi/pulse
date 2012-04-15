package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.EntityWithIdPredicate;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.persistence.BuildDependencyLinkDao;
import com.zutubi.pulse.master.model.persistence.InMemoryEntityDao;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.DisjunctivePredicate;
import com.zutubi.util.Predicate;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class DefaultDependencyManagerTest extends PulseTestCase
{
    private DefaultDependencyManager dependencyManager;
    private InMemoryBuildDependencyLinkDao buildDependencyLinkDao;

    private List<BuildResult> builds = new LinkedList<BuildResult>();
    private BuildResult build1;
    private BuildResult build2;
    private BuildResult build3;
    private BuildResult build4;

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

        dependencyManager.setBuildDependencyLinkDao(buildDependencyLinkDao);
        dependencyManager.setBuildManager(buildManager);

        build1 = createBuild(1);
        build2 = createBuild(2);
        build3 = createBuild(3);
        build4 = createBuild(4);
    }

    public void testUpstreamGraphTrivial()
    {
        // 1
        BuildGraph expected = new BuildGraph(new BuildGraph.Node(build1));
        assertEquals(expected, dependencyManager.getUpstreamDependencyGraph(build1));
    }

    public void testUpstreamGraphSingleUpstream()
    {
        // 1 - 2
        link(build1, build2);

        BuildGraph.Node root = new BuildGraph.Node(build2);
        root.connectNode(new BuildGraph.Node(build1));
        BuildGraph expected = new BuildGraph(root);

        assertEquals(expected, dependencyManager.getUpstreamDependencyGraph(build2));
    }

    public void testUpstreamGraphMultipleUpstream()
    {
        // 1
        //   > 3
        // 2
        link(build1, build3);
        link(build2, build3);

        BuildGraph.Node root = new BuildGraph.Node(build3);
        root.connectNode(new BuildGraph.Node(build1));
        root.connectNode(new BuildGraph.Node(build2));
        BuildGraph expected = new BuildGraph(root);

        assertEquals(expected, dependencyManager.getUpstreamDependencyGraph(build3));
    }

    public void testUpstreamGraphDiamond()
    {
        //     2
        // 1 <   > 4
        //     3
        link(build1, build2);
        link(build1, build3);
        link(build2, build4);
        link(build3, build4);

        BuildGraph.Node n1 = new BuildGraph.Node(build1);
        BuildGraph.Node n2 = new BuildGraph.Node(build2);
        BuildGraph.Node n3 = new BuildGraph.Node(build3);
        BuildGraph.Node n4 = new BuildGraph.Node(build4);
        n2.connectNode(n1);
        n3.connectNode(n1);
        n4.connectNode(n2);
        n4.connectNode(n3);

        BuildGraph expected = new BuildGraph(n4);

        assertEquals(expected, dependencyManager.getUpstreamDependencyGraph(build4));
    }

    public BuildResult createBuild(long id)
    {
        BuildResult build = new BuildResult(new UnknownBuildReason(), new Project(), 1, false);
        build.setId(id);
        build.complete();
        builds.add(build);
        return build;
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
