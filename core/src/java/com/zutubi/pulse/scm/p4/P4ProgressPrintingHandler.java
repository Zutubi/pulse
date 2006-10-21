package com.zutubi.pulse.scm.p4;

import com.zutubi.pulse.personal.PersonalBuildUI;
import com.zutubi.pulse.scm.SCMException;

/**
 */
public class P4ProgressPrintingHandler extends P4ErrorDetectingHandler
{
    private PersonalBuildUI ui;

    public P4ProgressPrintingHandler(PersonalBuildUI ui, boolean throwOnStderr)
    {
        super(throwOnStderr);
        this.ui = ui;
    }

    public void handleStdout(String line) throws SCMException
    {
        if(ui != null)
        {
            ui.status(line);
        }
    }
}
