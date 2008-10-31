package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmFeedbackHandler;

/**
 */
public class PerforceCheckoutHandler extends PerforceErrorDetectingHandler
{
    private ScmFeedbackHandler handler;

    public PerforceCheckoutHandler(boolean throwOnStderr, ScmFeedbackHandler handler)
    {
        super(throwOnStderr);
        this.handler = handler;
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
