/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.core.util.FileSystemUtils;

import java.io.IOException;

/**
 */
public class AntPulseFileDetailsTest extends TemplatePulseFileDetailsTestBase
{
    private AntPulseFileDetails details;

    protected void setUp() throws Exception
    {
        details = new AntPulseFileDetails();
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

    public void testBasic() throws IOException
    {
        createAndVerify("basic");
    }

    public void testExplicitBuildFile() throws IOException
    {
        details.setBuildFile("test.xml");
        createAndVerify("explicitBuildFile");
    }

    public void testEnvironment() throws IOException
    {
        details.addEnvironmentalVariable("var", "value");
        details.addEnvironmentalVariable("var2", "value2");
        createAndVerify("environment");
    }

    public void testExplicitTargets() throws IOException
    {
        details.setTargets("build test");
        createAndVerify("explicitTargets");
    }


    public void testExplicitWorkingDir() throws IOException
    {
        details.setWorkingDir("mywork");
        createAndVerify("explicitWorkingDir");
    }

    public void testExplicitArgs() throws IOException
    {
        details.setArguments("arg1 arg2");
        createAndVerify("explicitArgs");
    }
}
