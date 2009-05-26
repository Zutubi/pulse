package com.zutubi.pulse.master.xwork.actions.restore;

import com.zutubi.pulse.master.util.monitor.Monitor;
import com.zutubi.pulse.master.util.monitor.Task;
import com.opensymphony.webwork.interceptor.ExecuteAndWaitInterceptor;

import java.io.File;
import java.util.List;

/**
 * Trigger the archive restoration process.
 */
public class ExecuteRestoreAction extends RestoreActionSupport
{
    private File backedUpArchive;

    public boolean isArchiveBackedUp()
    {
        return backedUpArchive != null;
    }

    public File getBackedUpArchive()
    {
        return backedUpArchive;
    }

    public Monitor getMonitor()
    {
        return restoreManager.getMonitor();
    }

    public String execute() throws Exception
    {
        Monitor monitor = restoreManager.getMonitor();
        if (monitor.isFinished())
        {
            backedUpArchive = restoreManager.getBackedupArchive();
            return SUCCESS;
        }
        if (monitor.isStarted())
        {
            return ExecuteAndWaitInterceptor.WAIT;
        }

        restoreManager.restoreArchive();
        backedUpArchive = restoreManager.getBackedupArchive();

        return SUCCESS;
    }
}
