package com.cinnamonbob.core;

import com.cinnamonbob.BuildRequest;
import com.cinnamonbob.core.BuildProcessor;
import com.cinnamonbob.core.BuildResult;
import com.cinnamonbob.model.ResultState;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class BuildProcessorTest extends TestCase
{
    
    public void testExecute() throws Exception
    {
        BuildProcessor processor = new BuildProcessor();
        BuildRequest request = new BuildRequest("testProject");
        BuildResult result = processor.execute(request);        
        assertNotNull(result);
        assertEquals(result.getState(), ResultState.SUCCESS);
        assertEquals(1, result.getCommandResults().size());
    }
}
