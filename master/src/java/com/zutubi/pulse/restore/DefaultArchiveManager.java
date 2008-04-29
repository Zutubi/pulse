package com.zutubi.pulse.restore;

import com.zutubi.pulse.bootstrap.Data;
import com.zutubi.pulse.restore.feedback.Feedback;
import com.zutubi.pulse.restore.feedback.FeedbackProvider;
import com.zutubi.pulse.restore.feedback.TaskMonitor;
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
    private TaskMonitor taskMonitor = new TaskMonitor();

    private List<RestoreTask> tasks = new LinkedList<RestoreTask>();

    private File source;
    private Archive archive;
    private File backedUpArchive;

    private File tmpDirectory;

    //TODO: bypass the UserPaths instance, set the directory directly to ease testing and minimise the dependencies.
    private Data paths = null;

    private List<ArchiveableComponent> archiveableComponents = new LinkedList<ArchiveableComponent>();

    private static final Logger LOG = Logger.getLogger(DefaultArchiveManager.class);

    public void add(ArchiveableComponent component)
    {
        archiveableComponents.add(component);
    }

    public void setRestorableComponents(List<ArchiveableComponent> components)
    {
        archiveableComponents = new LinkedList<ArchiveableComponent>(components);
    }

    public TaskMonitor getTaskMonitor()
    {
        return taskMonitor;
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
            final Feedback feedback = taskMonitor.add(task);
            if (component instanceof FeedbackProvider)
            {
                ((FeedbackProvider) component).setFeedback(feedback);
            }

            tasks.add(task);
        }

        return archive;
    }

    public Archive getArchive()
    {
        return archive;
    }

    public List<RestoreTask> previewRestore()
    {
        // Check which of the restorable components is represented within the backup.

        // return a list of tasks that need to be processed.

        return tasks;
    }

    public void restoreArchive()
    {
        if (taskMonitor.isStarted())
        {
            LOG.warning("Attempted to execute an executing upgrade.  Request has been ignored.");
            return;
        }

        taskMonitor.start();

        // this task listener is here to as part of the synchronisation process with the UpgradeManager.  They both
        // execute a sequence of tasks, both of which require monitoring, so both of which should be the same / extracted.
        TaskListener listener = new DelegateTaskListener();

        List<RestoreTask> tasksToExecute = tasks;

        boolean abort = false;
        for (RestoreTask task : tasksToExecute)
        {
            try
            {
                if (!abort)
                {
                    final Feedback feedback = taskMonitor.started(task);

                    // some of the components are able to provide finer grained feedback on the
                    // tasks progress. If this is the case, hook it up!.
                    ArchiveableComponent component = task.getComponent();
                    if (component instanceof FeedbackProvider)
                    {
                        ((FeedbackProvider) component).setFeedback(feedback);
                    }

                    try
                    {
                        LOG.info("Executing restore task: " + task.getName());
                        task.execute();
                    }
                    catch (ArchiveException e)
                    {
                        feedback.setStatusMessage(e.getMessage());
                        throw e;
                    }
                    catch (Throwable t)
                    {
                        throw new ArchiveException(t);
                    }

                    if (task.hasFailed())
                    {
                        // use an exception to break out to the task failure handling.
                        StringBuffer errors = new StringBuffer();
                        String sep = "\n";
                        for (String error : task.getErrors())
                        {
                            errors.append(sep);
                            errors.append(error);
                        }

                        String message = "RestoreTask '" + task.getName() + "' is marked as failed. The " +
                                "following errors were recorded:" + errors.toString();
                        feedback.setStatusMessage(message);
                        
                        throw new ArchiveException(message);
                    }

                    taskMonitor.completed();
                    listener.taskCompleted(task);
                }
                else
                {
                    taskMonitor.started(task);
                    taskMonitor.aborted();
                    listener.taskAborted(task);
                }
            }
            catch (ArchiveException e)
            {
                taskMonitor.failed();
                listener.taskFailed(task);
                if (task.haltOnFailure())
                {
                    abort = true;
                }
                LOG.severe(e);
            }
        }

        taskMonitor.finish();

        if(taskMonitor.isSuccessful())
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

    private class DelegateTaskListener implements TaskListener
    {
        private TaskListener delegate = null;

        public DelegateTaskListener()
        {
        }

        public DelegateTaskListener(TaskListener delegate)
        {
            this.delegate = delegate;
        }

        public void taskCompleted(RestoreTask task)
        {
            if (delegate != null)
            {
                delegate.taskCompleted(task);
            }
        }

        public void taskAborted(RestoreTask task)
        {
            if (delegate != null)
            {
                delegate.taskAborted(task);
            }
        }

        public void taskFailed(RestoreTask task)
        {
            if (delegate != null)
            {
                delegate.taskFailed(task);
            }
        }
    }

}
