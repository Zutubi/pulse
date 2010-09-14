package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.ui.api.UserInterface;

/**
 */
public class PerforceProgressPrintingFeedbackHandler extends PerforceErrorDetectingFeedbackHandler
{
    private UserInterface ui;

    public PerforceProgressPrintingFeedbackHandler(UserInterface ui, boolean throwOnStderr)
    {
        super(throwOnStderr);
        this.ui = ui;
    }

    public void handleStdout(String line)
    {
        if(ui != null)
        {
            ui.status(line);
        }
    }
}
