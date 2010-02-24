package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmFeedbackHandler;

/**
 */
public class PerforceCheckoutFeedbackHandler extends PerforceErrorDetectingFeedbackHandler
{
    private ScmFeedbackHandler handler;

    public PerforceCheckoutFeedbackHandler(boolean throwOnStderr, ScmFeedbackHandler handler)
    {
        super(throwOnStderr);
        this.handler = handler;
    }

    public void handleCommandLine(String line)
    {
        if (handler != null)
        {
            handler.status(">> " + line);
        }
    }

    public void handleStdout(String line)
    {
        if (handler != null)
        {
            handler.status(line);
        }
    }

    public void checkCancelled() throws ScmCancelledException
    {
        if(handler != null)
        {
            handler.checkCancelled();
        }
    }
}
