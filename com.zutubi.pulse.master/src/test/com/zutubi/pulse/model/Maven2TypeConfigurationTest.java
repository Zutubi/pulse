package com.zutubi.pulse.model;

import com.zutubi.pulse.tove.config.project.types.Maven2TypeConfiguration;
import com.zutubi.pulse.tove.config.project.types.TemplateTypeConfiguration;
import com.zutubi.pulse.core.util.FileSystemUtils;

/**
 */
public class Maven2TypeConfigurationTest extends TemplateTypeConfigurationTestBase
{
    private Maven2TypeConfiguration type;

    protected void setUp() throws Exception
    {
        type = new Maven2TypeConfiguration();
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

}
