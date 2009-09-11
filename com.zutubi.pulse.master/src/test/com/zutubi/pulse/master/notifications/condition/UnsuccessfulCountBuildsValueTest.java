package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.ManualTriggerBuildReason;
import com.zutubi.pulse.master.model.Project;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.List;

public class UnsuccessfulCountBuildsValueTest extends PulseTestCase
{
    private BuildManager buildManager;
    private UnsuccessfulCountBuildsValue value;
    private Project project;

    protected void setUp() throws Exception
    {
        buildManager = mock(BuildManager.class);

        project = new Project();
        project.setId(12);
        value = new UnsuccessfulCountBuildsValue();
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
        stub(buildManager.queryBuilds(eq(project), aryEq(new ResultState[]{ResultState.SUCCESS}), anyLong(), eq(buildNumber - 1), anyInt(), anyInt(), eq(true), eq(false))).toReturn(lastSuccesses);
        stub(buildManager.getBuildCount(eq(project), eq(sinceBuildNumber), eq(buildNumber))).toReturn(unsuccessfulCount);
    }
}
