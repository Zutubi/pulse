package com.cinnamonbob.core2;

import com.cinnamonbob.BuildRequest;

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
        assertTrue(result.succeeded());
        assertEquals(1, result.getCommandResults().size());
    }
}
