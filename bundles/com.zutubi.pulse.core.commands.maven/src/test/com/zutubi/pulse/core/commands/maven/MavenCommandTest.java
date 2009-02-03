package com.zutubi.pulse.core.commands.maven;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.commands.core.ExecutableCommandTestCase;

import java.io.File;
import java.io.IOException;

public class MavenCommandTest extends ExecutableCommandTestCase
{
    public void testNoBuildFileNoTargets() throws Exception
    {
        MavenCommandConfiguration command = new MavenCommandConfiguration();
        successRun(command, "BUILD SUCCESSFUL", "Total time", "_Apache_", "v. 1.0.2");
    }

    public void testDefaultTarget() throws Exception
    {
        copyMavenFile("basic");
        MavenCommandConfiguration command = new MavenCommandConfiguration();
        command.setWorkingDir(baseDir);
        successRun(command, "BUILD SUCCESSFUL", "build target", "_Apache_", "v. 1.0.2");
    }

    public void testExtractVersion() throws Exception
    {
        copyMavenFile("basic");
        MavenCommandConfiguration command = new MavenCommandConfiguration();
        PulseExecutionContext context = (PulseExecutionContext) createExecutionContext();
        runCommand(new MavenCommand(command), context);
        assertEquals("1.0-SNAPSHOT", context.getVersion());
    }

    public void testRunSpecificTarget() throws Exception
    {
        copyMavenFile("basic");
        MavenCommandConfiguration command = new MavenCommandConfiguration();
        command.setTargets("mytest");
        command.setWorkingDir(baseDir);
        successRun(command, "BUILD SUCCESSFUL", "test target", "_Apache_", "v. 1.0.2");
    }

    public void testRunMultipleTargets() throws Exception
    {
        copyMavenFile("basic");
        MavenCommandConfiguration command = new MavenCommandConfiguration();
        command.setTargets("mybuild mytest");
        command.setWorkingDir(baseDir);
        successRun(command, "BUILD SUCCESSFUL", "build target", "test target", "_Apache_", "v. 1.0.2");
    }

    public void testMissingTarget() throws Exception
    {
        copyMavenFile("basic");
        MavenCommandConfiguration command = new MavenCommandConfiguration();
        command.setTargets("missingTarget");
        command.setWorkingDir(baseDir);
        failedRun(command, "BUILD FAILED");
    }

    public void testExtraArgument() throws Exception
    {
        copyMavenFile("basic");
        MavenCommandConfiguration command = new MavenCommandConfiguration();
        command.setTargets("mybuild");
        command.setWorkingDir(baseDir);
        command.setArgs("-X");
        successRun(command, "Loading plugin", "'maven-j2ee-plugin-1.5.1'", "build target", "_Apache_", "v. 1.0.2");
    }

    private File copyMavenFile(String name) throws IOException
    {
        return copyMavenFile(name, "maven.xml");
    }

    private File copyMavenFile(String name, String toName) throws IOException
    {
        return copyBuildFile(name, "xml", toName);
    }

    private void successRun(MavenCommandConfiguration configuration, String... content) throws Exception
    {
        successRun(new MavenCommand(configuration), content);
    }

    private void failedRun(MavenCommandConfiguration configuration, String... content) throws Exception
    {
        failedRun(new MavenCommand(configuration), content);
    }
}
