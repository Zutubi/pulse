package com.zutubi.pulse.core;

import com.zutubi.pulse.BuildContext;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 
 *
 */
public class ExecutableCommandTest extends PulseTestCase
{
    private File baseDirectory;
    private File outputDirectory;

    public void setUp() throws Exception
    {
        super.setUp();
        baseDirectory = FileSystemUtils.createTempDir(ExecutableCommandTest.class.getName(), ".base");
        outputDirectory = FileSystemUtils.createTempDir(ExecutableCommandTest.class.getName(), ".out");
    }

    public void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(outputDirectory);
        FileSystemUtils.rmdir(baseDirectory);
        super.tearDown();
    }

    public void testExecuteSuccessExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("echo");
        command.setArgs("hello world");
        CommandResult result = new CommandResult("success");
        execute(command, result);
        assertEquals(result.getState(), ResultState.SUCCESS);
    }

    public void testExecuteFailureExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("dir");
        command.setArgs("wtfisgoingon");
        CommandResult result = new CommandResult("failure");
        execute(command, result);
        assertEquals(ResultState.FAILURE, result.getState());
    }

    public void testExecuteSuccessExpectedNoArg() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("netstat");
        CommandResult result = new CommandResult("no arg");
        execute(command, result);
        assertEquals(result.getState(), ResultState.SUCCESS);
    }

    public void testExecuteExceptionExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("unknown");
        command.setArgs("command");
        CommandResult result = new CommandResult("exception");
        try
        {
            execute(command, result);
            fail();
        }
        catch (BuildException e)
        {
            // noop            
        }

        // verify that the env output is captured even with the command failing.
        assertEquals(1, result.getArtifacts().size());
        StoredArtifact artifact = result.getArtifacts().get(0);
        assertEquals(1, artifact.getChildren().size());
        StoredFileArtifact fileArtifact = artifact.getChildren().get(0);
        assertEquals(ExecutableCommand.ENV_NAME + "/env.txt", fileArtifact.getPath());
    }

    public void testPostProcess() throws FileLoadException
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("echo");
        command.setArgs("error: badness");

        ProcessArtifact processArtifact = command.createProcess();
        RegexPostProcessor processor = new RegexPostProcessor();
        RegexPattern regex = new RegexPattern();
        regex.setCategory("error");
        regex.setExpression("error:.*");
        processor.addRegexPattern(regex);
        processArtifact.setProcessor(processor);

        CommandResult cmdResult = new CommandResult("processed");
        execute(command, cmdResult);
        assertEquals(ResultState.FAILURE, cmdResult.getState());

        StoredArtifact artifact = cmdResult.getArtifact(Command.OUTPUT_ARTIFACT_NAME);
        List<Feature> features = artifact.getFeatures(Feature.Level.ERROR);
        assertEquals(1, features.size());
        Feature feature = features.get(0);
        assertEquals(Feature.Level.ERROR, feature.getLevel());
        assertEquals("error: badness", feature.getSummary());
    }

    public void testWorkingDir() throws IOException
    {
        File dir = new File(baseDirectory, "nested");
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

        CommandResult result = new CommandResult("work");
        execute(command, result);
        assertTrue(result.succeeded());
    }

    public void testRelativeExe() throws IOException
    {
        File dir = new File(baseDirectory, "nested");
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

        CommandResult result = new CommandResult("work");
        execute(command, result);
        assertTrue(result.succeeded());
    }

    public void testExtraPathInScope() throws IOException
    {
        File data = getTestDataFile("core", "scope", "bin");
        Scope scope = new Scope();
        scope.add(new ResourceProperty("mypath", data.getAbsolutePath(), false, true, false));

        ExecutableCommand command = new ExecutableCommand();
        command.setExe("custom");
        command.setScope(scope);

        CommandResult result = new CommandResult("work");
        execute(command, result);
        assertTrue(result.succeeded());
    }

    public void testEnvironmentVariableFromScope() throws IOException
    {
        File data = getTestDataFile("core", "scope", "bin");
        Scope scope = new Scope();
        scope.add(new ResourceProperty("mypath", data.getAbsolutePath(), false, true, false));
        scope.add(new ResourceProperty("TESTVAR", "test variable value", true, false, false));

        ExecutableCommand command = new ExecutableCommand();
        command.setExe("custom");
        command.setScope(scope);

        CommandResult result = new CommandResult("work");
        execute(command, result);
        assertTrue(result.succeeded());
        String output = getOutput();
        assertTrue(output.contains("test variable value"));
    }

    public void testEnvironmentDetailsAreCaptured() throws IOException
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("echo");
        command.setArgs("hello world");
        CommandResult result = new CommandResult("success");
        execute(command, result);

        List<StoredArtifact> artifacts = result.getArtifacts();
        assertEquals(2, artifacts.size());

        StoredArtifact artifact = artifacts.get(0);
        assertEquals(1, artifact.getChildren().size());

        StoredFileArtifact envArtifact = artifact.getChildren().get(0);
        assertEquals(ExecutableCommand.ENV_NAME + "/env.txt", envArtifact.getPath());
        assertEquals("text/plain", envArtifact.getType());

        String output = getEnv();
        assertTrue(output.contains("Command Line:"));
        assertTrue(output.contains("Process Environment:"));
        assertTrue(output.contains("Resources:"));

        artifact = artifacts.get(1);
        StoredFileArtifact outputArtifact = artifact.getChildren().get(0);
        assertEquals(Command.OUTPUT_ARTIFACT_NAME + "/output.txt", outputArtifact.getPath());
        assertEquals("text/plain", outputArtifact.getType());
    }

    public void testBuildNumberAddedToEnvironment() throws IOException
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("echo");
        CommandResult result = new CommandResult("success");
        execute(command, result, 1234);

        String output = getEnv();
        assertTrue(output.contains("PULSE_BUILD_NUMBER=1234"));
    }

    public void testBuildNumberNotAddedToEnvironmentWhenNotSpecified() throws IOException
    {
        // if we are running in pulse, then PULSE_BUILD_NUMBER will already
        // be added to the environment.
        boolean runningInPulse = System.getenv().containsKey("PULSE_BUILD_NUMBER");

        ExecutableCommand command = new ExecutableCommand();
        command.setExe("echo");
        CommandResult result = new CommandResult("success");
        execute(command, result);

        String output = getEnv();

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
        ExecutableCommand command = new ExecutableCommand();
        Scope scope = new Scope();
        ResourceProperty rp = new ResourceProperty("java.bin.dir", "somedir", false, true, false);
        scope.add(rp);
        command.setScope(scope);
        command.setExe("echo");

        CommandResult result = new CommandResult("success");
        execute(command, result, 1234);

        String output = getEnv();
        assertTrue(output.toLowerCase().contains("path=somedir" + File.pathSeparator));
    }

    public void testNoSuchExecutableOnWindows()
    {
        if(SystemUtils.IS_WINDOWS)
        {
            ExecutableCommand command = new ExecutableCommand();
            command.setExe("thisfiledoesnotexist");

            try
            {
                CommandResult result = new CommandResult("success");
                execute(command, result, 1234);
                fail();
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
    }

    public void testNoSuchWorkDirOnWindows()
    {
        if(SystemUtils.IS_WINDOWS)
        {
            ExecutableCommand command = new ExecutableCommand();
            command.setExe("dir");
            command.setWorkingDir(new File("nosuchworkdir"));

            try
            {
                CommandResult result = new CommandResult("success");
                execute(command, result, 1234);
                fail();
            }
            catch (BuildException e)
            {
                String message = e.getMessage();
                boolean java15 = message.contains("Working directory 'nosuchworkdir' does not exist");
                boolean jaav16 = message.endsWith("The directory name is invalid");
                assertTrue(java15 || jaav16);
            }
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

    private String getOutput() throws IOException
    {
        return IOUtils.fileToString(new File(outputDirectory, Command.OUTPUT_ARTIFACT_NAME + "/output.txt"));
    }

    private String getEnv() throws IOException
    {
        return IOUtils.fileToString(new File(outputDirectory, ExecutableCommand.ENV_NAME + "/env.txt"));
    }

    private void execute(ExecutableCommand command, CommandResult result)
    {
        CommandContext context = new CommandContext(new SimpleRecipePaths(baseDirectory, null), outputDirectory, null);
        command.execute(context, result);
    }

    private void execute(ExecutableCommand command, CommandResult result, long buildNumber)
    {
        BuildContext buildContext = new BuildContext();
        buildContext.setBuildNumber(buildNumber);
        CommandContext context = new CommandContext(new SimpleRecipePaths(baseDirectory, null), outputDirectory, null);
        context.setBuildContext(buildContext);

        if(buildNumber > 0)
        {
            Scope scope = new Scope();
            scope.add(new Property("build.number", Long.toString(buildNumber)));
            context.setGlobalScope(scope);
        }
        command.execute(context, result);
    }
}
