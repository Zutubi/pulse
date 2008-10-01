package com.zutubi.pulse.slave;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.servercore.services.MasterService;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ErrorHandlingRunnableTest extends PulseTestCase
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
        Runnable r = new Runnable()
        {
            public void run()
            {
                throw exceptionToThrow;
            }
        };
        MasterService masterService = mock(MasterService.class);
        ErrorHandlingRunnable errorHandlingRunnable = new ErrorHandlingRunnable(masterService, "a token", 0, r);
        errorHandlingRunnable.run();

        RecipeErrorEvent recipeErrorEvent = new RecipeErrorEvent(null, 0, expectedError);
        verify(masterService).handleEvent("a token", recipeErrorEvent);
    }
}
