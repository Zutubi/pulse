package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.ManualTriggerBuildReason;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.Constants;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class BrokenCountDaysValueTest extends PulseTestCase
{
    private BuildManager buildManager;
    private BrokenCountDaysValue value;
    private Project project;

    protected void setUp() throws Exception
    {
        buildManager = mock(BuildManager.class);
        project = new Project();
        project.setId(99);
        value = new BrokenCountDaysValue();
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

    public void testNoPreviousSuccessFirstFailure()
    {
        BuildResult failure = createBuild(20);
        setupCalls(20, null, failure);
        assertEquals(0, value.getValue(failure, null));
    }

    public void testNoPreviousSuccess()
    {
        BuildResult firstFailure = createBuild(2);
        firstFailure.getStamps().setEndTime(System.currentTimeMillis() - Constants.DAY * 3 - 7200000);
        BuildResult failure = createBuild(20);
        setupCalls(20, null, firstFailure);
        assertEquals(3, value.getValue(failure, null));
    }

    public void testPreviousSuccessFirstFailure()
    {
        BuildResult success = createBuild(3);
        success.setState(ResultState.SUCCESS);
        BuildResult failure = createBuild(20);
        setupCalls(20, success, failure);
        assertEquals(0, value.getValue(failure, null));
    }

    public void testPreviousSuccess()
    {
        BuildResult success = createBuild(3);
        success.setState(ResultState.SUCCESS);
        BuildResult firstFailure = createBuild(5);
        firstFailure.getStamps().setEndTime(System.currentTimeMillis() - Constants.DAY * 4);
        setupCalls(20, success, firstFailure);
        assertEquals(4, value.getValue(createBuild(20), null));
    }

    private BuildResult createBuild(long number)
    {
        BuildResult buildResult = new BuildResult(new ManualTriggerBuildReason("w00t"), project, number, false);
        buildResult.complete(System.currentTimeMillis());
        buildResult.setState(ResultState.FAILURE);
        return buildResult;
    }

    private void setupCalls(long number, BuildResult lastSuccess, BuildResult... firstFailure)
    {
        List<BuildResult> lastSuccesses = lastSuccess == null ? Collections.<BuildResult>emptyList() : asList(lastSuccess);
        stub(buildManager.queryBuilds(eq(project), aryEq(ResultState.getHealthyStates()), eq(-1L), eq(number - 1), anyInt(), anyInt(), eq(true), eq(false))).toReturn(lastSuccesses);
        long lastSuccessNumber = lastSuccess == null ? 1 : lastSuccess.getNumber() + 1;
        stub(buildManager.queryBuilds(eq(project), (ResultState[]) isNull(), eq(lastSuccessNumber), eq(-1L), anyInt(), anyInt(), eq(false), eq(false))).toReturn(asList(firstFailure));
    }
}
