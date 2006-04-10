package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;

import java.io.IOException;

/**
 */
public class MakeCommandTest extends CommandTestBase
{

    public void testBasicDefault() throws IOException
    {
        copyBuildFile("basic");
        MakeCommand command = new MakeCommand();
        successRun(command, "build target");
    }

    public void testBasicTargets() throws IOException
    {
        copyBuildFile("basic");
        MakeCommand command = new MakeCommand();
        command.setTargets("build test");
        successRun(command, "build target", "test target");
    }

    public void testDoubleSpaceTargets() throws IOException
    {
        copyBuildFile("basic");
        MakeCommand command = new MakeCommand();
        command.setTargets("build  test");
        successRun(command, "build target", "test target");
    }

    public void testEnvironment() throws IOException
    {
        copyBuildFile("basic");
        MakeCommand command = new MakeCommand();
        command.setTargets("environment");
        ExecutableCommand.Environment env = command.createEnvironment();
        env.setName("TEST_ENV_VAR");
        env.setValue("test variable value");
        successRun(command, "test variable value");
    }

    public void testExplicitBuildfile() throws IOException
    {
        copyBuildFile("basic", "custom.makefile");
        MakeCommand command = new MakeCommand();
        command.setMakefile("custom.makefile");
        successRun(command, "build target");
    }

    public void testExplicitArguments() throws IOException
    {
        copyBuildFile("basic", "custom.makefile");
        MakeCommand command = new MakeCommand();
        command.setMakefile("custom.makefile");
        command.setTargets("build");
        ExecutableCommand.Arg arg = command.createArg();
        arg.addText("test");
        successRun(command, "build target", "test target");
    }

    public void testRunNoBuildFile() throws IOException
    {
        MakeCommand command = new MakeCommand();
        failedRun(command, "No targets specified and no makefile found");
    }

    public void testRunNonExistantBuildFile() throws IOException
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

    protected void checkOutput(CommandResult commandResult, String ...contents) throws IOException
    {
        assertTrue(commandResult.getProperties().containsKey("command line"));
        assertTrue(commandResult.getProperties().get("command line").toString().startsWith("make"));
        super.checkOutput(commandResult, contents);
    }
}
