package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.test.api.PulseTestCase;

public class BuildResultTest extends PulseTestCase
{
    public void testSuccessfulCompletion()
    {
        BuildResult buildResult = createBuildResult();
        buildResult.commence();
        buildResult.complete();
        assertEquals(ResultState.SUCCESS, buildResult.getState());
    }

    public void testFailure()
    {
        BuildResult buildResult = createBuildResult();
        buildResult.commence();
        buildResult.failure("bad");
        buildResult.complete();
        assertEquals(ResultState.FAILURE, buildResult.getState());
    }

    public void testError()
    {
        BuildResult buildResult = createBuildResult();
        buildResult.commence();
        buildResult.error("bad");
        buildResult.complete();
        assertEquals(ResultState.ERROR, buildResult.getState());
    }

    public void testFailureThenError()
    {
        BuildResult buildResult = createBuildResult();
        buildResult.commence();
        buildResult.failure("fail");
        buildResult.error("err");
        buildResult.complete();
        assertEquals(ResultState.ERROR, buildResult.getState());
    }

    public void testErrorThenFailure()
    {
        BuildResult buildResult = createBuildResult();
        buildResult.commence();
        buildResult.error("err");
        buildResult.failure("fail");
        buildResult.complete();
        assertEquals(ResultState.ERROR, buildResult.getState());
    }

    public void testTerminate()
    {
        BuildResult buildResult = createBuildResult();
        buildResult.commence();
        buildResult.terminate("term");
        buildResult.complete();
        assertEquals(ResultState.ERROR, buildResult.getState());
    }
    
    public void testFailureWhenTerminating()
    {
        BuildResult buildResult = createBuildResult();
        buildResult.commence();
        buildResult.terminate("term");
        buildResult.failure("fail");
        buildResult.complete();
        assertEquals(ResultState.ERROR, buildResult.getState());
    }

    public void testErrorWhenTerminating()
    {
        BuildResult buildResult = createBuildResult();
        buildResult.commence();
        buildResult.terminate("term");
        buildResult.error("err");
        buildResult.complete();
        assertEquals(ResultState.ERROR, buildResult.getState());
    }

    public void testChooseWorstRecipe()
    {
        BuildResult buildResult = createBuildResult();
        buildResult.commence();
        buildResult.getRoot().addChild(createRecipeResultNode(ResultState.SUCCESS));
        buildResult.getRoot().addChild(createRecipeResultNode(ResultState.ERROR));
        buildResult.getRoot().addChild(createRecipeResultNode(ResultState.FAILURE));
        buildResult.complete();
        assertEquals(ResultState.ERROR, buildResult.getState());
    }

    public void testOwnStateWorseThanAnyRecipe()
    {
        BuildResult buildResult = createBuildResult();
        buildResult.commence();
        buildResult.failure("fail");
        buildResult.getRoot().addChild(createRecipeResultNode(ResultState.SUCCESS));
        buildResult.complete();
        assertEquals(ResultState.FAILURE, buildResult.getState());
    }
    
    private RecipeResultNode createRecipeResultNode(ResultState state)
    {
        RecipeResult result = new RecipeResult("recipe");
        result.setState(state);
        return new RecipeResultNode("stage", 1, result);
    }

    private BuildResult createBuildResult()
    {
        return new BuildResult(new UnknownBuildReason(), null, 1, false);
    }
}
