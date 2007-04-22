package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class PostBuildActionTest extends PulseTestCase
{
    private boolean executed = false;
    private MockPostBuildAction action = new MockPostBuildAction();

    public void testNoRestrictions()
    {
        testResult("test");
        assertTrue(executed);
    }

    public void testRestrictedToSpec()
    {
        BuildSpecification spec = new BuildSpecification("test");
        action.setSpecifications(Arrays.asList(spec));
        testResult("diff");
        assertFalse(executed);
        testResult("test");
        assertTrue(executed);
    }

    public void testRestrictedToState()
    {
        action.setStates(Arrays.asList(ResultState.FAILURE));
        testResult(ResultState.ERROR);
        assertFalse(executed);
        testResult(ResultState.FAILURE);
        assertTrue(executed);
    }

    public void testErrorNoFail()
    {
        action.setError("test error");
        BuildResult result = testResult(ResultState.SUCCESS);
        assertEquals(ResultState.SUCCESS, result.getState());
    }

    public void testErrorFail()
    {
        action.setError("test error");
        action.setFailOnError(true);
        BuildResult result = testResult(ResultState.SUCCESS);
        assertEquals(ResultState.ERROR, result.getState());
        assertEquals("Executing post build action 'mock': test error", result.getFeatures(Feature.Level.ERROR).get(0).getSummary());
    }

    private BuildResult testResult(String spec)
    {
        BuildResult result = new BuildResult(new UnknownBuildReason(), null, new BuildSpecification(spec), 1, false);
        action.execute(null, result, null, new LinkedList<ResourceProperty>());
        return result;
    }

    private BuildResult testResult(ResultState state)
    {
        BuildResult result = new BuildResult(new UnknownBuildReason(), null, new BuildSpecification("foo"), 1, false);
        result.setState(state);
        action.execute(null, result, null, new LinkedList<ResourceProperty>());
        return result;
    }

    private class MockPostBuildAction extends PostBuildAction
    {
        private String error = null;

        public MockPostBuildAction()
        {
            setName("mock");
        }

        protected void internalExecute(ProjectConfiguration projectConfig, BuildResult result, RecipeResultNode recipe, List<ResourceProperty> properties)
        {
            executed = true;
            if(error != null)
            {
                addError(error);
            }
        }

        public String getType()
        {
            return "mock";
        }

        public PostBuildAction copy()
        {
            throw new RuntimeException("Method not implemented.");
        }

        public void setError(String error)
        {
            this.error = error;
        }
    }
}
