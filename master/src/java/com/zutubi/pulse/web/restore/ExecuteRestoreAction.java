package com.zutubi.pulse.web.restore;

import com.zutubi.pulse.restore.ProgressMonitor;
import com.zutubi.pulse.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.bootstrap.SetupManager;

/**
 *
 *
 */
public class ExecuteRestoreAction extends RestoreActionSupport
{
    public ProgressMonitor getMonitor()
    {
        return archiveManager.getMonitor();
    }

    private SetupManager setupManager;

    public String execute() throws Exception
    {
        ((DefaultSetupManager)setupManager).doExecuteRestorationRequest();

        while (!getMonitor().isStarted())
        {
            // this is to give the restore an opportunity to start before we return to the user.
            pause(200);
        }

        return SUCCESS;
    }

    private void pause(long time)
    {
        try
        {
            Thread.sleep(time);
        }
        catch (InterruptedException e)
        {
            // noop.
        }
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
