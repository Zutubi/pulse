package com.zutubi.pulse.master.cleanup;

import com.zutubi.pulse.core.Stoppable;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.RandomUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;

/**
 * The delete file processor, as the name suggests, handles the deletion of
 * files and directories.  When deleting files, it does a couple of other
 * useful things.
 * <p/>
 * Firstly, the deletion is handled on a separate thread to the calling thread.
 * This ensures that if the directory is large, its deletion does not impact on
 * performance.
 * <p/>
 * Secondly, it allows the deletion to occur across Pulse restarts.
 */
public class DeleteFileProcessor implements Stoppable
{
    public static final String SUFFIX = ".dead";

    private static final int DELETE_THREAD_COUNT = 1;

    private ThreadPoolExecutor executor = null;
    private ThreadFactory threadFactory = Executors.defaultThreadFactory();

    /**
     * Initialise the processor
     */
    public void init()
    {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(DELETE_THREAD_COUNT, threadFactory);
    }

    public void stop(boolean force)
    {
        if (force)
        {
            executor.shutdownNow();
        }
        else
        {
            executor.shutdown();
        }
    }

    /**
     * Delete the specified file.  This deletion may occur now or at some
     * point in the future.
     *
     * @param file to be deleted.
     *
     * @return a Future representing pending completion of the deletion.
     *
     * @throws IOException on error
     */
    public Future<Boolean> delete(File file) throws IOException
    {
        // only rename the file if it has not already been renamed.
        if (!file.getName().endsWith(SUFFIX))
        {
            File dest = determineRenamedFile(file);
            if (!file.renameTo(dest))
            {
                // if rename fails, maybe we should try to delete it inline?
                throw new IOException("Failed to rename file from '" + file.getCanonicalPath() + "' to '" + dest.getCanonicalPath() + "'");
            }
            file = dest;
        }

        return scheduleDeletion(file);
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    /**
     * Determine a sutable name to rename this file to prior to deletion.  The
     * criteria are that a) the file does not exist, b) the file name has the
     * suffix '.dead' so that it can later be identified as a file/directory that
     * we attempted to delete at some stage, and hence is safe to delete.
     *
     * @param file to be renamed
     * @return the new file to which the original can be renamed.
     */
    private File determineRenamedFile(File file)
    {
        int i = 1;
        File dest = new File(file.getParentFile(), file.getName() + SUFFIX);
        while (dest.exists())
        {
            dest = new File(file.getParentFile(), file.getName() + RandomUtils.randomString(i++) + SUFFIX);
        }
        return dest;
    }

    private Future<Boolean> scheduleDeletion(File file)
    {
        return executor.submit(new Delete(file));
    }

    private class Delete implements Callable<Boolean>
    {
        private File target;

        private Delete(File target)
        {
            this.target = target;
        }

        public Boolean call() throws Exception
        {
            if (target.exists())
            {
                if (target.isFile())
                {
                    return target.delete();
                }
                else // target.isDirectory()
                {
                    return FileSystemUtils.rmdir(target);
                }
            }
            return true;
        }
    }
}
