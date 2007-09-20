package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.events.build.BuildCompletedEvent;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.UnknownBuildReason;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;
import com.zutubi.pulse.test.PulseTestCase;

/**
 */
public class BuildCompletedEventFilterTest extends PulseTestCase
{
    private static final long PROJECT = 10L;

    private BuildCompletedEventFilter filter;

    protected void setUp() throws Exception
    {
        super.setUp();
        filter = new BuildCompletedEventFilter();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        filter = null;
    }

    public void testMissingStateParam()
    {
        MockTrigger trigger = new MockTrigger();
        trigger.getDataMap().put(BuildCompletedEventFilter.PARAM_PROJECT, PROJECT);
        assertTrue(filter.accept(trigger, createEvent(ResultState.SUCCESS)));
    }

    public void testInvalidStateParam()
    {
        assertFalse(filter.accept(createTrigger("nosuchstate"), createEvent(ResultState.SUCCESS)));
    }

    public void testEmptyParam()
    {
        assertTrue(filter.accept(createTrigger(""), createEvent(ResultState.SUCCESS)));
        assertTrue(filter.accept(createTrigger(""), createEvent(ResultState.FAILURE)));
    }

    public void testMatchingState()
    {
        assertTrue(filter.accept(createTrigger("FAILURE"), createEvent(ResultState.FAILURE)));
    }

    public void testNonMatchingState()
    {
        assertFalse(filter.accept(createTrigger("ERROR"), createEvent(ResultState.FAILURE)));
    }

    public void testMatchingFromStates()
    {
        assertTrue(filter.accept(createTrigger("SUCCESS,FAILURE,ERROR"), createEvent(ResultState.FAILURE)));
    }

    public void testNonMatchingFromStates()
    {
        assertFalse(filter.accept(createTrigger("SUCCESS,FAILURE,ERROR"), createEvent(ResultState.IN_PROGRESS)));
    }

    public void testMissingProjectParam()
    {
        assertTrue(filter.accept(createTrigger(null, "SUCCESS"), createEvent(ResultState.SUCCESS)));
    }

    public void testDifferentProject()
    {
        assertFalse(filter.accept(createTrigger(PROJECT + 1, "SUCCESS"), createEvent(ResultState.SUCCESS)));
    }

    public void testPersonalBuild()
    {
        Project project = new Project();
        project.setId(PROJECT);
        User user = new User();
        UserConfiguration config = new UserConfiguration("test", "test");
        user.setConfig(config);
        BuildResult result = new BuildResult(user, project, 1);
        result.setState(ResultState.SUCCESS);
        assertFalse(filter.accept(createTrigger("SUCCESS"), new BuildCompletedEvent(this, result)));
    }

    private MockTrigger createTrigger(String s)
    {
        return createTrigger(PROJECT, s);
    }

    private MockTrigger createTrigger(Long id, String states)
    {
        MockTrigger t = new MockTrigger();
        if(id != null)
        {
            t.getDataMap().put(BuildCompletedEventFilter.PARAM_PROJECT, id);
        }

        if(states != null)
        {
            t.getDataMap().put(BuildCompletedEventFilter.PARAM_STATES, states);
        }

        return t;
    }

    private BuildCompletedEvent createEvent(ResultState state)
    {
        return createEvent(PROJECT, state);
    }

    private BuildCompletedEvent createEvent(long id, ResultState state)
    {
        Project project = new Project();
        project.setId(id);
        BuildResult result = new BuildResult(new UnknownBuildReason(), project, 1, false);
        result.setState(state);
        return new BuildCompletedEvent(this, result);
    }
}
