package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.ResultState;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.test.BobTestCase;

import java.io.*;

/**
 */
public class AntCommandTest extends BobTestCase
{
    File workDir;
    File outputDir;

    public void setUp() throws IOException
    {
        workDir = FileSystemUtils.createTempDirectory(getClass().getName(), ".work");
        outputDir = FileSystemUtils.createTempDirectory(getClass().getName(), ".out");
    }

    public void tearDown()
    {
        FileSystemUtils.removeDirectory(workDir);
        FileSystemUtils.removeDirectory(outputDir);
    }

    public void testBasicDefault() throws IOException
    {
        copyBuildFile("basic");
        AntCommand command = new AntCommand();
        successRun(command, "build target");
    }

    public void testBasicTargets() throws IOException
    {
        copyBuildFile("basic");
        AntCommand command = new AntCommand();
        command.setTargets("build test");
        successRun(command, "build target", "test target");
    }

    public void testDoubleSpaceTargets() throws IOException
    {
        copyBuildFile("basic");
        AntCommand command = new AntCommand();
        command.setTargets("build  test");
        successRun(command, "build target", "test target");
    }

    public void testEnvironment() throws IOException
    {
        copyBuildFile("basic");
        AntCommand command = new AntCommand();
        command.setTargets("environment");
        ExecutableCommand.Environment env = command.createEnvironment();
        env.setName("TEST_ENV_VAR");
        env.setValue("test variable value");
        successRun(command, "test variable value");
    }

    public void testExplicitBuildfile() throws IOException
    {
        copyBuildFile("basic", "custom.xml");
        AntCommand command = new AntCommand();
        command.setBuildFile("custom.xml");
        successRun(command, "build target");
    }

    public void testExplicitArguments() throws IOException
    {
        copyBuildFile("basic", "custom.xml");
        AntCommand command = new AntCommand();
        command.setBuildFile("custom.xml");
        command.setTargets("build");
        ExecutableCommand.Arg arg = command.createArg();
        arg.addText("test");
        successRun(command, "build target", "test target");
    }

    public void testRunNoBuildFile() throws IOException
    {
        AntCommand command = new AntCommand();
        failedRun(command, "Buildfile: build.xml does not exist!");
    }

    public void testRunNonExistantBuildFile() throws IOException
    {
        AntCommand command = new AntCommand();
        command.setBuildFile("nope.xml");
        failedRun(command, "Buildfile: nope.xml does not exist!");
    }

    private void failedRun(AntCommand command, String message) throws IOException
    {
        CommandResult commandResult = runCommand(command);
        assertEquals(ResultState.FAILURE, commandResult.getState());
        checkOutput(commandResult, message);
    }

    private void successRun(AntCommand command, String ...contents) throws IOException
    {
        CommandResult commandResult = runCommand(command);
        assertEquals(ResultState.SUCCESS, commandResult.getState());
        checkOutput(commandResult, contents);
    }

    private CommandResult runCommand(AntCommand command)
    {
        command.setWorkingDir(workDir);
        CommandResult commandResult = new CommandResult("test");
        command.execute(workDir, outputDir, commandResult);
        return commandResult;
    }

    private void checkOutput(CommandResult commandResult, String ...contents) throws IOException
    {
        assertTrue(commandResult.getProperties().containsKey("command line"));
        assertTrue(commandResult.getProperties().get("command line").toString().startsWith("ant"));
        FileInputStream is = new FileInputStream(new File(outputDir, "output.txt"));
        String output = IOUtils.inputStreamToString(is);
        for (String content : contents)
        {
            assertTrue(output.contains(content));
        }
    }

    private void copyBuildFile(String name) throws IOException
    {
        copyBuildFile(name, "build.xml");
    }

    private void copyBuildFile(String name, String filename) throws IOException
    {
        InputStream is = getInput(name);
        FileOutputStream os = new FileOutputStream(new File(workDir, filename));
        IOUtils.joinStreams(is, os);
    }
}
