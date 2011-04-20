package com.zutubi.pulse.core.scm.process.api;

import com.zutubi.pulse.core.scm.api.ScmFeedbackHandler;

/**
 * A helper base class for implementing {@link ScmLineHandler}.  Adapts
 * feedback to an {@link ScmFeedbackHandler} when one is available.
 */
public class ScmLineHandlerSupport extends ScmOutputHandlerSupport implements ScmLineHandler
{
    /**
     * Creates a handler with no underlying {@link ScmFeedbackHandler}.
     */
    public ScmLineHandlerSupport()
    {
    }

    /**
     * Creates a handler with the given underlying {@link ScmFeedbackHandler}.
     * 
     * @param feedbackHandler handler that will be passed status messages
     */
    public ScmLineHandlerSupport(ScmFeedbackHandler feedbackHandler)
    {
        super(feedbackHandler);
    }

    public void handleStdout(String line)
    {
        status(line);
    }

    public void handleStderr(String line)
    {
        status(line);
    }
}
