package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.UnknownBuildReason;
import com.zutubi.pulse.model.persistence.BuildResultDao;
import com.zutubi.pulse.model.persistence.BuildSpecificationDao;
import com.zutubi.pulse.model.persistence.ProjectDao;
import com.zutubi.pulse.util.ListUtils;
import com.zutubi.pulse.util.Predicate;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class BuildQueryTest extends MasterPersistenceTestCase
{
    private BuildResultDao buildResultDao;
    private ProjectDao projectDao;
    private BuildSpecificationDao buildSpecificationDao;
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
        p1 = new Project("p1", "p1 description");
        projectDao.save(p1);
        p2 = new Project("p2", "p2 description");
        projectDao.save(p2);
    }

    private void setupBuilds()
    {
        allResults = new LinkedList<BuildResult>();

        createBuild(p1, "default", 1, ResultState.SUCCESS, 10000, 11000, false);
        createBuild(p2, "default", 2, ResultState.SUCCESS, 11500, 11900);
        createBuild(p1, "overnight", 3, ResultState.SUCCESS, 12000, 13000, false);
        createBuild(p2, "overnight", 4, ResultState.SUCCESS, 13333, 13999);
        createBuild(p1, "default", 5, ResultState.FAILURE, 14000, 15000);
        createBuild(p2, "borken", 6, ResultState.ERROR, 15000, 15821);
        createBuild(p1, "default", 7, ResultState.SUCCESS, 16000, 17000);
        createBuild(p2, "borken", 8, ResultState.ERROR, 17777, 17777);
        createBuild(p1, "default", 9, ResultState.ERROR, 18000, 19000);
        createBuild(p2, "borken", 10, ResultState.ERROR, 19005, 19006);
        createBuild(p1, "overnight", 11, ResultState.ERROR, 20000, 21000, false);
        createBuild(p2, "borken", 12, ResultState.ERROR, 21100, 21900);
        createBuild(p1, "default", 13, ResultState.SUCCESS, 22000, 23000);
        createBuild(p2, "default", 14, ResultState.SUCCESS, 23332, 23880);
        createBuild(p1, "default", 15, ResultState.SUCCESS, 24000, 25000);
        createBuild(p2, "default", 16, ResultState.SUCCESS, 29001, 29999);
        createBuild(p1, "overnight", 17, ResultState.FAILURE, 26000, 27000);
        createBuild(p2, "overnight", 18, ResultState.FAILURE, 30000, 31000);
        createBuild(p1, "default", 19, ResultState.SUCCESS, 28000, 29000);
    }

    private void createBuild(Project project, String spec, int number, ResultState state, int start, int end)
    {
        createBuild(project, spec, number, state, start, end, true);
    }

    private void createBuild(Project project, String spec, int number, ResultState state, int start, int end, boolean hasWorkDir)
    {
        BuildResult result = new BuildResult(new UnknownBuildReason(), project, spec, number);
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
        result.setHasWorkDir(hasWorkDir);
        buildResultDao.save(result);
        allResults.add(0, result);
    }

    public void tearDown() throws Exception
    {
        projectDao = null;
        buildResultDao = null;
        p1 = null;
        p2 = null;
        allResults = null;

        try
        {
            super.tearDown();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void testAllSpecs()
    {
        List<String> specs = buildResultDao.findAllSpecificationsForProjects(null);
        assertEquals(getStringList("borken", "default", "overnight"), specs);
    }

    public void testAllSpecsForP1()
    {
        List<String> specs = buildResultDao.findAllSpecificationsForProjects(new Project[] { p1 });
        assertEquals(getStringList("default", "overnight"), specs);
    }

    public void testAllSpecsForBoth()
    {
        List<String> specs = buildResultDao.findAllSpecificationsForProjects(new Project[] { p1, p2 });
        assertEquals(getStringList("borken", "default", "overnight"), specs);
    }

    public void testQueryAll()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, null, 0, 0, null, -1, -1, true);
        assertEquals(allResults, results);
    }

    public void testAllOldestFirst()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, null, 0, 0, null, -1, -1, false);
        assertEquals(getReversed(), results);
    }

    public void testProjectP1()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(new Project[]{ p1 }, null, null, 0, 0, null, -1, -1, true);
        assertEquals(getFiltered(p1), results);
    }

    public void testProjectBoth()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(new Project[]{ p1, p2 }, null, null, 0, 0, null, -1, -1, true);
        assertEquals(allResults, results);
    }

    public void testSuccessful()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, new ResultState[] { ResultState.SUCCESS } , null, 0, 0, null, -1, -1, true);
        assertEquals(getFiltered(ResultState.SUCCESS), results);
    }

    public void testErrorOrFail()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, new ResultState[] { ResultState.ERROR, ResultState.FAILURE } , null, 0, 0, null, -1, -1, true);
        assertEquals(getFiltered(ResultState.ERROR, ResultState.FAILURE), results);
    }

    public void testSpecDefault()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, new String[] { "default" }, 0, 0, null, -1, -1, true);
        assertEquals(getFiltered("default"), results);
    }

    public void testSpecOvernight()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, new String[] { "overnight" }, 0, 0, null, -1, -1, true);
        assertEquals(getFiltered("overnight"), results);
    }

    public void testProjectP1SpecDefault()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(new Project[] { p1 }, null, new String[] { "default" }, 0, 0, null, -1, -1, true);
        assertEquals(getFiltered(p1, "default"), results);
    }

    public void testEarliestTime()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, null, 15000, 0, null, -1, -1, true);
        assertEquals(getFiltered(15000, 0), results);
    }

    public void testLatestTime()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, null, 0, 15000, null, -1, -1, true);
        assertEquals(getFiltered(0, 15000), results);
    }

    public void testEarliestAndLatestTime()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, null, 15000, 15000, null, -1, -1, true);
        assertEquals(1, results.size());
        assertEquals(getFiltered(15000, 15000), results);
    }

    public void testEarliestTimeBeyond()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, null, 150000, 0, null, -1, -1, true);
        assertEquals(0, results.size());
    }

    public void testLatestTimeBefore()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, null, 0, 1, null, -1, -1, true);
        assertEquals(0, results.size());
    }

    public void testHasWorkDir()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, null, 0, 0, true, -1, -1, true);
        assertEquals(getFiltered(true), results);
    }

    public void testHasNoWorkDir()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, null, 0, 0, false, -1, -1, true);
        assertEquals(getFiltered(false), results);
    }

    public void testPage()
    {
        List<BuildResult> results = buildResultDao.queryBuilds(null, null, null, 0, 0, null, 2, 5, true);
        assertEquals(getPaged(2, 5), results);
    }

    private List<BuildResult> getFiltered(final Project project)
    {
        return new ListUtils<BuildResult>().filter(allResults, new Predicate<BuildResult>() {
            public boolean satisfied(BuildResult t)
            {
                return t.getProject().equals(project);
            }
        });
    }

    private List<BuildResult> getFiltered(final ResultState state)
    {
        return new ListUtils<BuildResult>().filter(allResults, new Predicate<BuildResult>() {
            public boolean satisfied(BuildResult t)
            {
                return t.getState().equals(state);
            }
        });
    }

    private List<BuildResult> getFiltered(final ResultState... states)
    {
        return new ListUtils<BuildResult>().filter(allResults, new Predicate<BuildResult>() {
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

    private List<BuildResult> getFiltered(final String spec)
    {
        return new ListUtils<BuildResult>().filter(allResults, new Predicate<BuildResult>() {
            public boolean satisfied(BuildResult t)
            {
                return t.getBuildSpecification().equals(spec);
            }
        });
    }

    private List<BuildResult> getFiltered(final Project project, final String spec)
    {
        return new ListUtils<BuildResult>().filter(allResults, new Predicate<BuildResult>() {
            public boolean satisfied(BuildResult t)
            {
                return t.getProject().equals(project) && t.getBuildSpecification().equals(spec);
            }
        });
    }

    private List<BuildResult> getFiltered(final int earliestStart, final int latestStart)
    {
        return new ListUtils<BuildResult>().filter(allResults, new Predicate<BuildResult>() {
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

    private List<BuildResult> getFiltered(final boolean hasWorkDir)
    {
        return new ListUtils<BuildResult>().filter(allResults, new Predicate<BuildResult>() {
            public boolean satisfied(BuildResult t)
            {
                return t.getHasWorkDir() == hasWorkDir;
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

    List<String> getStringList(String ...strings)
    {
        return Arrays.asList(strings);
    }
}
