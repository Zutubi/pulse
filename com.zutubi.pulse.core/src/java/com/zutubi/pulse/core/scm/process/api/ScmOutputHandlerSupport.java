package com.zutubi.pulse.core.scm.process.api;

import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmFeedbackHandler;

/**
 * A helper base class for implementing {@link ScmOutputHandler}.  Adapts
 * feedback to an {@link ScmFeedbackHandler} when one is available.
 */
public class ScmOutputHandlerSupport implements ScmOutputHandler
{
    private int exitCode;
    private ScmFeedbackHandler scmHandler;

    /**
     * Creates a handler with no underlying {@link ScmFeedbackHandler}.
     */
    public ScmOutputHandlerSupport()
    {
    }

    /**
     * Creates a handler with the given underlying {@link ScmFeedbackHandler}.
     * 
     * @param feedbackHandler handler that will be passed status messages
     */
    public ScmOutputHandlerSupport(ScmFeedbackHandler feedbackHandler)
    {
        this.scmHandler = feedbackHandler;
    }

    public void handleCommandLine(String line)
    {
        if (scmHandler != null)
        {
            scmHandler.status(">> " + line);
        }
    }

    public void handleStdout(String line)
    {
        if (scmHandler != null)
        {
            scmHandler.status(line);
        }
    }

    public void handleStderr(String line)
    {
        if (scmHandler != null)
        {
            scmHandler.status(line);
        }
    }

    public void handleExitCode(int code)
    {
        this.exitCode = code;
    }

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
}
