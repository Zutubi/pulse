package com.zutubi.pulse.master.cleanup;

import com.zutubi.pulse.servercore.util.background.BackgroundServiceSupport;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.RandomUtils;
import com.zutubi.i18n.Messages;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

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
public class FileDeletionService extends BackgroundServiceSupport
{
    private static final Messages I18N = Messages.getInstance(FileDeletionService.class);

    public static final String SUFFIX = ".dead";

    public FileDeletionService()
    {
        super(I18N.format("service.name"));
    }

    /**
     * Delete the specified file.  This deletion may occur now or at some
     * point in the future.
     *
     * @param file to be deleted.
     * @return a Future representing pending completion of the deletion.  The
     *         future will return true if the file has been deleted or no longer
     *         does exists, false otherwise.
     */
    public Future<Boolean> delete(File file)
    {
        if (file == null)
        {
            throw new IllegalArgumentException(I18N.format("delete.null.exception"));
        }

        if (!file.exists())
        {
            FutureTask<Boolean> task = new FutureTask<Boolean>(new Callable<Boolean>()
            {
                public Boolean call() throws Exception
                {
                    return true;
                }
            });
            task.run();
            return task;
        }

        // only rename the file if it has not already been renamed.
        if (!file.getName().endsWith(SUFFIX))
        {
            File dest = determineRenamedFile(file);
            if (file.renameTo(dest))
            {
                file = dest;
            } // else rename fails, but should not prevent us from scheduling the deletion.
        }

        return scheduleDeletion(file);
    }

    /**
     * This method indicates whether or not the specified file was previously scheduled for
     * deletion.  Note that this method does not identify all previously scheduled files
     * with 100% accuracy.  A result of true indicates that this file was previously scheduled,
     * whilst a result of false indicates that it is unlikely that this file was previously
     * scheduled for deletion.
     *
     * @param f the file in question
     * @return  true if the file was previously scheduled for deletion, false otherwise.
     */
    public boolean wasScheduledForDeletion(File f)
    {
        return f.getName().endsWith(SUFFIX);
    }

    /**
     * Determine a suitable name to rename this file to prior to deletion.  The
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
        File dest = new File(file.getAbsolutePath() + SUFFIX);
        while (dest.exists())
        {
            dest = new File(file.getAbsolutePath() + RandomUtils.randomString(i++) + SUFFIX);
        }
        return dest;
    }

    private Future<Boolean> scheduleDeletion(File file)
    {
        return getExecutorService().submit(new Delete(file));
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
