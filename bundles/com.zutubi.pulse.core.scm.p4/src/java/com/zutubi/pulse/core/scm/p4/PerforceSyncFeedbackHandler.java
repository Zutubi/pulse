package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.process.api.ScmLineHandler;
import com.zutubi.pulse.core.ui.api.UserInterface;

/**
 * A handler for the p4 sync operation that just passes the output through to
 * the UI.
 */
public class PerforceSyncFeedbackHandler implements ScmLineHandler
{
    private UserInterface ui;
    private String commandLine;
    private boolean resolveRequired = false;
    private boolean errorEncountered = false;

    public PerforceSyncFeedbackHandler(UserInterface ui)
    {
        this.ui = ui;
    }

    public void handleCommandLine(String line)
    {
        commandLine = line;
    }

    public void handleStdout(String line)
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

    public void handleStderr(String line)
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

    public void handleExitCode(int code) throws ScmException
    {
        String command = commandLine == null ? "p4 process" : "'" + commandLine + "'";
        if(code != 0)
        {
            throw new ScmException(command + " returned non-zero exit code: " + code);
        }
        else if(errorEncountered)
        {
            throw new ScmException(command + " reported errors");
        }
    }

    public void checkCancelled() throws ScmCancelledException
    {
    }

    public boolean isResolveRequired()
    {
        return resolveRequired;
    }
}
