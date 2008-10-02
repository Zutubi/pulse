package com.zutubi.pulse.master.scheduling;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.UnknownBuildReason;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.scheduling.tasks.BuildProjectTask;
import com.zutubi.pulse.tove.config.user.UserConfiguration;

import java.io.Serializable;
import java.util.Map;

public class BuildCompletedEventFilterTest extends PulseTestCase
{
    private static final long PROJECT = 10L;
    private static final String REVISION = "test revision";

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
        assertTrue(filter.accept(trigger, createEvent(ResultState.SUCCESS), new TaskExecutionContext()));
    }

    public void testInvalidStateParam()
    {
        assertFalse(filter.accept(createTrigger("nosuchstate"), createEvent(ResultState.SUCCESS), new TaskExecutionContext()));
    }

    public void testEmptyParam()
    {
        assertTrue(filter.accept(createTrigger(""), createEvent(ResultState.SUCCESS), new TaskExecutionContext()));
        assertTrue(filter.accept(createTrigger(""), createEvent(ResultState.FAILURE), new TaskExecutionContext()));
    }

    public void testMatchingState()
    {
        assertTrue(filter.accept(createTrigger("FAILURE"), createEvent(ResultState.FAILURE), new TaskExecutionContext()));
    }

    public void testNonMatchingState()
    {
        assertFalse(filter.accept(createTrigger("ERROR"), createEvent(ResultState.FAILURE), new TaskExecutionContext()));
    }

    public void testMatchingFromStates()
    {
        assertTrue(filter.accept(createTrigger("SUCCESS,FAILURE,ERROR"), createEvent(ResultState.FAILURE), new TaskExecutionContext()));
    }

    public void testNonMatchingFromStates()
    {
        assertFalse(filter.accept(createTrigger("SUCCESS,FAILURE,ERROR"), createEvent(ResultState.IN_PROGRESS), new TaskExecutionContext()));
    }

    public void testMissingProjectParam()
    {
        assertTrue(filter.accept(createTrigger(null, "SUCCESS"), createEvent(ResultState.SUCCESS), new TaskExecutionContext()));
    }

    public void testDifferentProject()
    {
        assertFalse(filter.accept(createTrigger(PROJECT + 1, "SUCCESS"), createEvent(ResultState.SUCCESS), new TaskExecutionContext()));
    }

    public void testContextNotPopulatedWhenNotAccepted()
    {
        TaskExecutionContext context = new TaskExecutionContext();
        assertFalse(filter.accept(createTrigger(""), createEvent(PROJECT + 1, ResultState.SUCCESS), context));
        assertNull(context.get(BuildProjectTask.PARAM_REVISION));
        assertNull(context.get(BuildProjectTask.PARAM_REPLACEABLE));
    }

    public void testContextNotPopulatedWhenNotAcceptedAllOptionsTrue()
    {
        TaskExecutionContext context = new TaskExecutionContext();
        assertFalse(filter.accept(createTrigger(PROJECT, "", true, true), createEvent(PROJECT + 1, ResultState.SUCCESS), context));
        assertNull(context.get(BuildProjectTask.PARAM_REVISION));
        assertNull(context.get(BuildProjectTask.PARAM_REPLACEABLE));
    }

    public void testRevisionPopulatedPropagateTrue()
    {
        TaskExecutionContext context = new TaskExecutionContext();
        assertTrue(filter.accept(createTrigger(PROJECT, "", true, false), createEvent(ResultState.SUCCESS), context));
        Revision revision = (Revision) context.get(BuildProjectTask.PARAM_REVISION);
        assertNotNull(revision);
        assertEquals(REVISION, revision.getRevisionString());
    }

    public void testRevisionNotPopulatedPropagateFalse()
    {
        TaskExecutionContext context = new TaskExecutionContext();
        assertTrue(filter.accept(createTrigger(PROJECT, "", false, false), createEvent(ResultState.SUCCESS), context));
        assertNull(context.get(BuildProjectTask.PARAM_REVISION));
    }

    public void testReplaceablePopulatedTrue()
    {
        TaskExecutionContext context = new TaskExecutionContext();
        assertTrue(filter.accept(createTrigger(PROJECT, "", true, true), createEvent(ResultState.SUCCESS), context));
        Boolean supercede = (Boolean) context.get(BuildProjectTask.PARAM_REPLACEABLE);
        assertNotNull(supercede);
        assertTrue(supercede);
    }

    public void testReplaceablePopulatedFalse()
    {
        TaskExecutionContext context = new TaskExecutionContext();
        assertTrue(filter.accept(createTrigger(PROJECT, "", true, false), createEvent(ResultState.SUCCESS), context));
        Boolean supercede = (Boolean) context.get(BuildProjectTask.PARAM_REPLACEABLE);
        assertNotNull(supercede);
        assertFalse(supercede);
    }

    public void testReplaceableNotPopulatedWhenPropagateFalse()
    {
        TaskExecutionContext context = new TaskExecutionContext();
        assertTrue(filter.accept(createTrigger(PROJECT, "", false, true), createEvent(ResultState.SUCCESS), context));
        assertNull(context.get(BuildProjectTask.PARAM_REPLACEABLE));
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
        assertFalse(filter.accept(createTrigger("SUCCESS"), new BuildCompletedEvent(this, result, null), new TaskExecutionContext()));
    }

    private MockTrigger createTrigger(String states)
    {
        return createTrigger(PROJECT, states);
    }

    private MockTrigger createTrigger(Long id, String states)
    {
        return createTrigger(id, states, false, false);
    }

    private MockTrigger createTrigger(Long id, String states, boolean propagateRevision, boolean supercede)
    {
        MockTrigger t = new MockTrigger();
        t.setId(42);

        Map<Serializable,Serializable> dataMap = t.getDataMap();
        if(id != null)
        {
            dataMap.put(BuildCompletedEventFilter.PARAM_PROJECT, id);
        }

        if(states != null)
        {
            dataMap.put(BuildCompletedEventFilter.PARAM_STATES, states);
        }

        dataMap.put(BuildCompletedEventFilter.PARAM_PROPAGATE_REVISION, propagateRevision);
        dataMap.put(BuildCompletedEventFilter.PARAM_REPLACEABLE, supercede);

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
        result.setRevision(new Revision(REVISION));
        return new BuildCompletedEvent(this, result, null);
    }
}
