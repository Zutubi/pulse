package com.zutubi.pulse.restore;

import com.zutubi.pulse.bootstrap.UserPaths;
import com.zutubi.pulse.restore.feedback.Feedback;
import com.zutubi.pulse.restore.feedback.TaskMonitor;
import com.zutubi.pulse.restore.feedback.FeedbackProvider;
import com.zutubi.util.logging.Logger;

import java.io.File;
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

    private Archive archive;

    private File tmpDirectory;

    //TODO: bypass the UserPaths instance, set the directory directly to ease testing and minimise the dependencies.
    private UserPaths paths = null;

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
                ((FeedbackProvider)component).setFeedback(feedback);
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
            throw new IllegalStateException("Can not start restoration of archive. Restoration in progress.");
        }

        try
        {
            // -- we should know which restorable components we are dealing with at this stage, so should
            //    not need to run the componentBase.isDirectory check.

            for (RestoreTask task : tasks)
            {
                // monitor start task
                taskMonitor.started(task);
                task.execute();
                taskMonitor.completed();
            }

        }
        catch (ArchiveException e)
        {
            LOG.error(e);

            Feedback feedback = taskMonitor.getCurrentTaskProgress();
            feedback.setStatusMessage(e.getMessage());
            
            taskMonitor.errored();

            // abort the remaining.
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

    public void restoreArchive(Archive archive)
    {

    }

    public void cancelRestoreOnRestart()
    {

    }

    public void requestRestoreOnRestart(Archive archive)
    {

    }

    public boolean isRestoreOnRestartRequested()
    {
        return false;
    }

    public Archive getArchiveToBeRestoredOnRestart()
    {
        return null;
    }

    public void setTmpDirectory(File tmpDirectory)
    {
        this.tmpDirectory = tmpDirectory;
    }

    public void setPaths(UserPaths paths)
    {
        this.paths = paths;
    }
}
