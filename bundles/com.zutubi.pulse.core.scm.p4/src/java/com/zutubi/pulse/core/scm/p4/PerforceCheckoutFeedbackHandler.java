package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmFeedbackHandler;

/**
 * Adapts between the API callback interface {@link com.zutubi.pulse.core.scm.api.ScmFeedbackHandler}
 * and the SCM API's own equivalent {@link com.zutubi.pulse.core.scm.process.api.ScmOutputHandler}
 * to report feedback during checkout/update operations.
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
        if (handler != null)
        {
            handler.checkCancelled();
        }
    }
}
