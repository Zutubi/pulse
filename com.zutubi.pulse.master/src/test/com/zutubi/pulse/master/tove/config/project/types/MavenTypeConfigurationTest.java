package com.zutubi.pulse.master.tove.config.project.types;

public class MavenTypeConfigurationTest extends TemplateTypeConfigurationTestBase
{
    private MavenTypeConfiguration type = new MavenTypeConfiguration();

    public TemplateTypeConfiguration getType()
    {
        return type;
    }

    public void testBasic() throws Exception
    {
        createAndVerify("basic");
    }

    public void testArguments() throws Exception
    {
        type.setArguments("arg1 arg2");
        createAndVerify("arguments");
    }

    public void testTargets() throws Exception
    {
        type.setTargets("target1 target2");
        createAndVerify("targets");
    }

    public void testWorkingDir() throws Exception
    {
        type.setWorkingDir("work");
        createAndVerify("workingDir");
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
