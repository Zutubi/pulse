package com.zutubi.pulse.restore;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class DefaultRestoreManager implements RestoreManager
{

    private RestoreProgressMonitor restoreMonitor = new RestoreProgressMonitor();

    private BackupInfo backupInfo;

    private List<Restorable> restorableComponents = new LinkedList<Restorable>();

    public void add(Restorable component)
    {
        restorableComponents.add(component);
    }

    public void setRestorableComponents(List<Restorable> components)
    {
        restorableComponents = new LinkedList<Restorable>(components);
    }

    public RestoreProgressMonitor getRestoreMonitor()
    {
        return restoreMonitor;
    }

    public BackupInfo prepareRestore(File backup)
    {
        // check the backup file, load the backup info.
        backupInfo = new BackupInfo();
        backupInfo.setSource(backup);

        return backupInfo;
    }

    public BackupInfo previewRestore()
    {
        // Check which of the restorable components is represented within the backup.

        return backupInfo;
    }

    public void executeRestore()
    {
        try
        {
            restoreMonitor.start();

            // -- we should know which restorable components we are dealing with at this stage, so should
            //    not need to run the componentBase.isDirectory check.

            File base = backupInfo.getSource();
            for (Restorable component : restorableComponents)
            {
                File componentBase = new File(base, component.getName());
                if (componentBase.isDirectory())
                {
                    // starting component.getName();
                    component.restore(componentBase);
                    // finishing component.getName();
                }
            }

            restoreMonitor.finish();
        }
        catch (RestoreException e)
        {
            restoreMonitor.fail();
        }
    }
}
