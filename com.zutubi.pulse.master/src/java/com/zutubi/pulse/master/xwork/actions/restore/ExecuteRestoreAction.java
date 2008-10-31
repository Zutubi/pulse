package com.zutubi.pulse.master.xwork.actions.restore;

import com.zutubi.pulse.master.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.master.bootstrap.SetupManager;
import com.zutubi.pulse.master.util.monitor.Monitor;
import com.zutubi.pulse.master.util.monitor.Task;

import java.io.File;
import java.util.List;

/**
 *
 *
 */
public class ExecuteRestoreAction extends RestoreActionSupport
{
    private SetupManager setupManager;
    private File backedUpArchive;

    public boolean isArchiveBackedUp()
    {
        return backedUpArchive != null;
    }

    public File getBackedUpArchive()
    {
        return backedUpArchive;
    }

    public List<Task> getTasks()
    {
        return restoreManager.previewRestore();
    }
    
    public Monitor getMonitor()
    {
        return restoreManager.getTaskMonitor();
    }

    public String execute() throws Exception
    {
        // Ensure that we behave correctly if this action is triggered a second time.
        Monitor monitor = restoreManager.getTaskMonitor();
        if (monitor.isFinished())
        {
            return SUCCESS;
        }

        if (monitor.isStarted())
        {
            return "wait";
        }
        
        ((DefaultSetupManager)setupManager).doExecuteRestorationRequest();
        backedUpArchive = restoreManager.postRestore();

        return SUCCESS;
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
