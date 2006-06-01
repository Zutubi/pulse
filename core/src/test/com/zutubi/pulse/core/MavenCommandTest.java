package com.zutubi.pulse.core;

import java.io.IOException;

/**
 * <class-comment/>
 */
public class MavenCommandTest extends CommandTestBase
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

    public void testNoBuildFileNoTargets() throws IOException
    {
        MavenCommand command = new MavenCommand();
        successRun(command, "BUILD SUCCESSFUL", "Total time", "_Apache_", "v. 1.0.2");
    }

    public void testDefaultTarget() throws IOException
    {
        copyBuildFile("basic");
        MavenCommand command = new MavenCommand();
        command.setWorkingDir(baseDir);
        successRun(command, "BUILD SUCCESSFUL", "build target", "_Apache_", "v. 1.0.2");
    }

    public void testRunSpecificTarget() throws IOException
    {
        copyBuildFile("basic");
        MavenCommand command = new MavenCommand();
        command.setTargets("mytest");
        command.setWorkingDir(baseDir);
        successRun(command, "BUILD SUCCESSFUL", "test target", "_Apache_", "v. 1.0.2");
    }

    public void testRunMultipleTargets() throws IOException
    {
        copyBuildFile("basic");
        MavenCommand command = new MavenCommand();
        command.setTargets("mybuild mytest");
        command.setWorkingDir(baseDir);
        successRun(command, "BUILD SUCCESSFUL", "build target", "test target", "_Apache_", "v. 1.0.2");
    }

    public void testMissingTarget() throws IOException
    {
        copyBuildFile("basic");
        MavenCommand command = new MavenCommand();
        command.setTargets("missingTarget");
        command.setWorkingDir(baseDir);
        failedRun(command, "BUILD FAILED");
    }

    public void testExtraArgument() throws IOException
    {
        copyBuildFile("basic");
        MavenCommand command = new MavenCommand();
        command.setTargets("mybuild");
        command.setWorkingDir(baseDir);
        command.addArguments("-X");
        successRun(command, "Loading plugin", "'maven-j2ee-plugin-1.5.1'", "build target", "_Apache_", "v. 1.0.2");
    }
}
