package com.zutubi.pulse.web.restore;

import com.zutubi.pulse.bootstrap.Data;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.SetupManager;
import com.zutubi.pulse.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.restore.Archive;
import com.zutubi.pulse.restore.RestoreTaskGroup;
import com.zutubi.pulse.restore.RestoreTask;
import com.zutubi.pulse.monitor.Task;

import java.io.File;
import java.util.List;

/**
 *
 *
 */
public class PreviewRestoreAction extends RestoreActionSupport
{
    private Archive archive;

    public List<Task> getTasks()
    {
        return archiveManager.previewRestore();
    }

    public Archive getInfo()
    {
        return archive;
    }

    public String execute() throws Exception
    {
        archive = archiveManager.getArchive();

        return SUCCESS;
    }
}
