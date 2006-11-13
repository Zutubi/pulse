package com.zutubi.pulse.scm.p4;

import com.zutubi.pulse.personal.PersonalBuildUI;
import com.zutubi.pulse.scm.SCMCancelledException;
import com.zutubi.pulse.scm.SCMException;

/**
 * A handler for the p4 sync operation that just passes the output through to
 * the UI.
 */
public class P4SyncHandler implements P4Handler
{
    private PersonalBuildUI ui;
    private boolean resolveRequired = false;
    private boolean errorEncountered = false;

    public P4SyncHandler(PersonalBuildUI ui)
    {
        this.ui = ui;
    }

    public void handleStdout(String line) throws SCMException
    {
        if(ui != null)
        {
            if(line.startsWith("...") && line.contains("must resolve"))
            {
                resolveRequired = true;
            }
            
            ui.status(line);
        }
    }

    public void handleStderr(String line) throws SCMException
    {
        if(ui != null)
        {
            if(line.contains("ile(s) up-to-date."))
            {
                ui.status(line);
            }
            else
            {
                ui.error(line);
                errorEncountered = true;
            }
        }
    }

    public void handleExitCode(int code) throws SCMException
    {
        if(code != 0)
        {
            throw new SCMException("p4 process returned non-zero exit code: " + code);
        }
        else if(errorEncountered)
        {
            throw new SCMException("p4 process reported errors");
        }
    }

    public void checkCancelled() throws SCMCancelledException
    {
    }

    public boolean isResolveRequired()
    {
        return resolveRequired;
    }
}
