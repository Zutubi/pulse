package com.zutubi.pulse.core.commands.make;

import com.zutubi.pulse.core.commands.core.ExecutableCommandConfiguration;
import com.zutubi.pulse.core.commands.core.ExecutableCommandTestCase;
import com.zutubi.pulse.core.commands.core.NamedArgumentCommand;

import java.io.File;
import java.io.IOException;

public class MakeCommandTest extends ExecutableCommandTestCase
{
    public void testBasicDefault() throws Exception
    {
        copyMakefile("basic");
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        successRun(command, "build target");
    }

    public void testBasicTargets() throws Exception
    {
        copyMakefile("basic");
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        command.setTargets("build test");
        successRun(command, "build target", "test target");
    }

    public void testDoubleSpaceTargets() throws Exception
    {
        copyMakefile("basic");
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        command.setTargets("build  test");
        successRun(command, "build target", "test target");
    }

    public void testEnvironment() throws Exception
    {
        copyMakefile("basic");
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        command.setTargets("environment");
        command.getEnvironments().add(new ExecutableCommandConfiguration.EnvironmentConfiguration("TEST_ENV_VAR", "test variable value"));
        successRun(command, "test variable value");
    }

    public void testExplicitBuildfile() throws Exception
    {
        copyMakefile("basic", "custom.makefile");
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        command.setMakefile("custom.makefile");
        successRun(command, "build target");
    }

    public void testExplicitArguments() throws Exception
    {
        copyMakefile("basic", "custom.makefile");
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        command.setMakefile("custom.makefile");
        command.setTargets("build");
        command.setArgs("test");
        successRun(command, "build target", "test target");
    }

    public void testRunNoBuildFile() throws Exception
    {
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        failedRun(command, "No targets specified and no makefile found");
    }

    public void testRunNonExistantBuildFile() throws Exception
    {
        MakeCommandConfiguration command = new MakeCommandConfiguration();
        command.setMakefile("nope.makefile");
        failedRun(command, "make: nope.makefile: No such file or directory");
    }

    private File copyMakefile(String name) throws IOException
    {
        return copyMakefile(name, "Makefile");
    }

    private File copyMakefile(String name, String toName) throws IOException
    {
        File buildFile = copyInputToDirectory(name, "txt", baseDir);
        assertTrue(buildFile.renameTo(new File(baseDir, toName)));
        return buildFile;
    }

    private void successRun(MakeCommandConfiguration configuration, String... contents) throws Exception
    {
        successRun(new NamedArgumentCommand(configuration), contents);
    }

    private void failedRun(MakeCommandConfiguration configuration, String... contents) throws Exception
    {
        failedRun(new NamedArgumentCommand(configuration), contents);
    }
}
