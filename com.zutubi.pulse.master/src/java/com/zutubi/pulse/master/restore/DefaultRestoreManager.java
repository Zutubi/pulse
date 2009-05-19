package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.master.bootstrap.Data;
import com.zutubi.pulse.master.util.monitor.JobManager;
import com.zutubi.pulse.master.util.monitor.Monitor;
import com.zutubi.pulse.master.util.monitor.Task;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class DefaultRestoreManager implements RestoreManager
{
    public static final String ARCHIVE_JOB_KEY = "archive";

    private static final Logger LOG = Logger.getLogger(DefaultRestoreManager.class);

    private List<Task> tasks = new LinkedList<Task>();

    private File source;
    private Archive archive;
    private File backedUpArchive;

    private File tmpDirectory;

    //TODO: bypass the UserPaths instance, set the directory directly to ease testing and minimise the dependencies.
    private Data paths = null;

    private List<ArchiveableComponent> archiveableComponents = new LinkedList<ArchiveableComponent>();

    private JobManager jobManager = null;

    public void add(ArchiveableComponent component)
    {
        archiveableComponents.add(component);
    }

    public void setRestorableComponents(List<ArchiveableComponent> components)
    {
        archiveableComponents = new LinkedList<ArchiveableComponent>(components);
    }

    public Monitor getMonitor()
    {
        return jobManager.getMonitor(ARCHIVE_JOB_KEY);
    }

    public Archive prepareRestore(File source) throws ArchiveException
    {
        try
        {
            this.source = source.getCanonicalFile();

            ArchiveFactory factory = new ArchiveFactory();
            factory.setTmpDirectory(tmpDirectory);

            archive = factory.importArchive(source);

            for (ArchiveableComponent component : archiveableComponents)
            {
                String name = component.getName();
                File archiveComponentBase = new File(archive.getBase(), name);
                if (component.exists(archiveComponentBase))
                {
                    RestoreComponentTask task = new RestoreComponentTask(component, archiveComponentBase);
                    tasks.add(task);
                }
            }

            jobManager.register(ARCHIVE_JOB_KEY, tasks);

            return archive;
        }
        catch (IOException e)
        {
            throw new ArchiveException(e);
        }
    }

    public Archive getArchive()
    {
        return archive;
    }

    public List<Task> previewRestore()
    {
        return tasks;
    }

    public void restoreArchive()
    {
        Monitor monitor = jobManager.getMonitor(ARCHIVE_JOB_KEY);
        if (monitor.isStarted())
        {
            LOG.warning("Attempted to execute a restore when a restore is already executing.  Request has been ignored.");
            return;
        }

        jobManager.start(ARCHIVE_JOB_KEY);

        if(monitor.isSuccessful())
        {
            backupSourceFile();
        }

        // cleanup the extracted archive.
        if (!FileSystemUtils.rmdir(archive.getBase()))
        {
            // Failed to cleanup the extracted archive file.  This is not desired, but not fatal.
        }
    }

    public File postRestore()
    {
        return backedUpArchive;
    }

    private void backupSourceFile()
    {
        if(source.getParentFile().equals(paths.getRestoreRoot()))
        {
            File backupRoot = paths.getBackupRoot();
            if(backupRoot.isDirectory() || backupRoot.mkdirs())
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                backedUpArchive = new File(backupRoot, String.format("restored-%s.zip", sdf.format(System.currentTimeMillis())));
                if(!backedUpArchive.exists())
                {
                    if(!source.renameTo(backedUpArchive))
                    {
                        LOG.severe("Unable to backup restore archive to '" + backedUpArchive.getAbsolutePath() + "'");
                    }
                }
                else
                {
                    LOG.severe("Unable to store backup of restore archive as a file '" + backedUpArchive.getAbsolutePath() + "' already exists.");
                }
            }
            else
            {
                LOG.severe("Unable to create backup directory '" + backupRoot.getAbsolutePath() + "'");
            }
        }
    }

    public Archive createArchive() throws ArchiveException
    {
        File archiveDirectory = new File(paths.getData(), "archives");
        ArchiveFactory factory = new ArchiveFactory();
        factory.setTmpDirectory(tmpDirectory);

        Archive archive = factory.createArchive();

        // now we fill the archive.
        for (ArchiveableComponent component : archiveableComponents)
        {
            String name = component.getName();
            File archiveComponentBase = new File(archive.getBase(), name);
            component.backup(archiveComponentBase);
        }

        factory.exportArchive(archive, archiveDirectory);

        return archive;
    }

    public void setJobManager(JobManager jobManager)
    {
        this.jobManager = jobManager;
    }

    public void setTmpDirectory(File tmpDirectory)
    {
        this.tmpDirectory = tmpDirectory;
    }

    public void setPaths(Data paths)
    {
        this.paths = paths;
    }
}
