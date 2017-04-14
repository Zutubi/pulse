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

package com.zutubi.pulse.slave;

import com.zutubi.pulse.core.RecipeProcessor;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.servercore.RecipeRunner;
import com.zutubi.pulse.servercore.services.MasterService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ErrorHandlingRecipeRunnerTest extends PulseTestCase
{
    public void testRunThrowBuildException()
    {
        verifyException(new BuildException("some error"), "some error");
    }

    public void testRunThrowOtherException()
    {
        verifyException(new RuntimeException("some error"), "Unexpected error: some error");
    }

    public void verifyException(final RuntimeException exceptionToThrow, String expectedError)
    {
        RecipeRunner r = new RecipeRunner()
        {
            public void runRecipe(RecipeRequest request, RecipeProcessor recipeProcessor)
            {
                throw exceptionToThrow;
            }
        };
        MasterService masterService = mock(MasterService.class);
        ErrorHandlingRecipeRunner errorHandlingRecipeRunner = new ErrorHandlingRecipeRunner(masterService, "a token", 0, 0, r);
        errorHandlingRecipeRunner.runRecipe(null, null);

        RecipeErrorEvent recipeErrorEvent = new RecipeErrorEvent(null, 0, 0, expectedError, false);
        verify(masterService).handleEvent("a token", recipeErrorEvent);
    }
}
