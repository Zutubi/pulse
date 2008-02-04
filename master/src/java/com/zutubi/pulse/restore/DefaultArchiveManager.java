package com.zutubi.pulse.restore;

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

    private List<Archiveable> archiveableComponents = new LinkedList<Archiveable>();

    public void add(Archiveable component)
    {
        archiveableComponents.add(component);
    }

    public void setRestorableComponents(List<Archiveable> components)
    {
        archiveableComponents = new LinkedList<Archiveable>(components);
    }

    public ProgressMonitor getMonitor()
    {
        return monitor;
    }

    public Archive prepareRestore(File source)
    {
        // check the backup file, load the backup info.
        archive = new Archive(source);

        return archive;
    }

    public Archive previewRestore()
    {
        // Check which of the restorable components is represented within the backup.

        return archive;
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

            for (Archiveable component : archiveableComponents)
            {
                // starting component.getName();
                component.restore(archive);
                // finishing component.getName();
            }

            monitor.finish();
        }
        catch (ArchiveException e)
        {
            monitor.fail();
        }
    }


    public Archive createArchive()
    {
        return null;
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
}
