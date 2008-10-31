package com.zutubi.pulse.core.commands.maven;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.commands.core.ExecutableCommandTestBase;

/**
 * <class-comment/>
 */
public class MavenCommandTest extends ExecutableCommandTestBase
{
    public MavenCommandTest()
    {
    }

    public MavenCommandTest(String name)
    {
        super(name);
    }

    protected String getBuildFileName()
    {
        return "maven.xml";
    }

    protected String getBuildFileExt()
    {
        return "xml";
    }

    public void testNoBuildFileNoTargets() throws Exception
    {
        MavenCommand command = new MavenCommand();
        successRun(command, "BUILD SUCCESSFUL", "Total time", "_Apache_", "v. 1.0.2");
    }

    public void testDefaultTarget() throws Exception
    {
        copyBuildFileToBaseDir("basic");
        MavenCommand command = new MavenCommand();
        command.setWorkingDir(baseDir);
        successRun(command, "BUILD SUCCESSFUL", "build target", "_Apache_", "v. 1.0.2");
    }

    public void testExtractVersion() throws Exception
    {
        copyBuildFileToBaseDir("basic");
        MavenCommand command = new MavenCommand();
        PulseExecutionContext context = new PulseExecutionContext();
        runCommand(command, context);
        assertEquals("1.0-SNAPSHOT", context.getVersion());
    }

    public void testRunSpecificTarget() throws Exception
    {
        copyBuildFileToBaseDir("basic");
        MavenCommand command = new MavenCommand();
        command.setTargets("mytest");
        command.setWorkingDir(baseDir);
        successRun(command, "BUILD SUCCESSFUL", "test target", "_Apache_", "v. 1.0.2");
    }

    public void testRunMultipleTargets() throws Exception
    {
        copyBuildFileToBaseDir("basic");
        MavenCommand command = new MavenCommand();
        command.setTargets("mybuild mytest");
        command.setWorkingDir(baseDir);
        successRun(command, "BUILD SUCCESSFUL", "build target", "test target", "_Apache_", "v. 1.0.2");
    }

    public void testMissingTarget() throws Exception
    {
        copyBuildFileToBaseDir("basic");
        MavenCommand command = new MavenCommand();
        command.setTargets("missingTarget");
        command.setWorkingDir(baseDir);
        failedRun(command, "BUILD FAILED");
    }

    public void testExtraArgument() throws Exception
    {
        copyBuildFileToBaseDir("basic");
        MavenCommand command = new MavenCommand();
        command.setTargets("mybuild");
        command.setWorkingDir(baseDir);
        command.addArguments("-X");
        successRun(command, "Loading plugin", "'maven-j2ee-plugin-1.5.1'", "build target", "_Apache_", "v. 1.0.2");
    }
}
