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

package com.zutubi.pulse.master.cleanup.config;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.core.test.EqualityAssertions;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.TriggerBuildReason;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.master.model.persistence.ProjectDao;
import com.zutubi.pulse.master.model.persistence.hibernate.MasterPersistenceTestCase;
import com.zutubi.util.Constants;

import java.util.Arrays;
import java.util.List;

import static com.zutubi.pulse.core.dependency.ivy.IvyStatus.*;

public class CleanupConfigurationTest extends MasterPersistenceTestCase
{
    private ProjectDao projectDao;
    private BuildResultDao buildResultDao;
    private Project p1;
    private Project p2;
    private BuildResult b1;
    private BuildResult b2;
    private BuildResult b3;
    private BuildResult b4;

    protected void setUp() throws Exception
    {
        super.setUp();
        projectDao = SpringComponentContext.getBean("projectDao");
        buildResultDao = SpringComponentContext.getBean("buildResultDao");

        p1 = new Project();
        p2 = new Project();
        projectDao.save(p1);
        projectDao.save(p2);

        createBuild(p2, 1, System.currentTimeMillis() - Constants.DAY * 6, ResultState.SUCCESS, STATUS_INTEGRATION);
        b1 = createBuild(p1, 1, System.currentTimeMillis() - Constants.DAY * 5, ResultState.SUCCESS, STATUS_MILESTONE);
        b2 = createBuild(p1, 2, System.currentTimeMillis() - Constants.DAY * 4, ResultState.ERROR, STATUS_RELEASE);
        b3 = createBuild(p1, 3, System.currentTimeMillis() - Constants.DAY * 3, ResultState.SUCCESS, STATUS_INTEGRATION);
        b4 = createBuild(p1, 4, System.currentTimeMillis() - Constants.DAY * 2, ResultState.SUCCESS, STATUS_MILESTONE);
        createBuild(p1, 5, System.currentTimeMillis() - Constants.DAY, ResultState.FAILURE, STATUS_RELEASE);
        // Create a build that has started but is not in progress yet: -1 timestamp
        createBuild(p1, 6, -1, ResultState.PENDING, STATUS_INTEGRATION);
    }

    public void testAllAfterBuilds()
    {
        CleanupConfiguration allBuildsRule = new CleanupConfiguration(null, null, 2, CleanupUnit.BUILDS);
        List<BuildResult> results = allBuildsRule.getMatchingResults(p1, buildResultDao);
        EqualityAssertions.assertEquals(Arrays.asList(b1, b2, b3), results);
    }

    public void testAllAfterDays()
    {
        CleanupConfiguration allBuildsRule = new CleanupConfiguration(null, null, 2, CleanupUnit.DAYS);
        List<BuildResult> results = allBuildsRule.getMatchingResults(p1, buildResultDao);
        EqualityAssertions.assertEquals(Arrays.asList(b1, b2, b3, b4), results);
    }

    public void testStatesBuilds()
    {
        CleanupConfiguration rule = new CleanupConfiguration(null, Arrays.asList(ResultState.SUCCESS), 1, CleanupUnit.BUILDS);
        List<BuildResult> results = rule.getMatchingResults(p1, buildResultDao);
        EqualityAssertions.assertEquals(Arrays.asList(b1, b3), results);
    }

    public void testStatesDays()
    {
        CleanupConfiguration rule = new CleanupConfiguration(null, Arrays.asList(ResultState.SUCCESS), 1, CleanupUnit.DAYS);
        List<BuildResult> results = rule.getMatchingResults(p1, buildResultDao);
        EqualityAssertions.assertEquals(Arrays.asList(b1, b3, b4), results);
    }

    public void testSingleStatus()
    {
        CleanupConfiguration rule = new CleanupConfiguration(null, Arrays.asList(ResultState.SUCCESS), 1, CleanupUnit.DAYS);
        rule.setStatuses(Arrays.asList(STATUS_INTEGRATION));
        List<BuildResult> results = rule.getMatchingResults(p1, buildResultDao);
        EqualityAssertions.assertEquals(Arrays.asList(b3), results);
    }

    public void testMultipleStatuses()
    {
        CleanupConfiguration rule = new CleanupConfiguration(null, Arrays.asList(ResultState.SUCCESS), 1, CleanupUnit.DAYS);
        rule.setStatuses(Arrays.asList(STATUS_INTEGRATION, STATUS_MILESTONE));
        List<BuildResult> results = rule.getMatchingResults(p1, buildResultDao);
        EqualityAssertions.assertEquals(Arrays.asList(b1, b3, b4), results);
    }

    private BuildResult createBuild(Project project, long number, long startTime, ResultState state, String status)
    {
        BuildResult result = new BuildResult(new TriggerBuildReason("scm trigger"), project, number, false);
        if (startTime >= 0)
        {
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
        }
        result.setStatus(status);
        buildResultDao.save(result);
        return result;
    }
}
