package com.zutubi.pulse.model;

import com.zutubi.pulse.tove.config.project.types.MakeTypeConfiguration;
import com.zutubi.pulse.tove.config.project.types.TemplateTypeConfiguration;
import com.zutubi.pulse.core.util.FileSystemUtils;

/**
 */
public class MakeTypeConfigurationTest extends TemplateTypeConfigurationTestBase
{
    private MakeTypeConfiguration type;

    protected void setUp() throws Exception
    {
        type = new MakeTypeConfiguration();
        generateMode = true;
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        FileSystemUtils.rmdir(tmpDir);
    }

    public TemplateTypeConfiguration getType()
    {
        return type;
    }

    public void testBasic() throws Exception
    {
        createAndVerify("basic");
    }

    public void testExplicitBuildFile() throws Exception
    {
        type.setMakefile("test.makefile");
        createAndVerify("explicitMakefile");
    }

    public void testExplicitTargets() throws Exception
    {
        type.setTargets("build test");
        createAndVerify("explicitTargets");
    }

    public void testExplicitWorkingDir() throws Exception
    {
        type.setWorkingDir("mywork");
        createAndVerify("explicitWorkingDir");
    }

    public void testExplicitArgs() throws Exception
    {
        type.setArguments("arg1 arg2");
        createAndVerify("explicitArgs");
    }

    public void testProcessOutput() throws Exception
    {
        type.addPostProcessor("junit");
        createAndVerify("processOutput");
    }

    public void testCaptureArtifacts() throws Exception
    {
        addArtifacts(type);
        createAndVerify("captureArtifacts");
    }
}
