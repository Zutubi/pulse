/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.core.util.Constants;
import com.zutubi.pulse.model.persistence.BuildResultDao;
import com.zutubi.pulse.model.persistence.ProjectDao;
import com.zutubi.pulse.model.persistence.hibernate.MasterPersistenceTestCase;

import java.util.Arrays;
import java.util.List;

/**
 */
public class CleanupRuleTest extends MasterPersistenceTestCase
{
    private ProjectDao projectDao;
    private BuildResultDao buildResultDao;
    private Project p1;
    private Project p2;
    private BuildResult b1;
    private BuildResult b2;
    private BuildResult b3;
    private BuildResult b4;
    private BuildResult b5;

    protected void setUp() throws Exception
    {
        super.setUp();
        projectDao = (ProjectDao) ComponentContext.getBean("projectDao");
        buildResultDao = (BuildResultDao) ComponentContext.getBean("buildResultDao");

        p1 = new Project("p1", "desc");
        p2 = new Project("p2", "desc");
        projectDao.save(p1);
        projectDao.save(p2);

        createBuild(p2, 1, System.currentTimeMillis() - Constants.DAY * 5, ResultState.SUCCESS, false);
        b1 = createBuild(p1, 1, System.currentTimeMillis() - Constants.DAY * 5, ResultState.SUCCESS, false);
        b2 = createBuild(p1, 2, System.currentTimeMillis() - Constants.DAY * 4, ResultState.ERROR, false);
        b3 = createBuild(p1, "otherspec", 3, System.currentTimeMillis() - Constants.DAY * 3, ResultState.SUCCESS, true);
        b4 = createBuild(p1, 4, System.currentTimeMillis() - Constants.DAY * 2, ResultState.SUCCESS, true);
        b5 = createBuild(p1, 5, System.currentTimeMillis() - Constants.DAY * 1, ResultState.FAILURE, true);
    }

    protected void tearDown() throws Exception
    {
        projectDao = null;
        buildResultDao = null;
        p1 = null;
        p2 = null;
        b1 = null;
        b2 = null;
        b3 = null;
        b4 = null;
        b5 = null;
        super.tearDown();
    }

    public void testWorkAfterBuilds()
    {
        CleanupRule workBuildsRule = new CleanupRule(true, null, 2, CleanupRule.CleanupUnit.BUILDS);
        List<BuildResult> results = workBuildsRule.getMatchingResults(p1, buildResultDao);
        assertEquals(Arrays.asList(b3), results);
    }

    public void testAllAfterBuilds()
    {
        CleanupRule allBuildsRule = new CleanupRule(false, null, 2, CleanupRule.CleanupUnit.BUILDS);
        List<BuildResult> results = allBuildsRule.getMatchingResults(p1, buildResultDao);
        assertEquals(Arrays.asList(b1, b2, b3), results);
    }

    public void testWorkAfterDays()
    {
        CleanupRule allBuildsRule = new CleanupRule(true, null, 2, CleanupRule.CleanupUnit.DAYS);
        List<BuildResult> results = allBuildsRule.getMatchingResults(p1, buildResultDao);
        assertEquals(Arrays.asList(b3, b4), results);
    }

    public void testAllAfterDays()
    {
        CleanupRule allBuildsRule = new CleanupRule(false, null, 2, CleanupRule.CleanupUnit.DAYS);
        List<BuildResult> results = allBuildsRule.getMatchingResults(p1, buildResultDao);
        assertEquals(Arrays.asList(b1, b2, b3, b4), results);
    }

    public void testStatesBuilds()
    {
        CleanupRule rule = new CleanupRule(false, new ResultState[]{ResultState.SUCCESS}, 1, CleanupRule.CleanupUnit.BUILDS);
        List<BuildResult> results = rule.getMatchingResults(p1, buildResultDao);
        assertEquals(Arrays.asList(b1, b3), results);
    }

    public void testStatesDays()
    {
        CleanupRule rule = new CleanupRule(false, new ResultState[]{ResultState.SUCCESS}, 1, CleanupRule.CleanupUnit.DAYS);
        List<BuildResult> results = rule.getMatchingResults(p1, buildResultDao);
        assertEquals(Arrays.asList(b1, b3, b4), results);
    }

    private BuildResult createBuild(Project project, long number, long startTime, ResultState state, boolean hasWorkDir)
    {
        return createBuild(project,  "default", number, startTime, state, hasWorkDir);
    }

    private BuildResult createBuild(Project project, String spec, long number, long startTime, ResultState state, boolean hasWorkDir)
    {
        BuildResult result = new BuildResult(project, spec, number);
        result.commence(startTime);
        switch (state)
        {

            case ERROR:
                result.error("wow");
                break;
            case FAILURE:
                result.failure();
                break;
        }
        result.complete();
        result.setHasWorkDir(hasWorkDir);
        buildResultDao.save(result);
        return result;
    }
}
