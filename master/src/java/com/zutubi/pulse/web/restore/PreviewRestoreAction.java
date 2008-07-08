package com.zutubi.pulse.web.restore;

import com.zutubi.pulse.restore.Archive;
import com.zutubi.pulse.monitor.Task;

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
        return restoreManager.previewRestore();
    }

    public Archive getInfo()
    {
        return archive;
    }

    public String execute() throws Exception
    {
        archive = restoreManager.getArchive();

        return SUCCESS;
    }
}
