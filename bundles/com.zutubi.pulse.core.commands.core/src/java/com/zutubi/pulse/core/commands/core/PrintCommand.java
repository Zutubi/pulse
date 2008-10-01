package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.*;
import static com.zutubi.pulse.core.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.BuildProperties.PROPERTY_OUTPUT_DIR;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * A command that prints a message to stdout.
 */
public class PrintCommand extends CommandSupport
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
    private PrecapturedArtifact outputArtifact;

    public void execute(ExecutionContext context, CommandResult result)
    {
        if(terminated)
        {
            result.error("Terminated");
            return;
        }

        File outputFileDir = new File(context.getFile(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR), "command output");
        if (!outputFileDir.mkdir())
        {
            throw new BuildException("Unable to create directory for output artifact '" + outputFileDir.getAbsolutePath() + "'");
        }

        initialiseOutputArtifact();

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
    }

    private void initialiseOutputArtifact()
    {
        outputArtifact = new PrecapturedArtifact();
        outputArtifact.setName(ExecutableCommand.OUTPUT_ARTIFACT_NAME);
        outputArtifact.setType("text/plain");
        outputArtifact.setProcesses(processes);
    }

    public List<Artifact> getArtifacts()
    {
        List<Artifact> artifacts = new LinkedList<Artifact>();
        if (outputArtifact != null)
        {
            artifacts.add(outputArtifact);
        }
        return artifacts;
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
