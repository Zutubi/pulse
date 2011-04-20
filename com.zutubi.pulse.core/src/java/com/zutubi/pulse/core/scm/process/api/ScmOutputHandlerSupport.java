package com.zutubi.pulse.core.scm.process.api;

import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmFeedbackHandler;

/**
 * A helper base class for implementing {@link ScmOutputHandler}.  Adapts
 * feedback to an {@link ScmFeedbackHandler} when one is available.
 */
public class ScmOutputHandlerSupport implements ScmOutputHandler
{
    private ScmFeedbackHandler scmHandler;
    private String commandLine;
    private int exitCode;

    public ScmOutputHandlerSupport()
    {
    }

    public ScmOutputHandlerSupport(ScmFeedbackHandler scmHandler)
    {
        this.scmHandler = scmHandler;
    }

    public void handleCommandLine(String line)
    {
        commandLine = line;
        status(">> " + line);
    }

    /**
     * Returns the command line used to run the external scm process.
     * 
     * @return the scm tool command line
     */
    public String getCommandLine()
    {
        return commandLine;
    }

    public void handleExitCode(int code)
    {
        this.exitCode = code;
    }

    /**
     * Returns the exit code of external scm process.
     * 
     * @return the exit code captured from the external process
     */
    public int getExitCode()
    {
        return exitCode;
    }

    public void checkCancelled() throws ScmCancelledException
    {
        if (scmHandler != null)
        {
            scmHandler.checkCancelled();
        }
    }
    
    protected void status(String status)
    {
        if (scmHandler != null)
        {
            scmHandler.status(status);
        }
    }
}
