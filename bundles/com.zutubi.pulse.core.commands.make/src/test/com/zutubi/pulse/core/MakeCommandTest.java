package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.core.ExecutableCommand;
import com.zutubi.pulse.core.commands.core.ExecutableCommandTestBase;
import com.zutubi.pulse.core.model.CommandResult;

import java.io.IOException;

/**
 */
public class MakeCommandTest extends ExecutableCommandTestBase
{
    public void testBasicDefault() throws Exception
    {
        copyBuildFileToBaseDir("basic");
        MakeCommand command = new MakeCommand();
        successRun(command, "build target");
    }

    public void testBasicTargets() throws Exception
    {
        copyBuildFileToBaseDir("basic");
        MakeCommand command = new MakeCommand();
        command.setTargets("build test");
        successRun(command, "build target", "test target");
    }

    public void testDoubleSpaceTargets() throws Exception
    {
        copyBuildFileToBaseDir("basic");
        MakeCommand command = new MakeCommand();
        command.setTargets("build  test");
        successRun(command, "build target", "test target");
    }

    public void testEnvironment() throws Exception
    {
        copyBuildFileToBaseDir("basic");
        MakeCommand command = new MakeCommand();
        command.setTargets("environment");
        ExecutableCommand.Environment env = command.createEnvironment();
        env.setName("TEST_ENV_VAR");
        env.setValue("test variable value");
        successRun(command, "test variable value");
    }

    public void testExplicitBuildfile() throws Exception
    {
        copyBuildFile("basic", "custom.makefile");
        MakeCommand command = new MakeCommand();
        command.setMakefile("custom.makefile");
        successRun(command, "build target");
    }

    public void testExplicitArguments() throws Exception
    {
        copyBuildFile("basic", "custom.makefile");
        MakeCommand command = new MakeCommand();
        command.setMakefile("custom.makefile");
        command.setTargets("build");
        ExecutableCommand.Arg arg = command.createArg();
        arg.setText("test");
        successRun(command, "build target", "test target");
    }

    public void testRunNoBuildFile() throws Exception
    {
        MakeCommand command = new MakeCommand();
        failedRun(command, "No targets specified and no makefile found");
    }

    public void testRunNonExistantBuildFile() throws Exception
    {
        MakeCommand command = new MakeCommand();
        command.setMakefile("nope.makefile");
        failedRun(command, "make: nope.makefile: No such file or directory");
    }

    protected String getBuildFileName()
    {
        return "Makefile";
    }

    protected String getBuildFileExt()
    {
        return "txt";
    }

    protected void checkOutput(CommandResult result, String ...contents) throws IOException
    {
        assertTrue(result.getProperties().containsKey("command line"));
        assertTrue(result.getProperties().get("command line").toString().contains("make"));
        super.checkOutput(result, contents);
    }
}
