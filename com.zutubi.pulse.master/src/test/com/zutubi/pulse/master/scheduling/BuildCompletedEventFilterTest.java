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

package com.zutubi.pulse.master.scheduling;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.scheduling.tasks.BuildProjectTask;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.BuildCompletedTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

import static java.util.Arrays.asList;

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

    public void testMissingStateParam()
    {
        Trigger trigger = createTrigger(PROJECT);
        assertTrue(filter.accept(trigger, createEvent(ResultState.SUCCESS), new TaskExecutionContext()));
    }

    public void testEmptyParam()
    {
        ResultState[] states1 = new ResultState[]{};
        assertTrue(filter.accept(createTrigger(PROJECT, states1), createEvent(ResultState.SUCCESS), new TaskExecutionContext()));
        ResultState[] states = new ResultState[]{};
        assertTrue(filter.accept(createTrigger(PROJECT, states), createEvent(ResultState.FAILURE), new TaskExecutionContext()));
    }

    public void testMatchingState()
    {
        ResultState[] states = new ResultState[]{ResultState.FAILURE};
        assertTrue(filter.accept(createTrigger(PROJECT, states), createEvent(ResultState.FAILURE), new TaskExecutionContext()));
    }

    public void testNonMatchingState()
    {
        ResultState[] states = new ResultState[]{ResultState.ERROR};
        assertFalse(filter.accept(createTrigger(PROJECT, states), createEvent(ResultState.FAILURE), new TaskExecutionContext()));
    }

    public void testMatchingFromStates()
    {
        ResultState[] states = new ResultState[]{ResultState.SUCCESS, ResultState.FAILURE, ResultState.ERROR};
        assertTrue(filter.accept(createTrigger(PROJECT, states), createEvent(ResultState.FAILURE), new TaskExecutionContext()));
    }

    public void testNonMatchingFromStates()
    {
        ResultState[] states = new ResultState[]{ResultState.SUCCESS, ResultState.FAILURE, ResultState.ERROR};
        assertFalse(filter.accept(createTrigger(PROJECT, states), createEvent(ResultState.IN_PROGRESS), new TaskExecutionContext()));
    }

    public void testMissingProjectParam()
    {
        assertTrue(filter.accept(createTrigger(null, ResultState.SUCCESS), createEvent(ResultState.SUCCESS), new TaskExecutionContext()));
    }

    public void testDifferentProject()
    {
        assertFalse(filter.accept(createTrigger(PROJECT + 1, ResultState.SUCCESS), createEvent(ResultState.SUCCESS), new TaskExecutionContext()));
    }

    public void testContextNotPopulatedWhenNotAccepted()
    {
        TaskExecutionContext context = new TaskExecutionContext();
        ResultState[] states = new ResultState[]{};
        assertFalse(filter.accept(createTrigger(PROJECT, states), createEvent(PROJECT + 1, ResultState.SUCCESS), context));
        assertNull(context.get(BuildProjectTask.PARAM_REVISION));
        assertNull(context.get(BuildProjectTask.PARAM_REPLACEABLE));
    }

    public void testContextNotPopulatedWhenNotAcceptedAllOptionsTrue()
    {
        TaskExecutionContext context = new TaskExecutionContext();
        assertFalse(filter.accept(createTrigger(PROJECT, true, true), createEvent(PROJECT + 1, ResultState.SUCCESS), context));
        assertNull(context.get(BuildProjectTask.PARAM_REVISION));
        assertNull(context.get(BuildProjectTask.PARAM_REPLACEABLE));
    }

    public void testRevisionPopulatedPropagateTrue()
    {
        TaskExecutionContext context = new TaskExecutionContext();
        assertTrue(filter.accept(createTrigger(PROJECT, true, false), createEvent(ResultState.SUCCESS), context));
        Revision revision = (Revision) context.get(BuildProjectTask.PARAM_REVISION);
        assertNotNull(revision);
        assertEquals(REVISION, revision.getRevisionString());
    }

    public void testRevisionNotPopulatedPropagateFalse()
    {
        TaskExecutionContext context = new TaskExecutionContext();
        assertTrue(filter.accept(createTrigger(PROJECT, false, false), createEvent(ResultState.SUCCESS), context));
        assertNull(context.get(BuildProjectTask.PARAM_REVISION));
    }

    public void testReplaceablePopulatedTrue()
    {
        TaskExecutionContext context = new TaskExecutionContext();
        assertTrue(filter.accept(createTrigger(PROJECT, true, true), createEvent(ResultState.SUCCESS), context));
        Boolean supercede = (Boolean) context.get(BuildProjectTask.PARAM_REPLACEABLE);
        assertNotNull(supercede);
        assertTrue(supercede);
    }

    public void testReplaceablePopulatedFalse()
    {
        TaskExecutionContext context = new TaskExecutionContext();
        assertTrue(filter.accept(createTrigger(PROJECT, true, false), createEvent(ResultState.SUCCESS), context));
        Boolean supercede = (Boolean) context.get(BuildProjectTask.PARAM_REPLACEABLE);
        assertNotNull(supercede);
        assertFalse(supercede);
    }

    public void testReplaceableNotPopulatedWhenPropagateFalse()
    {
        TaskExecutionContext context = new TaskExecutionContext();
        assertTrue(filter.accept(createTrigger(PROJECT, false, true), createEvent(ResultState.SUCCESS), context));
        assertNull(context.get(BuildProjectTask.PARAM_REPLACEABLE));
    }


    public void testPersonalBuild()
    {
        Project project = new Project();
        project.setId(PROJECT);
        User user = new User();
        UserConfiguration config = new UserConfiguration("test", "test");
        user.setConfig(config);
        BuildResult result = new BuildResult(new PersonalBuildReason(user.getLogin()), user, project, 1);
        result.setState(ResultState.SUCCESS);
        ResultState[] states = new ResultState[]{ResultState.SUCCESS};
        assertFalse(filter.accept(createTrigger(PROJECT, states), new BuildCompletedEvent(this, result, null), new TaskExecutionContext()));
    }

    private Trigger createTrigger(Long id, ResultState... states)
    {
        return createTrigger(id, false, false, states);
    }

    private Trigger createTrigger(Long id, boolean propagateRevision, boolean supercede, ResultState... states)
    {
        Trigger t = new EventTrigger();
        t.setId(42);

        BuildCompletedTriggerConfiguration config = new BuildCompletedTriggerConfiguration();
        if (id != null)
        {
            ProjectConfiguration projectConfig = new ProjectConfiguration();
            projectConfig.setProjectId(id);
            config.setProject(projectConfig);
        }

        if (states != null)
        {
            config.setStates(asList(states));
        }

        config.setPropagateRevision(propagateRevision);
        config.setSupercedeQueued(supercede);
        t.setConfig(config);
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
