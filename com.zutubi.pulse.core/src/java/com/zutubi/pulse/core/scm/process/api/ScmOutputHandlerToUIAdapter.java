package com.zutubi.pulse.core.scm.process.api;

import com.zutubi.pulse.core.ui.api.UserInterface;

/**
 * A class to adapt the {@link ScmOutputHandler} interface to the
 * {@link UserInterface} interface.  Reports all output from an external SCM
 * process directly to the UI.
 */
public class ScmOutputHandlerToUIAdapter extends ScmLineHandlerSupport
{
    private final UserInterface ui;

    public ScmOutputHandlerToUIAdapter(UserInterface ui)
    {
        this.ui = ui;
    }

    @Override
    public void handleStdout(String line)
    {
        if (ui != null)
        {
            ui.status(line);
        }
    }

    @Override
    public void handleStderr(String line)
    {
        if (ui != null)
        {
            ui.status(line);
        }
    }
}
