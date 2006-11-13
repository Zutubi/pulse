package com.zutubi.pulse.model;

import com.zutubi.pulse.util.FileSystemUtils;

import java.io.IOException;

/**
 */
public class MakePulseFileDetailsTest extends TemplatePulseFileDetailsTestBase
{
    private MakePulseFileDetails details;

    protected void setUp() throws Exception
    {
        details = new MakePulseFileDetails();
        generateMode = true;
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

    public void testExplicitBuildFile() throws Exception
    {
        details.setMakefile("test.makefile");
        createAndVerify("explicitMakefile");
    }

    public void testEnvironment() throws Exception
    {
        details.addEnvironmentalVariable("var", "value");
        details.addEnvironmentalVariable("var2", "value2");
        createAndVerify("environment");
    }

    public void testExplicitTargets() throws Exception
    {
        details.setTargets("build test");
        createAndVerify("explicitTargets");
    }

    public void testExplicitWorkingDir() throws Exception
    {
        details.setWorkingDir("mywork");
        createAndVerify("explicitWorkingDir");
    }

    public void testExplicitArgs() throws Exception
    {
        details.setArguments("arg1 arg2");
        createAndVerify("explicitArgs");
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
