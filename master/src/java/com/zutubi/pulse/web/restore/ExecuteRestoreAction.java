package com.zutubi.pulse.web.restore;

import com.zutubi.pulse.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.bootstrap.SetupManager;
import com.zutubi.pulse.restore.feedback.TaskMonitor;
import com.zutubi.pulse.restore.RestoreTask;

import java.util.List;

/**
 *
 *
 */
public class ExecuteRestoreAction extends RestoreActionSupport
{
    private SetupManager setupManager;

    public List<RestoreTask> getTasks()
    {
        return archiveManager.previewRestore();
    }
    
    public TaskMonitor getMonitor()
    {
        return archiveManager.getTaskMonitor();
    }

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
