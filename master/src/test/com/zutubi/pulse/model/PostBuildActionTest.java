package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.test.PulseTestCase;

import java.util.Arrays;

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
        action.setSpecifications(Arrays.asList(new BuildSpecification[] { spec }));
        testResult("diff");
        assertFalse(executed);
        testResult("test");
        assertTrue(executed);
    }

    public void testRestrictedToState()
    {
        action.setStates(Arrays.asList(new ResultState[] {ResultState.FAILURE}));
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
        assertEquals("test error", result.getFeatures(Feature.Level.ERROR).get(0).getSummary());
    }

    private BuildResult testResult(String spec)
    {
        BuildResult result = new BuildResult(null, null, spec, 1);
        action.execute(result);
        return result;
    }

    private BuildResult testResult(ResultState state)
    {
        BuildResult result = new BuildResult(null, null, "foo", 1);
        result.setState(state);
        action.execute(result);
        return result;
    }

    private class MockPostBuildAction extends PostBuildAction
    {
        private String error = null;

        protected void internalExecute(BuildResult result)
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

        public void setError(String error)
        {
            this.error = error;
        }
    }
}
