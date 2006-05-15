/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.util.FileSystemUtils;

import java.io.IOException;

/**
 */
public class Maven2PulseFileDetailsTest extends TemplatePulseFileDetailsTestBase
{
    private Maven2PulseFileDetails details;

    protected void setUp() throws Exception
    {
        details = new Maven2PulseFileDetails();
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        FileSystemUtils.removeDirectory(tmpDir);
    }

    public TemplatePulseFileDetails getDetails()
    {
        return details;
    }

    public void testBasic() throws Exception
    {
        createAndVerify("basic");
    }

    public void testArguments() throws Exception
    {
        details.setArguments("arg1 arg2");
        createAndVerify("arguments");
    }

    public void testGoals() throws Exception
    {
        details.setGoals("goal1 goal2");
        createAndVerify("targets");
    }

    public void testWorkingDir() throws Exception
    {
        details.setWorkingDir("work");
        createAndVerify("workingDir");
    }

    public void testProcessOutput() throws Exception
    {
        details.getOutputProcessors().add("junit");
        createAndVerify("processOutput");
    }

    public void testCaptureArtifacts() throws Exception
    {
        addCaptures(details);
        createAndVerify("captureArtifacts");
    }

}
