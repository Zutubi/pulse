package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.SystemUtils;
import com.zutubi.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;

/**
 * 
 *
 */
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

        CommandResult result = null;
        try
        {
            result = runCommand(command);
            assertTrue(result.errored());
        }
        catch (BuildException e)
        {
            fail(e.getMessage());      
        }

        // verify that the env output is captured even with the command failing.
        assertEquals(1, result.getArtifacts().size());
        StoredArtifact artifact = result.getArtifacts().get(0);
        assertEquals(1, artifact.getChildren().size());
        StoredFileArtifact fileArtifact = artifact.getChildren().get(0);
        assertEquals(ExecutableCommand.ENV_ARTIFACT_NAME + "/env.txt", fileArtifact.getPath());
    }

    public void testPostProcess() throws Exception
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

        CommandResult cmdResult = runCommand(command);
        assertEquals(ResultState.FAILURE, cmdResult.getState());

        StoredArtifact artifact = cmdResult.getArtifact(ExecutableCommand.OUTPUT_ARTIFACT_NAME);
        List<Feature> features = artifact.getFeatures(Feature.Level.ERROR);
        assertEquals(1, features.size());
        Feature feature = features.get(0);
        assertEquals(Feature.Level.ERROR, feature.getLevel());
        assertEquals("error: badness", feature.getSummary());
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

    public void testExtraPathInScope() throws Exception
    {
/*
        File data = getTestDataFile("scope", "bin");
        System.out.println(data.getAbsolutePath());
        assertTrue(data.isDirectory());

        Scope scope = new Scope();
        scope.add(new ResourceProperty("mypath", data.getAbsolutePath(), false, true, false));

        ExecutableCommand command = new ExecutableCommand();
        command.setExe("custom");
        command.setScope(scope);

        CommandResult result = runCommand(command);
        assertTrue(result.succeeded());
*/
    }

    public void testEnvironmentVariableFromScope() throws Exception
    {
/*
        File data = getTestDataFile("scope", "bin");
        assertTrue(data.isDirectory());

        Scope scope = new Scope();
        scope.add(new ResourceProperty("mypath", data.getAbsolutePath(), false, true, false));
        scope.add(new ResourceProperty("TESTVAR", "test variable value", true, false, false));

        ExecutableCommand command = new ExecutableCommand();
        command.setExe("custom");
        command.setScope(scope);

        CommandResult result = runCommand(command);
        assertTrue(result.succeeded());

        checkOutput(result, "test variable value");
*/
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
        ExecutionContext context = new ExecutionContext();
        ExecutableCommand command = new ExecutableCommand();
        PulseScope scope = new PulseScope(context.getScope());
        scope.add(new ResourceProperty("java.bin.dir", "somedir", false, true, false));
        command.setScope(scope);
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

        CommandResult result;
        result = runCommand(command, 1234);
        assertTrue(result.errored());

        List<Feature> features = result.getFeatures(Feature.Level.ERROR);
        assertEquals(1, features.size());
        String message = features.get(0).getSummary();
        boolean java15 = message.contains("No such executable 'thisfiledoesnotexist'");
        // In Java 1.6, the error reporting is better, so we are
        // happy to pass it on through.
        boolean java16 = message.endsWith("The system cannot find the file specified");
        assertTrue(java15 || java16);
    }

    public void testNoSuchWorkDirOnWindows()
    {
        if(!SystemUtils.IS_WINDOWS)
        {
            return;
        }

        ExecutableCommand command = new ExecutableCommand();
        command.setExe("dir");
        command.setWorkingDir(new File("nosuchworkdir"));

        CommandResult result;
        result = runCommand(command, 1234);

        assertTrue(result.errored());
        List<Feature> features = result.getFeatures(Feature.Level.ERROR);
        assertEquals(1, features.size());
        String message = features.get(0).getSummary();
        boolean java15 = message.contains("Working directory 'nosuchworkdir' does not exist");
        boolean java16 = message.endsWith("The directory name is invalid");
        assertTrue(java15 || java16);
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
            ExecutionContext context = new ExecutionContext();
            ExecutableCommand command = new ExecutableCommand();
            PulseScope scope = new PulseScope(context.getScope());
            scope.add(new ResourceProperty("a<>", "b", true, false, false));
            command.setScope(scope);
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

    private CommandResult statusMappingHelper(int exitCode, int mappedCode, ResultState mappedStatus) throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("java");
        command.addArguments("-jar", getTestDataFile("bundles/command-core", "exit", "jar").getAbsolutePath(), Integer.toString(exitCode));
        StatusMapping mapping = command.createStatusMapping();
        mapping.setCode(mappedCode);
        mapping.setStatus(mappedStatus.getPrettyString());
        return runCommand(command);
    }

    private CommandResult runCommand(ExecutableCommand command, long buildNumber)
    {
        ExecutionContext context = new ExecutionContext();
        context.addInternalString(BuildProperties.PROPERTY_BUILD_NUMBER, Long.toString(buildNumber));
        PulseScope scope = new PulseScope(context.getScope());
        command.setScope(scope);
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

    protected File getTestDataFile(String testName, String extension)
    {
        URL resource = getClass().getResource("ExecutableCommandLoaderTest.testExecutableArgs.xml");
        File moduleDir = new File(resource.getPath().substring(0, resource.getPath().lastIndexOf("command-core") + 12));
        File dataFile = new File(moduleDir, FileSystemUtils.composeFilename("src", "test", getClass().getName().replace('.', File.separatorChar) + "." + testName + "." + extension));
        System.out.println(dataFile.getAbsolutePath() + " : exists: " + dataFile.exists());
        return dataFile;
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
