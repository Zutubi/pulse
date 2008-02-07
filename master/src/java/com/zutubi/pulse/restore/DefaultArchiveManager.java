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

    private File tmpDirectory;

    private List<ArchiveableComponent> archiveableComponents = new LinkedList<ArchiveableComponent>();

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

        archive = factory.openArchive(source);
        
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

            for (ArchiveableComponent component : archiveableComponents)
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

    public void setTmpDirectory(File tmpDirectory)
    {
        this.tmpDirectory = tmpDirectory;
    }
}
