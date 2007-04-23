package com.zutubi.pulse.scm.p4;

import com.zutubi.pulse.personal.PersonalBuildUI;
import com.zutubi.pulse.scm.ScmCancelledException;
import com.zutubi.pulse.scm.ScmException;

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

    public void handleStdout(String line) throws ScmException
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
