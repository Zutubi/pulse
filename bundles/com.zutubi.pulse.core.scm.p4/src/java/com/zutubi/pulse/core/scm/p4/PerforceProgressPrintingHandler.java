package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.api.PersonalBuildUI;
import com.zutubi.pulse.core.scm.api.ScmCancelledException;

/**
 */
public class PerforceProgressPrintingHandler extends PerforceErrorDetectingHandler
{
    private PersonalBuildUI ui;

    public PerforceProgressPrintingHandler(PersonalBuildUI ui, boolean throwOnStderr)
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

    public void checkCancelled() throws ScmCancelledException
    {
    }
}
