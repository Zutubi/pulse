package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.util.IOUtils;

import java.io.*;

/**
 * <class comment/>
 */
public abstract class ExecutableCommandTestBase extends CommandTestBase
{
    protected ExecutableCommandTestBase()
    {
    }

    protected ExecutableCommandTestBase(String name)
    {
        super(name);
    }

    protected CommandResult failedRun(Command command, String ...contents) throws Exception
    {
        CommandResult result = runCommand(command);
        assertEquals(ResultState.FAILURE, result.getState());
        checkOutput(result, contents);
        return result;
    }

    protected CommandResult successRun(Command command, String ...contents) throws Exception
    {
        CommandResult result = runCommand(command);
        assertEquals(ResultState.SUCCESS, result.getState());
        checkOutput(result, contents);
        return result;
    }

    /**
     * Verify that the output associated with the command result contains the specified contents.
     *
     * @param result being checked.
     * @param contents that much exist in the command results output.
     *
     * @throws java.io.IOException if there is a problem extracting the output contents.
     */
    protected void checkOutput(CommandResult result, String ...contents) throws IOException
    {
        checkArtifact(result, result.getArtifact(ExecutableCommand.OUTPUT_ARTIFACT_NAME), contents);
    }    

    /**
     * The default build filename.
     *
     */
    protected abstract String getBuildFileName();

    /**
     * The template build file extension.
     *
     */
    protected abstract String getBuildFileExt();

    protected void copyBuildFileToBaseDir(String name) throws IOException
    {
        copyBuildFile(name, getBuildFileName());
    }

    protected void copyBuildFile(String name, String filename) throws IOException
    {
        InputStream is = null;
        OutputStream os = null;
        try
        {
            is = getInput(name, getBuildFileExt());
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
