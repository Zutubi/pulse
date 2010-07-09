package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.api.PersonalBuildUI;

/**
 */
public class PerforceProgressPrintingFeedbackHandler extends PerforceErrorDetectingFeedbackHandler
{
    private PersonalBuildUI ui;

    public PerforceProgressPrintingFeedbackHandler(PersonalBuildUI ui, boolean throwOnStderr)
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
