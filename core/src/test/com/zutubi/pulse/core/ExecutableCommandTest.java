package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.util.FileSystemUtils;
import junit.framework.TestCase;

import java.io.File;
import java.util.List;

/**
 * 
 *
 */
public class ExecutableCommandTest extends TestCase
{
    private File baseDirectory;
    private File outputDirectory;

    public void setUp() throws Exception
    {
        super.setUp();
        baseDirectory = FileSystemUtils.createTempDirectory(ExecutableCommandTest.class.getName(), ".base");
        outputDirectory = FileSystemUtils.createTempDirectory(ExecutableCommandTest.class.getName(), ".out");
    }

    public void tearDown() throws Exception
    {
        FileSystemUtils.removeDirectory(outputDirectory);
        FileSystemUtils.removeDirectory(baseDirectory);
        super.tearDown();
    }

    public void testExecuteSuccessExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("echo");
        command.setArgs("hello world");
        CommandResult result = new CommandResult("success");
        command.execute(baseDirectory, outputDirectory, result);
        assertEquals(result.getState(), ResultState.SUCCESS);
    }

    public void testExecuteFailureExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("dir");
        command.setArgs("w");
        CommandResult result = new CommandResult("failure");
        command.execute(baseDirectory, outputDirectory, result);
        assertEquals(result.getState(), ResultState.FAILURE);
    }

    public void testExecuteSuccessExpectedNoArg() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("netstat");
        CommandResult result = new CommandResult("no arg");
        command.execute(baseDirectory, outputDirectory, result);
        assertEquals(result.getState(), ResultState.SUCCESS);
    }

    public void testExecuteExceptionExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("unknown");
        command.setArgs("command");
        try
        {
            command.execute(baseDirectory, outputDirectory, new CommandResult("exception"));
            assertTrue(false);
        }
        catch (BuildException e)
        {
            // noop            
        }
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
        command.execute(baseDirectory, outputDirectory, cmdResult);
        assertEquals(ResultState.FAILURE, cmdResult.getState());

        StoredArtifact artifact = cmdResult.getArtifact(ExecutableCommand.OUTPUT_NAME);
        List<Feature> features = artifact.getFeatures(Feature.Level.ERROR);
        assertEquals(1, features.size());
        Feature feature = features.get(0);
        assertEquals(Feature.Level.ERROR, feature.getLevel());
        assertEquals("error: badness", feature.getSummary());
    }
}
