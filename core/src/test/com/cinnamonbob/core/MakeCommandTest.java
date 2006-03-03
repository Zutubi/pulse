package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.ResultState;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.test.BobTestCase;

import java.io.*;

/**
 */
public class MakeCommandTest extends BobTestCase
{
    File baseDir;
    File outputDir;

    public void setUp() throws IOException
    {
        baseDir = FileSystemUtils.createTempDirectory(getClass().getName(), ".work");
        outputDir = FileSystemUtils.createTempDirectory(getClass().getName(), ".out");
    }

    public void tearDown() throws IOException
    {
        removeDirectory(baseDir);
        removeDirectory(outputDir);
    }

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

    private void failedRun(ExecutableCommand command, String message) throws IOException
    {
        CommandResult commandResult = runCommand(command);
        assertEquals(ResultState.FAILURE, commandResult.getState());
        checkOutput(commandResult, message);
    }

    private void successRun(ExecutableCommand command, String ...contents) throws IOException
    {
        CommandResult commandResult = runCommand(command);
        assertEquals(ResultState.SUCCESS, commandResult.getState());
        checkOutput(commandResult, contents);
    }

    private CommandResult runCommand(ExecutableCommand command)
    {
        command.setWorkingDir(baseDir);
        CommandResult commandResult = new CommandResult("test");
        command.execute(baseDir, outputDir, commandResult);
        return commandResult;
    }

    private void checkOutput(CommandResult commandResult, String ...contents) throws IOException
    {
        assertTrue(commandResult.getProperties().containsKey("command line"));
        assertTrue(commandResult.getProperties().get("command line").toString().startsWith("make"));
        FileInputStream is = new FileInputStream(new File(outputDir, "output.txt"));
        String output = IOUtils.inputStreamToString(is);
        for (String content : contents)
        {
            if (!output.contains(content))
            {
                fail("Output '" + output + "' does not contain '" + content + "'");
            }
        }
    }

    private void copyBuildFile(String name) throws IOException
    {
        copyBuildFile(name, "Makefile");
    }

    private void copyBuildFile(String name, String filename) throws IOException
    {
        InputStream is = null;
        OutputStream os = null;
        try
        {
            is = getInput(name, "txt");
            os = new FileOutputStream(new File(baseDir, filename));
            IOUtils.joinStreams(is, os);
        }
        finally
        {
            IOUtils.close(is);
            IOUtils.close(os);
        }
    }
}
