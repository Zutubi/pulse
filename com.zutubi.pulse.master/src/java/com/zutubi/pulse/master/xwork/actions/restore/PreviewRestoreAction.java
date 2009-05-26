package com.zutubi.pulse.master.xwork.actions.restore;

import com.zutubi.pulse.master.util.monitor.Task;
import com.zutubi.pulse.master.restore.Archive;

import java.util.List;
import java.io.File;
import java.io.IOException;

/**
 * The action that drives the preview page of the restoration process.
 */
public class PreviewRestoreAction extends RestoreActionSupport
{
    private Archive archive;

    public String getArchiveCreated()
    {
        return archive.getCreated();
    }

    public String getArchiveName()
    {
        if (archive.getOriginal() != null)
        {
            return archive.getOriginal().getName();
        }
        return "n/a";
    }

    public String getArchiveLocation()
    {
        File original = archive.getOriginal();
        if (original != null)
        {
            try
            {
                File resolved = original.getCanonicalFile();
                return resolved.getParentFile().getAbsolutePath();
            }
            catch (IOException e)
            {
                if (original.getParentFile() != null)
                {
                    return original.getParentFile().getAbsolutePath();
                }
                return "unknown";
            }
        }
        return "n/a";
    }

    public String execute() throws Exception
    {
        archive = restoreManager.getArchive();

        return SUCCESS;
    }
}
