package com.zutubi.pulse.master.tove.config.project.types;

public class MakeTypeConfigurationTest extends TemplateTypeConfigurationTestBase
{
    private MakeTypeConfiguration type = new MakeTypeConfiguration();

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
