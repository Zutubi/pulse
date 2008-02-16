package com.zutubi.pulse.restore;

import com.zutubi.pulse.bootstrap.UserPaths;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class DefaultArchiveManager implements ArchiveManager
{
    private ProgressMonitor monitor = new ProgressMonitor();

    private Archive archive;

    private File tmpDirectory;

    //TODO: bypass the UserPaths instance, set the directory directly to ease testing and minimise the dependencies.
    private UserPaths paths = null;

    private List<ArchiveableComponent> archiveableComponents = new LinkedList<ArchiveableComponent>();

    private LinkedList<RestoreTaskGroup> groups;

    public void add(ArchiveableComponent component)
    {
        archiveableComponents.add(component);
    }

    public void setRestorableComponents(List<ArchiveableComponent> components)
    {
        archiveableComponents = new LinkedList<ArchiveableComponent>(components);
    }

    public ProgressMonitor getMonitor()
    {
        return monitor;
    }

    public Archive prepareRestore(File source) throws ArchiveException
    {
        ArchiveFactory factory = new ArchiveFactory();
        factory.setTmpDirectory(tmpDirectory);

        archive = factory.importArchive(source);

        // is this a complete restore? (all archiveable components are cleared first) or a partial
        // restore where only those components represented within the archive are cleared.?

        groups = new LinkedList<RestoreTaskGroup>();

        for (ArchiveableComponent component : archiveableComponents)
        {
            // task group takes details from the upgradeable component.
            RestoreTaskGroup taskGroup = new RestoreTaskGroup();
            taskGroup.setSource(component);

            String name = component.getName();
            File archiveComponentBase = new File(archive.getFile(), name);

            // does it matter if this does not exist, do we need to process something regardless?

            taskGroup.setTasks(component.getRestoreTasks(archiveComponentBase));

            groups.add(taskGroup);
        }

        return archive;
    }

    public List<RestoreTaskGroup> previewRestore()
    {
        // Check which of the restorable components is represented within the backup.

        // return a list of tasks that need to be processed.

        return new LinkedList<RestoreTaskGroup>();
    }

    public void restoreArchive()
    {
        if (monitor.isStarted())
        {
            throw new IllegalStateException("Can not start restoration of archive. Restoration in progress.");
        }

        try
        {
            monitor.start();

            // -- we should know which restorable components we are dealing with at this stage, so should
            //    not need to run the componentBase.isDirectory check.

            for (RestoreTaskGroup group : groups)
            {
                // monitor start group.
                for (RestoreTask task : group.getTasks())
                {
                    // monitor start task
                    task.execute();
                    // monitor end task
                }
                // monitor end group.
            }

            monitor.finish();
        }
        catch (ArchiveException e)
        {
            monitor.fail();
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
            File archiveComponentBase = new File(archive.getFile(), name);
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
