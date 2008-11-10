package com.zutubi.pulse.master.tove.config.project.types;

public class Maven2TypeConfigurationTest extends TemplateTypeConfigurationTestBase
{
    private Maven2TypeConfiguration type = new Maven2TypeConfiguration();

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

    public void testGoals() throws Exception
    {
        type.setGoals("goal1 goal2");
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

    public void testSuppressWarning() throws Exception
    {
        type.setSuppressWarning("WARNING");
        createAndVerify("suppressWarning");
    }

    public void testSuppressError() throws Exception
    {
        type.setSuppressError("ERROR");
        createAndVerify("suppressError");
    }
}
