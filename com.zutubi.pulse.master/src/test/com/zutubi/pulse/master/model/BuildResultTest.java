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
        assertEquals(ResultState.TERMINATED, buildResult.getState());
    }
    
    public void testFailureWhenTerminating()
    {
        BuildResult buildResult = createBuildResult();
        buildResult.commence();
        buildResult.terminate("term");
        buildResult.failure("fail");
        buildResult.complete();
        assertEquals(ResultState.TERMINATED, buildResult.getState());
    }

    public void testErrorWhenTerminating()
    {
        BuildResult buildResult = createBuildResult();
        buildResult.commence();
        buildResult.terminate("term");
        buildResult.error("err");
        buildResult.complete();
        assertEquals(ResultState.TERMINATED, buildResult.getState());
    }

    public void testChooseWorstRecipe()
    {
        BuildResult buildResult = createBuildResult();
        buildResult.commence();
        buildResult.addStage(createRecipeResultNode(ResultState.SUCCESS));
        buildResult.addStage(createRecipeResultNode(ResultState.ERROR));
        buildResult.addStage(createRecipeResultNode(ResultState.FAILURE));
        buildResult.complete();
        assertEquals(ResultState.ERROR, buildResult.getState());
    }

    public void testOwnStateWorseThanAnyRecipe()
    {
        BuildResult buildResult = createBuildResult();
        buildResult.commence();
        buildResult.failure("fail");
        buildResult.addStage(createRecipeResultNode(ResultState.SUCCESS));
        buildResult.complete();
        assertEquals(ResultState.FAILURE, buildResult.getState());
    }

    public void testCancelRecipe()
    {
        BuildResult buildResult = createBuildResult();
        buildResult.commence();

        buildResult.cancel("cancel");
        assertEquals(ResultState.CANCELLING, buildResult.getState());
        assertTrue(buildResult.terminating());
        
        buildResult.complete();
        assertEquals(ResultState.CANCELLED, buildResult.getState());
        assertTrue(buildResult.terminated());
    }

    public void testCancelBeforeCommence()
    {
        BuildResult buildResult = createBuildResult();

        buildResult.cancel("cancel");
        assertEquals(ResultState.CANCELLING, buildResult.getState());
        assertTrue(buildResult.terminating());

        buildResult.complete();
        assertEquals(ResultState.CANCELLED, buildResult.getState());
        assertTrue(buildResult.terminated());
    }

    public void testErrorWhenCancelling()
    {
        BuildResult buildResult = createBuildResult();
        buildResult.commence();
        buildResult.cancel("cancel");
        buildResult.error("error");
        buildResult.complete();
        assertEquals(ResultState.CANCELLED, buildResult.getState());
    }

    public void testFailureWhenCancelling()
    {
        BuildResult buildResult = createBuildResult();
        buildResult.commence();
        buildResult.cancel("cancel");
        buildResult.failure("fail");
        buildResult.complete();
        assertEquals(ResultState.CANCELLED, buildResult.getState());
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
