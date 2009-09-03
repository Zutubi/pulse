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

import java.util.ArrayList;
import java.util.List;

/**
 */
public class UnsuccessfulCountBuildsValueTest extends PulseTestCase
{
    private Mock mockBuildManager;
    private UnsuccessfulCountBuildsValue value;
    private Project project;

    protected void setUp() throws Exception
    {
        mockBuildManager = new Mock(BuildManager.class);
        project = new Project();
        project.setId(12);
        value = new UnsuccessfulCountBuildsValue();
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

    public void testNoPreviousSuccess()
    {
        setupCalls(null, 0, 20, 1);
        setBuildManager();
        assertEquals(1, value.getValue(createBuild(20), null));
    }

    public void testPreviousSuccess()
    {
        setupCalls(createBuild(3), 3, 20, 12);
        setBuildManager();
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
        List<BuildResult> lastSuccesses = new ArrayList<BuildResult>(1);
        lastSuccesses.add(lastSuccess);
        mockBuildManager.expectAndReturn("queryBuilds", new FullConstraintMatcher(new Constraint[]{ C.eq(project), C.eq(new ResultState[]{ ResultState.SUCCESS }), C.eq(-1L), C.eq(buildNumber - 1), C.eq(0), C.eq(1), C.eq(true), C.eq(false) }), lastSuccesses);
        mockBuildManager.expectAndReturn("getBuildCount", C.args(C.eq(project), C.eq(sinceBuildNumber), C.eq(buildNumber)), unsuccessfulCount);
    }

    private void setBuildManager()
    {
        value.setBuildManager((BuildManager) mockBuildManager.proxy());
    }
}
