package com.zutubi.pulse.servercore.cleanup;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.util.background.BackgroundServiceSupport;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
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
    private static final Logger LOG = Logger.getLogger(FileDeletionService.class);

    public static final String INDEX_FILE_NAME = "dead-index.txt";
    public static final String SUFFIX = ".dead";

    public Set<File> index = new HashSet<File>();
    public File indexFile;
    
    private ConfigurationManager configurationManager;

    public FileDeletionService()
    {
        super(I18N.format("service.name"));
    }

    @Override
    public synchronized void init()
    {
        super.init();
        
        indexFile = new File(configurationManager.getUserPaths().getData(), INDEX_FILE_NAME);
        loadIndex();
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

        return indexAndScheduleDeletion(file);
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

    private Future<Boolean> indexAndScheduleDeletion(File file)
    {
        addToIndex(file);
        return scheduleDeletion(file);
    }

    private Future<Boolean> scheduleDeletion(File file)
    {
        return getExecutorService().submit(new Delete(file));
    }

    private synchronized void addToIndex(File file)
    {
        if (index.add(file))
        {
            saveIndex();
        }
    }
    
    private synchronized void removeFromIndex(File file)
    {
        if (index.remove(file))
        {
            saveIndex();
        }
    }

    private synchronized void loadIndex()
    {
        if (indexFile.exists())
        {
            BufferedReader reader = null;
            try
            {
                reader = new BufferedReader(new FileReader(indexFile));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    // There is some paranoia here, as this index file is
                    // telling us to delete arbitrary directories.  To avoid
                    // painful mistakes, only believe entries with the expected
                    // suffix.  If the rename to the suffix version failed, we
                    // won't restart that delete (seemingly a lesser evil than
                    // deleting something we shouldn't have).
                    line = line.trim();
                    if (line.endsWith(SUFFIX))
                    {
                        File file = new File(line);
                        if (file.isAbsolute())
                        {
                            index.add(file);
                        }
                    }
                }
            }
            catch (IOException e)
            {
                LOG.severe("Unable to load from dead index '" + indexFile.getAbsolutePath() + "': " + e.getMessage(), e);
            }
            finally
            {
                IOUtils.close(reader);
            }
            
            // This save filters out the rejected index entries.
            saveIndex();
            for (File file: index)
            {
                scheduleDeletion(file);
            }
        }
    }
    
    private synchronized void saveIndex()
    {
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(indexFile);
            for (File f: index)
            {
                writer.println(f.getAbsolutePath());
            }
        }
        catch (IOException e)
        {
            LOG.severe("Unable to save to dead index '" + indexFile.getAbsolutePath() + "': " + e.getMessage(), e);
        }
        finally
        {
            IOUtils.close(writer);
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
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
                try
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
                finally
                {
                    removeFromIndex(target);
                }
            }
            return true;
        }
    }
}
