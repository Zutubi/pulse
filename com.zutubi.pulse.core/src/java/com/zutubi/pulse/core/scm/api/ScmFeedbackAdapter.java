package com.zutubi.pulse.core.scm.api;

/**
 * A noop implementation of the scm feedback handler that can be overriden
 * for specific methods.
 */
public class ScmFeedbackAdapter implements ScmFeedbackHandler
{
    public void status(String message)
    {
        // noop.
    }

    public void checkCancelled() throws ScmCancelledException
    {
        // noop.
    }
}
