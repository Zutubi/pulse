package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * A command that prints a message to stdout.
 */
public class
        PrintCommand extends CommandSupport
{
    /**
     * The message to print.
     */
    private String message;
    /**
     * If true, add a new line after printing the message.
     */
    private boolean addNewline = true;
    /**
     * Post processors.
     */
    private List<ProcessArtifact> processes = new LinkedList<ProcessArtifact>();

    private boolean terminated = false;

    public void execute(long recipeId, CommandContext context, CommandResult result)
    {
        if(terminated)
        {
            result.error("Terminated");
            return;
        }

        File outputFileDir = new File(context.getOutputDir(), "command output");
        if (!outputFileDir.mkdir())
        {
            throw new BuildException("Unable to create directory for output artifact '" + outputFileDir.getAbsolutePath() + "'");
        }

        File outputFile = new File(outputFileDir, "output.txt");
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(outputFile);
            writer.write(message);
            if(addNewline)
            {
                writer.println();
            }
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
        finally
        {
            IOUtils.close(writer);
        }

        ProcessSupport.postProcess(processes, outputFileDir, outputFile, result, context);
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public void setAddNewLine(boolean addNewline)
    {
        this.addNewline = addNewline;
    }

    public ProcessArtifact createProcess()
    {
        ProcessArtifact p = new ProcessArtifact();
        processes.add(p);
        return p;
    }

    public void terminate()
    {
        terminated = true;
    }
}
