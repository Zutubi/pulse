package com.zutubi.pulse.web.restore;

import com.zutubi.pulse.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.bootstrap.SetupManager;
import com.zutubi.pulse.restore.RestoreTask;
import com.zutubi.pulse.restore.feedback.TaskMonitor;

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
        backedUpArchive = archiveManager.postRestore();
        return SUCCESS;
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
