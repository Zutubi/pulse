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

package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.ManualTriggerBuildReason;
import com.zutubi.pulse.master.model.Project;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class BrokenCountBuildsValueTest extends PulseTestCase
{
    private BuildManager buildManager;
    private BrokenCountBuildsValue value;
    private Project project;

    protected void setUp() throws Exception
    {
        buildManager = mock(BuildManager.class);

        project = new Project();
        project.setId(12);
        value = new BrokenCountBuildsValue();
        value.setBuildManager(buildManager);
    }

    public void testNullBuild()
    {
        assertEquals(0, value.getValue(null, null));
    }

    public void testSuccessfulBuild()
    {
        BuildResult result = createBuild(1);
        result.setState(ResultState.SUCCESS);

        assertEquals(0, value.getValue(result, null));
    }

    public void testNoPreviousSuccess()
    {
        setupCalls(null, 0, 20, 1);
        assertEquals(1, value.getValue(createBuild(20), null));
    }

    public void testPreviousSuccess()
    {
        setupCalls(createBuild(3), 3, 20, 12);
        assertEquals(12, value.getValue(createBuild(20), null));
    }

    private BuildResult createBuild(long number)
    {
        BuildResult buildResult = new BuildResult(new ManualTriggerBuildReason("w00t"), project, number, false);
        buildResult.setState(ResultState.FAILURE);
        return buildResult;
    }

    private void setupCalls(BuildResult lastSuccess, long sinceBuildNumber, long buildNumber, int unsuccessfulCount)
    {
        List<BuildResult> lastSuccesses = lastSuccess == null ? Collections.<BuildResult>emptyList() : asList(lastSuccess);
        stub(buildManager.queryBuilds(eq(project), aryEq(ResultState.getHealthyStates()), anyLong(), eq(buildNumber - 1), anyInt(), anyInt(), eq(true), eq(false))).toReturn(lastSuccesses);
        stub(buildManager.getBuildCount(eq(project), eq(sinceBuildNumber), eq(buildNumber))).toReturn(unsuccessfulCount);
    }
}
