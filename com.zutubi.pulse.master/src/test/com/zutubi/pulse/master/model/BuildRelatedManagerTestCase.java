package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.model.PersistentFileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.persistence.ChangelistDao;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

/**
 * Helper base class for testing managers that deal with builds/changes/dependencies.  Sets up some
 * in-memory daos, projects and builds and wires things together.
 */
public abstract class BuildRelatedManagerTestCase extends PulseTestCase
{
    protected DefaultBuildManager buildManager;
    protected DefaultChangelistManager changelistManager;
    protected DefaultDependencyManager dependencyManager;
    private InMemoryBuildDependencyLinkDao buildDependencyLinkDao;

    protected Project project1;
    protected Project project2;
    protected Project project3;
    protected Project project4;
    protected BuildResult build1_1;
    protected BuildResult build2_1;
    protected BuildResult build3_1;
    protected BuildResult build4_1;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        buildManager = new DefaultBuildManager();
        changelistManager = new DefaultChangelistManager();
        dependencyManager = new DefaultDependencyManager();
        buildDependencyLinkDao = new InMemoryBuildDependencyLinkDao();
        ChangelistDao changelistDao = mock(ChangelistDao.class);
        stub(changelistDao.findByResult(anyLong(), anyBoolean())).toAnswer(new Answer<List<PersistentChangelist>>()
        {
            public List<PersistentChangelist> answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Long buildId = (Long) invocationOnMock.getArguments()[0];
                BuildResult build = buildManager.getBuildResult(buildId);
                return asList(createChangelist(build.getProject(), build.getNumber()));
            }
        });

        buildManager.setBuildResultDao(new InMemoryBuildResultDao());
        buildManager.setChangelistDao(changelistDao);

        changelistManager.setChangelistDao(changelistDao);
        changelistManager.setBuildManager(buildManager);
        changelistManager.setDependencyManager(dependencyManager);

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

    protected Project createProject(long id)
    {
        Project project = new Project();
        project.setId(id);
        project.setConfig(new ProjectConfiguration("project-" + id));
        return project;
    }

    protected BuildResult createBuild(Project project, long number)
    {
        BuildResult build = new BuildResult(new UnknownBuildReason(), project, number, false);
        build.complete();
        buildManager.save(build);
        return build;
    }

    protected PersistentChangelist createChangelist(Project project, long buildNumber)
    {
        // Changes are created in a particular manner so we can map them back to the builds they
        // came from for later verification.  Ids sort by project then number, also used as time
        // stamps.
        long id = project.getId() * 10000 + buildNumber;
        PersistentChangelist changelist = new PersistentChangelist(new Revision(buildNumber), id, project.getName(), "", Collections.<PersistentFileChange>emptyList());
        changelist.setId(id);
        return changelist;
    }

    protected void link(BuildResult upstream, BuildResult downstream)
    {
        BuildDependencyLink link = new BuildDependencyLink(upstream.getId(), downstream.getId());
        buildDependencyLinkDao.save(link);
    }
}
