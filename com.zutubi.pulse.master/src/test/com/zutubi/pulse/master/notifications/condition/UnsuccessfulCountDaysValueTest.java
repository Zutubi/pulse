package com.zutubi.pulse.master.notifications.condition;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.FullConstraintMatcher;
import com.mockobjects.dynamic.Mock;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.ManualTriggerBuildReason;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 */
public class UnsuccessfulCountDaysValueTest extends PulseTestCase
{
    private Mock mockBuildManager;
    private UnsuccessfulCountDaysValue value;
    private Project project;

    protected void setUp() throws Exception
    {
        mockBuildManager = new Mock(BuildManager.class);
        project = new Project();
        project.setId(99);
        value = new UnsuccessfulCountDaysValue();
    }

    public void testNullBuild()
    {
        setBuildManager();
        assertEquals(0, value.getValue(null, null));
    }

    public void testSuccessfulBuild()
    {
        setBuildManager();
        BuildResult result = createBuild(1);
        result.setState(ResultState.SUCCESS);
        assertEquals(0, value.getValue(result, null));
    }

    public void testNoPreviousSuccessFirstFailure()
    {
        BuildResult failure = createBuild(20);
        setupCalls(20, null, failure);
        setBuildManager();
        assertEquals(0, value.getValue(failure, null));
    }

    public void testNoPreviousSuccess()
    {
        BuildResult firstFailure = createBuild(2);
        firstFailure.getStamps().setEndTime(System.currentTimeMillis() - Constants.DAY * 3 - 7200000);
        BuildResult failure = createBuild(20);
        setupCalls(20, null, firstFailure);
        setBuildManager();
        assertEquals(3, value.getValue(failure, null));
    }

    public void testPreviousSuccessFirstFailure()
    {
        BuildResult success = createBuild(3);
        success.setState(ResultState.SUCCESS);
        BuildResult failure = createBuild(20);
        setupCalls(20, success, failure);
        setBuildManager();
        assertEquals(0, value.getValue(failure, null));
    }

    public void testPreviousSuccess()
    {
        BuildResult success = createBuild(3);
        success.setState(ResultState.SUCCESS);
        BuildResult firstFailure = createBuild(5);
        firstFailure.getStamps().setEndTime(System.currentTimeMillis() - Constants.DAY * 4);
        setupCalls(20, success, firstFailure);
        setBuildManager();
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
        List<BuildResult> lastSuccesses = new ArrayList<BuildResult>(1);
        lastSuccesses.add(lastSuccess);
        mockBuildManager.expectAndReturn("queryBuilds", new FullConstraintMatcher(new Constraint[]{ C.eq(project), C.eq(new ResultState[]{ ResultState.SUCCESS }), C.eq(-1L), C.eq(number - 1), C.eq(0), C.eq(1), C.eq(true), C.eq(false) }), lastSuccesses);
        long lastSuccessNumber = lastSuccess == null ? 1 : lastSuccess.getNumber() + 1;
        mockBuildManager.expectAndReturn("queryBuilds", new FullConstraintMatcher(new Constraint[]{ C.eq(project), C.IS_NULL, C.eq(lastSuccessNumber), C.eq(-1L), C.eq(0), C.eq(1), C.eq(false), C.eq(false) }), Arrays.asList(firstFailure));
    }

    private void setBuildManager()
    {
        value.setBuildManager((BuildManager) mockBuildManager.proxy());
    }
}
