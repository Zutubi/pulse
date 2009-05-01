package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_BUILD_NUMBER;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.io.IOUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public class ExecutableCommandTest extends ExecutableCommandTestBase
{
    public void testExecuteSuccessExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("echo");
        command.setArgs("hello world");
        CommandResult result = runCommand(command);
        assertEquals(result.getState(), ResultState.SUCCESS);
    }

    public void testExecuteFailureExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe(SystemUtils.IS_WINDOWS ? "dir" : "ls");
        command.setArgs("wtfisgoingon");
        CommandResult result = runCommand(command);
        assertEquals(ResultState.FAILURE, result.getState());
    }

    public void testExecuteSuccessExpectedNoArg() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("netstat");
        CommandResult result = runCommand(command);
        assertEquals(result.getState(), ResultState.SUCCESS);
    }

    public void testExecuteExceptionExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("unknown");
        command.setArgs("command");

        CommandResult result;
        try
        {
            result = runCommand(command);
            assertTrue(result.errored());
            fail("cannot run unknown command");
        }
        catch (BuildException e)
        {
        }
    }

    public void testWorkingDir() throws Exception
    {
        File dir = new File(baseDir, "nested");
        File file;

        assertTrue(dir.mkdir());

        if (SystemUtils.IS_WINDOWS)
        {
            file = new File(dir, "list.bat");
            FileSystemUtils.createFile(file, "dir");
        }
        else
        {
            file = new File(dir, "./list.sh");
            FileSystemUtils.createFile(file, "#! /bin/sh\nls");
            FileSystemUtils.setPermissions(file, FileSystemUtils.PERMISSION_ALL_FULL);
        }

        ExecutableCommand command = new ExecutableCommand();
        command.setWorkingDir(new File("nested"));
        command.setExe(file.getPath());

        CommandResult result = runCommand(command);
        assertTrue(result.succeeded());
    }

    public void testRelativeExe() throws Exception
    {
        File dir = new File(baseDir, "nested");
        File file;
        String exe;

        assertTrue(dir.mkdir());

        if (SystemUtils.IS_WINDOWS)
        {
            exe = "list.bat";
            file = new File(dir, exe);
            FileSystemUtils.createFile(file, "dir");
        }
        else
        {
            exe = "list.sh";
            file = new File(dir, exe);
            FileSystemUtils.createFile(file, "#! /bin/sh\nls");
            FileSystemUtils.setPermissions(file, FileSystemUtils.PERMISSION_ALL_FULL);
        }

        ExecutableCommand command = new ExecutableCommand();
        command.setWorkingDir(new File("nested"));
        command.setExe(exe);

        CommandResult result = runCommand(command);
        assertTrue(result.succeeded());
    }

    public void testEnvironmentDetailsAreCaptured() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("echo");
        command.setArgs("hello world");

        CommandResult result = runCommand(command);

        List<StoredArtifact> artifacts = result.getArtifacts();
        assertEquals(2, artifacts.size());

        StoredArtifact artifact = artifacts.get(0);
        assertEquals(1, artifact.getChildren().size());

        StoredFileArtifact envArtifact = artifact.getChildren().get(0);
        assertEquals(ExecutableCommand.ENV_ARTIFACT_NAME + "/env.txt", envArtifact.getPath());
        assertEquals("text/plain", envArtifact.getType());

        checkEnv(result, "Command Line:", "Process Environment:", "Resources:");

        artifact = artifacts.get(1);
        StoredFileArtifact outputArtifact = artifact.getChildren().get(0);
        assertEquals(ExecutableCommand.OUTPUT_ARTIFACT_NAME + "/output.txt", outputArtifact.getPath());
        assertEquals("text/plain", outputArtifact.getType());
    }

    public void testBuildNumberAddedToEnvironment() throws IOException
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("echo");

        CommandResult result = runCommand(command, 1234);

        checkEnv(result, "PULSE_BUILD_NUMBER=1234");
    }

    public void testBuildNumberNotAddedToEnvironmentWhenNotSpecified() throws Exception
    {
        // if we are running in pulse, then PULSE_BUILD_NUMBER will already
        // be added to the environment.
        boolean runningInPulse = System.getenv().containsKey("PULSE_BUILD_NUMBER");

        ExecutableCommand command = new ExecutableCommand();
        command.setExe("echo");
        CommandResult result = runCommand(command);

        String output = IOUtils.fileToString(getCommandEnv(result));

        if (runningInPulse)
        {
            // should only appear once.
            assertTrue(output.indexOf("PULSE_BUILD_NUMBER") == output.lastIndexOf("PULSE_BUILD_NUMBER"));
        }
        else
        {
            // does not appear.
            assertFalse(output.contains("PULSE_BUILD_NUMBER"));
        }
    }

    public void testResourcePathsAddedToEnvironment() throws IOException
    {
        PulseExecutionContext context = new PulseExecutionContext();
        context.add(new ResourceProperty("java.bin.dir", "somedir", false, true, false));
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("echo");

        CommandResult result = runCommand(command, context);

        checkEnv(result, "path=somedir" + File.pathSeparator);
    }

    public void testNoSuchExecutableOnWindows()
    {
        if(!SystemUtils.IS_WINDOWS)
        {
            return;
        }
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("thisfiledoesnotexist");

        try
        {
            runCommand(command, 1234);
            fail("Should not be able to run non-existant exe");
        }
        catch (BuildException e)
        {
            String message = e.getMessage();
            boolean java15 = message.contains("No such executable 'thisfiledoesnotexist'");
            // In Java 1.6, the error reporting is better, so we are
            // happy to pass it on through.
            boolean java16 = message.endsWith("The system cannot find the file specified");
            assertTrue(java15 || java16);
        }
    }

    public void testNoSuchWorkDir()
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("dir");
        command.setWorkingDir(new File("nosuchworkdir"));

        try
        {
            runCommand(command, 1234);
            fail("Should not be able to run in non-existant directory");
        }
        catch (BuildException e)
        {
            assertThat(e.getMessage(), containsString("Working directory 'nosuchworkdir' does not exist"));
        }
    }

    public void testSpecifiedWorkDirIsFile() throws IOException
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("dir");

        File plainFile = new File(baseDir, "f");
        assertTrue(plainFile.createNewFile());
        command.setWorkingDir(new File("f"));

        try
        {
            runCommand(command, 1234);
            fail("Should not be able to run with a working directory that is a file");
        }
        catch (BuildException e)
        {
            assertThat(e.getMessage(), containsString("Working directory 'f' exists, but is not a directory"));
        }
    }

    public void testProcessId() throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException
    {
        if(SystemUtils.IS_WINDOWS)
        {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd");
            Process p = processBuilder.start();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class clazz = loader.loadClass("java.lang.ProcessImpl");
            assertNotNull(clazz);
            Field handleField = clazz.getDeclaredField("handle");
            handleField.setAccessible(true);
            long handle = handleField.getLong(p);
            System.out.println("handle = " + handle);
        }
    }

    public void testAcceptableNamesOnWindows()
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            return;
        }

        ExecutableCommand cmd = new ExecutableCommand();
        assertTrue(cmd.acceptableName("^"));
        assertTrue(cmd.acceptableName("<"));
        assertTrue(cmd.acceptableName(">"));
        assertTrue(cmd.acceptableName("|"));
        assertTrue(cmd.acceptableName("&"));
        assertTrue(cmd.acceptableName(" "));

        assertFalse(cmd.acceptableName("="));
    }

    public void testAcceptableNames()
    {
        ExecutableCommand cmd = new ExecutableCommand();
        assertTrue(cmd.acceptableName("a"));
        assertTrue(cmd.acceptableName("Z"));
        assertTrue(cmd.acceptableName("2"));

        assertFalse(cmd.acceptableName("env.something"));
    }

    public void testConvertNames()
    {
        ExecutableCommand cmd = new ExecutableCommand();
        assertEquals("PULSE_A", cmd.convertName("a"));
        assertEquals("PULSE_1", cmd.convertName("1"));
    }

    public void testWindowsEnvironmentNames() throws Exception
    {
        if (SystemUtils.IS_WINDOWS)
        {
            PulseExecutionContext context = new PulseExecutionContext();
            context.add(new ResourceProperty("a<>", "b", true, false, false));
            ExecutableCommand command = new ExecutableCommand();
            command.setExe("dir");

            runCommand(command, context);
        }
    }

    public void testStatusMapping() throws Exception
    {
        CommandResult result = statusMappingHelper(1, 1, ResultState.SUCCESS);
        assertEquals(ResultState.SUCCESS, result.getState());
    }

    public void testStatusMappingNoMatch() throws Exception
    {
        CommandResult result = statusMappingHelper(2, 1, ResultState.SUCCESS);
        assertEquals(ResultState.FAILURE, result.getState());
    }

    public void testStatusMappingError() throws Exception
    {
        CommandResult result = statusMappingHelper(2, 2, ResultState.ERROR);
        assertEquals(ResultState.ERROR, result.getState());
    }

    public void testArgumentCreationEmptyString()
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setArgs("");
        assertEquals(0, command.getArgs().size());
    }

    public void testArgumentCreationWhitespaceString()
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setArgs("  ");
        assertEquals(0, command.getArgs().size());
    }

    public void testArgumentCreationTrimsWhitespace()
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setArgs("  a   b ");
        assertEquals(2, command.getArgs().size());
        assertEquals("a", command.getArgs().get(0).getText());
        assertEquals("b", command.getArgs().get(1).getText());
    }

    private CommandResult statusMappingHelper(int exitCode, int mappedCode, ResultState mappedStatus) throws Exception
    {
        File jarFile = copyInputToDirectory("exit", "jar", baseDir);

        ExecutableCommand command = new ExecutableCommand();
        command.setExe("java");
        command.addArguments("-jar", jarFile.getAbsolutePath(), Integer.toString(exitCode));
        StatusMapping mapping = command.createStatusMapping();
        mapping.setCode(mappedCode);
        mapping.setStatus(mappedStatus.getPrettyString());
        return runCommand(command);
    }

    private CommandResult runCommand(ExecutableCommand command, long buildNumber)
    {
        PulseExecutionContext context = new PulseExecutionContext();
        context.addString(NAMESPACE_INTERNAL, PROPERTY_BUILD_NUMBER, Long.toString(buildNumber));
        return super.runCommand(command, context);
    }

    protected String getBuildFileName()
    {
        return null;
    }

    protected String getBuildFileExt()
    {
        return null;
    }

    protected void checkEnv(CommandResult commandResult, String ...contents) throws IOException
    {
        File outputFile =  getCommandEnv(commandResult);
        checkContents(outputFile, false, contents);
    }

    protected File getCommandEnv(CommandResult commandResult) throws IOException
    {
        return getCommandArtifact(commandResult, commandResult.getArtifact(ExecutableCommand.ENV_ARTIFACT_NAME));
    }
}
