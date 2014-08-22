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
