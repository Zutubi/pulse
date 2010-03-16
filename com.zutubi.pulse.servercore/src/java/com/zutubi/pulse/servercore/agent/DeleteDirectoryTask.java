package com.zutubi.pulse.servercore.agent;

import com.zutubi.pulse.servercore.cleanup.FileDeletionService;

import java.io.File;
import java.util.Properties;

/**
 * A synchronisation task that deletes a directory.  If the directory does not
 * exist, the request is ignored (this should also handle retries).
 * <p/>
 * As deleting can take time, the directory is just marked as dead and cleaned
 * asynchronously.
 */
public class DeleteDirectoryTask extends SynchronisationTaskSupport implements SynchronisationTask
{
    private String path;
    private transient FileDeletionService fileDeletionService;

    /**
     * Creates a task to delete a directory.
     *
     * @param path path of the directory to delete
     */
    public DeleteDirectoryTask(String path)
    {
        this.path = path;
    }

    public DeleteDirectoryTask(Properties properties)
    {
        super(properties);
    }

    public Type getType()
    {
        return Type.CLEANUP_DIRECTORY;
    }

    public void execute()
    {
        File dir = new File(path);
        if (dir.exists())
        {
            fileDeletionService.delete(dir);
        }
    }

    public void setFileDeletionService(FileDeletionService fileDeletionService)
    {
        this.fileDeletionService = fileDeletionService;
    }
}
