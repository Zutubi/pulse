package com.zutubi.pulse.web.restore;

import com.zutubi.pulse.restore.RestoreProgressMonitor;

/**
 *
 *
 */
public class RestoreExecuteAction extends RestoreActionSupport
{
    public RestoreProgressMonitor getMonitor()
    {
        return restoreManager.getRestoreMonitor();
    }

    public String execute() throws Exception
    {
        RestoreProgressMonitor progress = restoreManager.getRestoreMonitor();
        if (!progress.isStarted())
        {
            restoreManager.executeRestore();
        }

        while (!progress.isStarted())
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
}
