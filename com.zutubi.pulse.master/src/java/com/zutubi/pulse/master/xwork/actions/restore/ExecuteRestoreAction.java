package com.zutubi.pulse.master.xwork.actions.restore;

import com.zutubi.pulse.master.util.monitor.Monitor;
import com.zutubi.pulse.master.util.monitor.Task;

import java.io.File;
import java.util.List;

/**
 * Trigger the archive restoration process.
 *
 * NOTE: This action waits for the restoration to complete before returning.
 */
public class ExecuteRestoreAction extends RestoreActionSupport
{
    private static final long WAIT_TIME = 300;

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
        return restoreManager.getMonitor();
    }

    public String execute() throws Exception
    {
        // Ensure that we behave correctly if this action is triggered a second time.
        Monitor monitor = restoreManager.getMonitor();
        if (monitor.isFinished())
        {
            backedUpArchive = restoreManager.postRestore();
            return SUCCESS;
        }

        restoreManager.restoreArchive();

        while (!monitor.isFinished())
        {
            Thread.sleep(WAIT_TIME);
        }

        backedUpArchive = restoreManager.postRestore();
        return SUCCESS;
    }
}
