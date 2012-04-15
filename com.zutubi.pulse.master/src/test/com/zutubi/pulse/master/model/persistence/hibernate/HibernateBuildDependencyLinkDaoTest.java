package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.BuildDependencyLink;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.UnknownBuildReason;
import com.zutubi.pulse.master.model.persistence.BuildDependencyLinkDao;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.master.model.persistence.ProjectDao;

import java.util.List;

public class HibernateBuildDependencyLinkDaoTest extends MasterPersistenceTestCase
{
    private BuildResultDao buildResultDao;
    private BuildDependencyLinkDao buildDependencyLinkDao;
    private ProjectDao projectDao;

    protected void setUp() throws Exception
    {
        super.setUp();
        buildResultDao = (BuildResultDao) context.getBean("buildResultDao");
        buildDependencyLinkDao = (BuildDependencyLinkDao) context.getBean("buildDependencyLinkDao");
        projectDao = (ProjectDao) context.getBean("projectDao");
    }

    public void testFindDependencyLinks()
    {
        Project p1 = new Project();
        Project p2 = new Project();
        Project p3 = new Project();
        projectDao.save(p1);
        projectDao.save(p2);
        projectDao.save(p3);

        BuildResult build1_1 = createCompletedBuild(p1, 1);
        BuildResult build2_1 = createCompletedBuild(p2, 1);
        BuildResult build2_2 = createCompletedBuild(p2, 2);
        BuildResult build3_1 = createCompletedBuild(p3, 1);

        buildResultDao.save(build1_1);
        buildResultDao.save(build2_1);
        buildResultDao.save(build3_1);
        buildResultDao.save(build2_2);

        buildDependencyLinkDao.save(new BuildDependencyLink(build1_1.getId(), build2_1.getId()));
        buildDependencyLinkDao.save(new BuildDependencyLink(build1_1.getId(), build2_2.getId()));
        buildDependencyLinkDao.save(new BuildDependencyLink(build2_1.getId(), build3_1.getId()));

        commitAndRefreshTransaction();

        List<BuildDependencyLink> results = buildDependencyLinkDao.findAllDependencies(build1_1.getId());
        assertEquals(2, results.size());

        results = buildDependencyLinkDao.findAllUpstreamDependencies(build1_1.getId());
        assertEquals(0, results.size());

        results = buildDependencyLinkDao.findAllDownstreamDependencies(build1_1.getId());
        assertEquals(2, results.size());

        results = buildDependencyLinkDao.findAllDependencies(build2_1.getId());
        assertEquals(2, results.size());
        BuildDependencyLink link = results.get(0);
        if (link.getUpstreamBuildId() == build2_1.getId())
        {
            assertEquals(build3_1.getId(), link.getDownstreamBuildId());
            link = results.get(1);
            assertEquals(build1_1.getId(), link.getUpstreamBuildId());
            assertEquals(build2_1.getId(), link.getDownstreamBuildId());
        }
        else
        {
            assertEquals(build1_1.getId(), link.getUpstreamBuildId());
            assertEquals(build2_1.getId(), link.getDownstreamBuildId());
            link = results.get(1);
            assertEquals(build2_1.getId(), link.getUpstreamBuildId());
            assertEquals(build3_1.getId(), link.getDownstreamBuildId());
        }

        results = buildDependencyLinkDao.findAllUpstreamDependencies(build2_1.getId());
        assertEquals(1, results.size());
        link = results.get(0);
        assertEquals(build1_1.getId(), link.getUpstreamBuildId());
        assertEquals(build2_1.getId(), link.getDownstreamBuildId());

        results = buildDependencyLinkDao.findAllDownstreamDependencies(build2_1.getId());
        assertEquals(1, results.size());
        link = results.get(0);
        assertEquals(build2_1.getId(), link.getUpstreamBuildId());
        assertEquals(build3_1.getId(), link.getDownstreamBuildId());
    }

    public void testDeleteDependencyLinks()
    {
        Project p1 = new Project();
        Project p2 = new Project();
        Project p3 = new Project();
        projectDao.save(p1);
        projectDao.save(p2);
        projectDao.save(p3);

        BuildResult build1_1 = createCompletedBuild(p1, 1);
        BuildResult build2_1 = createCompletedBuild(p2, 1);
        BuildResult build2_2 = createCompletedBuild(p2, 2);
        BuildResult build3_1 = createCompletedBuild(p3, 1);

        buildResultDao.save(build1_1);
        buildResultDao.save(build2_1);
        buildResultDao.save(build3_1);
        buildResultDao.save(build2_2);

        buildDependencyLinkDao.save(new BuildDependencyLink(build1_1.getId(), build2_1.getId()));
        buildDependencyLinkDao.save(new BuildDependencyLink(build1_1.getId(), build2_2.getId()));
        buildDependencyLinkDao.save(new BuildDependencyLink(build2_1.getId(), build3_1.getId()));

        commitAndRefreshTransaction();

        assertEquals(2, buildDependencyLinkDao.deleteDependenciesByBuild(build1_1.getId()));
        assertEquals(0, buildDependencyLinkDao.findAllDependencies(build1_1.getId()).size());

        assertEquals(0, buildDependencyLinkDao.deleteDependenciesByBuild(build2_2.getId()));

        assertEquals(1, buildDependencyLinkDao.deleteDependenciesByBuild(build3_1.getId()));
        assertEquals(0, buildDependencyLinkDao.findAllDependencies(build3_1.getId()).size());
    }

    private BuildResult createCompletedBuild(Project project, long number)
    {
        BuildResult result = new BuildResult(new UnknownBuildReason(), project, number, false);
        result.commence();
        result.complete();
        return result;
    }
}
