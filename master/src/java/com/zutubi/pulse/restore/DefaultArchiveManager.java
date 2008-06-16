package com.zutubi.pulse.restore;

import com.zutubi.pulse.bootstrap.Data;
import com.zutubi.pulse.monitor.JobRunner;
import com.zutubi.pulse.monitor.Monitor;
import com.zutubi.pulse.monitor.Task;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class DefaultArchiveManager implements ArchiveManager
{
    private List<Task> tasks = new LinkedList<Task>();

    private File source;
    private Archive archive;
    private File backedUpArchive;

    private File tmpDirectory;

    //TODO: bypass the UserPaths instance, set the directory directly to ease testing and minimise the dependencies.
    private Data paths = null;

    private List<ArchiveableComponent> archiveableComponents = new LinkedList<ArchiveableComponent>();

    private JobRunner jobRunner = new JobRunner();

    private static final Logger LOG = Logger.getLogger(DefaultArchiveManager.class);

    public void add(ArchiveableComponent component)
    {
        archiveableComponents.add(component);
    }

    public void setRestorableComponents(List<ArchiveableComponent> components)
    {
        archiveableComponents = new LinkedList<ArchiveableComponent>(components);
    }

    public Monitor getTaskMonitor()
    {
        return jobRunner.getMonitor();
    }

    public Archive prepareRestore(File source) throws ArchiveException
    {
        this.source = source;

        ArchiveFactory factory = new ArchiveFactory();
        factory.setTmpDirectory(tmpDirectory);

        archive = factory.importArchive(source);

        for (ArchiveableComponent component : archiveableComponents)
        {
            String name = component.getName();
            File archiveComponentBase = new File(archive.getBase(), name);

            // does it matter if this does not exist, do we need to process something regardless?

            RestoreComponentTask task = new RestoreComponentTask(component, archiveComponentBase);
            tasks.add(task);
        }

        return archive;
    }

    public Archive getArchive()
    {
        return archive;
    }

    public List<Task> previewRestore()
    {
        // Check which of the restorable components is represented within the backup.

        // return a list of tasks that need to be processed.

        return tasks;
    }

    public void restoreArchive()
    {
        Monitor monitor = getTaskMonitor();
        if (monitor.isStarted())
        {
            LOG.warning("Attempted to execute an executing upgrade.  Request has been ignored.");
            return;
        }

        jobRunner.run(tasks);

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
        factory.setArchiveDirectory(archiveDirectory);
        factory.setTmpDirectory(tmpDirectory);

        Archive archive = factory.createArchive();

        // now we fill the archive.
        for (ArchiveableComponent component : archiveableComponents)
        {
            String name = component.getName();
            File archiveComponentBase = new File(archive.getBase(), name);
            component.backup(archiveComponentBase);
        }

        factory.exportArchive(archive);

        return archive;
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
