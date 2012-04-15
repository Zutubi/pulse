package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.test.EqualityAssertions;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.UnknownBuildReason;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.master.model.persistence.ProjectDao;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * See also {@link HibernateBuildResultDaoTest}.
 */
public class BuildQueryTest extends MasterPersistenceTestCase
{
    private BuildResultDao buildResultDao;
    private ProjectDao projectDao;
    private Project p1;
    private Project p2;
    private List<BuildResult> allResults;

    protected void setUp() throws Exception
    {
        super.setUp();
        buildResultDao = (BuildResultDao) context.getBean("buildResultDao");
        projectDao = (ProjectDao) context.getBean("projectDao");
        createProjects();
        setupBuilds();
    }

    private void createProjects()
    {
        p1 = new Project();
        projectDao.save(p1);

        p2 = new Project();
        projectDao.save(p2);
    }

    private void setupBuilds()
    {
        allResults = new LinkedList<BuildResult>();

        createBuild(p1, 1, ResultState.SUCCESS, 10000, 11000);
        createBuild(p2, 2, ResultState.SUCCESS, 11500, 11900);
        createBuild(p1, 3, ResultState.SUCCESS, 12000, 13000);
        createBuild(p2, 4, ResultState.SUCCESS, 13333, 13999);
        createBuild(p1, 5, ResultState.FAILURE, 14000, 15000);
        createBuild(p2, 6, ResultState.ERROR, 15000, 15821);
        createBuild(p1, 7, ResultState.SUCCESS, 16000, 17000);
        createBuild(p2, 8, ResultState.ERROR, 17777, 17777);
        createBuild(p1, 9, ResultState.ERROR, 18000, 19000);
        createBuild(p2, 10, ResultState.ERROR, 19005, 19006);
        createBuild(p1, 11, ResultState.ERROR, 20000, 21000);
        createBuild(p2, 12, ResultState.ERROR, 21100, 21900);
        createBuild(p1, 13, ResultState.SUCCESS, 22000, 23000);
        createBuild(p2, 14, ResultState.SUCCESS, 23332, 23880);
        createBuild(p1, 15, ResultState.SUCCESS, 24000, 25000);
        createBuild(p2, 16, ResultState.SUCCESS, 29001, 29999);
        createBuild(p1, 17, ResultState.FAILURE, 26000, 27000);
        createBuild(p2, 18, ResultState.FAILURE, 30000, 31000);
        createBuild(p1, 19, ResultState.SUCCESS, 28000, 29000);
        
        commitAndRefreshTransaction();
    }

    private void createBuild(Project project, int number, ResultState state, int start, int end)
    {
        BuildResult result = new BuildResult(new UnknownBuildReason(), project, number, false);
        result.commence(start);
        switch (state)
        {
            case FAILURE:
                result.failure();
                break;
            case ERROR:
                result.error("pfft");
                break;
        }

        result.complete();
        result.getStamps().setEndTime(end);
        buildResultDao.save(result);
        allResults.add(0, result);
    }

    public void testQueryAll()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, 0, 0, -1, -1, true);
        EqualityAssertions.assertEquals(allResults, results);
    }

    public void testAllOldestFirst()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, 0, 0, -1, -1, false);
        EqualityAssertions.assertEquals(getReversed(), results);
    }

    public void testProjectP1()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(new Project[]{ p1 }, null, 0, 0, -1, -1, true);
        EqualityAssertions.assertEquals(getFiltered(p1), results);
    }

    public void testProjectBoth()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(new Project[]{ p1, p2 }, null, 0, 0, -1, -1, true);
        EqualityAssertions.assertEquals(allResults, results);
    }

    public void testSuccessful()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, new ResultState[] { ResultState.SUCCESS } , 0, 0, -1, -1, true);
        EqualityAssertions.assertEquals(getFiltered(ResultState.SUCCESS), results);
    }

    public void testErrorOrFail()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, ResultState.getBrokenStates() , 0, 0, -1, -1, true);
        EqualityAssertions.assertEquals(getFiltered(ResultState.getBrokenStates()), results);
    }

    public void testEarliestTime()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, 15000, 0, -1, -1, true);
        EqualityAssertions.assertEquals(getFiltered(15000, 0), results);
    }

    public void testLatestTime()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, 0, 15000, -1, -1, true);
        EqualityAssertions.assertEquals(getFiltered(0, 15000), results);
    }

    public void testEarliestAndLatestTime()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, 15000, 15000, -1, -1, true);
        assertEquals(1, results.size());
        EqualityAssertions.assertEquals(getFiltered(15000, 15000), results);
    }

    public void testEarliestTimeBeyond()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, 150000, 0, -1, -1, true);
        assertEquals(0, results.size());
    }

    public void testLatestTimeBefore()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, 0, 1, -1, -1, true);
        assertEquals(0, results.size());
    }

    public void testPage()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, 0, 0, 2, 5, true);
        EqualityAssertions.assertEquals(getPaged(2, 5), results);
    }

    private List<BuildResult> getFiltered(final Project project)
    {
        return CollectionUtils.filter(allResults, new Predicate<BuildResult>() {
            public boolean satisfied(BuildResult t)
            {
                return t.getProject().equals(project);
            }
        });
    }

    private List<BuildResult> getFiltered(final ResultState state)
    {
        return CollectionUtils.filter(allResults, new Predicate<BuildResult>() {
            public boolean satisfied(BuildResult t)
            {
                return t.getState().equals(state);
            }
        });
    }

    private List<BuildResult> getFiltered(final ResultState... states)
    {
        return CollectionUtils.filter(allResults, new Predicate<BuildResult>() {
            public boolean satisfied(BuildResult t)
            {
                for(ResultState s: states)
                {
                    if(t.getState().equals(s))
                    {
                        return true;
                    }
                }

                return false;
            }
        });
    }

    private List<BuildResult> getFiltered(final int earliestStart, final int latestStart)
    {
        return CollectionUtils.filter(allResults, new Predicate<BuildResult>() {
            public boolean satisfied(BuildResult t)
            {
                if(earliestStart > 0 && t.getStamps().getStartTime() < earliestStart)
                {
                    return false;
                }

                if(latestStart > 0 && t.getStamps().getStartTime() > latestStart)
                {
                    return false;
                }

                return true;
            }
        });
    }

    private List<BuildResult> getPaged(int first, int max)
    {
        List<BuildResult> results = new LinkedList<BuildResult>();
        for(int i = first; i < first + max && i < allResults.size(); i++)
        {
            results.add(allResults.get(i));
        }

        return results;
    }

    private List<BuildResult> getReversed()
    {
        List<BuildResult> copy = new LinkedList<BuildResult>(allResults);
        Collections.reverse(copy);
        return copy;
    }
}
