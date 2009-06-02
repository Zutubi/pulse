package com.zutubi.pulse.master.restore;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.scheduling.CronTrigger;
import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.scheduling.SchedulingException;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.servercore.events.system.SystemStartedListener;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.TypeAdapter;
import com.zutubi.tove.config.TypeListener;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * The backup manager is responsible for coordinating the automated backups for the system.
 */
public class BackupManager
{
    private static final Logger LOG = Logger.getLogger(BackupManager.class);

    /**
     * The name of the scheduled cron trigger used to run regular backups 
     */
    public static final String TRIGGER_NAME = "automated.backup";

    public static final String TRIGGER_GROUP = "admin";

    private static final String DELETE_FILE_SUFFIX = ".delete";

    // default backup details in case no configuration is stored.
    static final BackupConfiguration DEFAULT = new BackupConfiguration();

    private ConfigurationProvider configurationProvider;

    private Scheduler scheduler;

    private EventManager eventManager;
    
    /**
     * The directory into which generated backups are copied for storage.
     */
    private File backupDir;

    /**
     * The list of archiveable components that are currently used for generating backups.
     */
    private List<ArchiveableComponent> backupableComponents = new LinkedList<ArchiveableComponent>();

    /**
     * The directory used for temporary file storage
     */
    private File tmp;

    public void init()
    {
        // all initialisation occurs on the system started event as we need
        // to be sure all of the components are available.
        eventManager.register(new SystemStartedListener()
        {
            public void systemStarted()
            {
                SpringComponentContext.autowire(BackupManager.this);
                initialiseManager();
            }
        });
    }

    protected void initialiseManager()
    {
        // initialise the automated backups with the scheduler
        BackupConfiguration instance = configurationProvider.get(BackupConfiguration.class);
        try
        {
            if (instance.isEnabled())
            {
                // check if the trigger exists. if not, create and schedule.
                Trigger trigger = scheduler.getTrigger(TRIGGER_NAME, TRIGGER_GROUP);
                if (trigger == null)
                {
                    scheduleTrigger(instance);
                }
                else
                {
                    // if any changes have been made to the record, then sync the trigger with that data.
                    updateTriggerIfNecessary(trigger, instance);
                }
            }
        }
        catch (SchedulingException e)
        {
            LOG.warning(e);
        }

        // register the configuration listener.
        TypeListener<BackupConfiguration> listener = new TypeAdapter<BackupConfiguration>(BackupConfiguration.class)
        {
            public void postSave(BackupConfiguration instance, boolean nested)
            {
                try
                {
                    CronTrigger trigger = (CronTrigger) scheduler.getTrigger(TRIGGER_NAME, TRIGGER_GROUP);

                    // is enabled?
                    if (instance.isEnabled())
                    {
                        if (trigger == null)
                        {
                            scheduleTrigger(instance);
                        }
                        else
                        {
                            updateTriggerIfNecessary(trigger, instance);
                        }
                    }
                    else
                    {
                        if (trigger != null)
                        {
                            // unschedule the trigger.
                            scheduler.unschedule(trigger);
                        }
                    }
                }
                catch (SchedulingException e)
                {
                    LOG.warning(e);
                }
            }
        };
        listener.register(configurationProvider, false);
    }

    private void updateTriggerIfNecessary(Trigger trigger, BackupConfiguration instance) throws SchedulingException
    {
        CronTrigger cronTrigger = (CronTrigger) trigger;
        String existingCronString = cronTrigger.getCron();
        
        if (!existingCronString.equals(instance.getCronSchedule()))
        {
            cronTrigger.setCron(instance.getCronSchedule());

            scheduler.update(trigger);
        }
    }

    private void scheduleTrigger(BackupConfiguration instance) throws SchedulingException
    {
        Trigger trigger = new CronTrigger(instance.getCronSchedule(), TRIGGER_NAME, TRIGGER_GROUP);
        trigger.setTaskClass(AutomatedBackupTask.class);

        scheduler.schedule(trigger);
    }

    public void triggerBackup()
    {
        // prior to creating backups, we need to ensure that we cleanup the existing backups.  Cleanups
        // are to ensure we have enough disk space, so it is better to cleanup before creating a new backup
        // and risk deleting early (if the backup fails) that to fail a backup before we run out of space.
        cleanupBackups();

        try
        {
            ArchiveFactory factory = new ArchiveFactory();
            factory.setTmpDirectory(tmp);

            Archive archive = factory.createArchive();

            // now we fill the archive.
            for (ArchiveableComponent component : backupableComponents)
            {
                String name = component.getName();
                File archiveComponentBase = new File(archive.getBase(), name);
                if (!archiveComponentBase.mkdirs())
                {
                    throw new ArchiveException("Failed to create new directory: " + archiveComponentBase.getAbsolutePath());
                }
                component.backup(archiveComponentBase);
            }

            factory.exportArchive(archive, backupDir);

            // cleanup the archive now that it has been exported to the persistent backup directory.
            FileSystemUtils.rmdir(archive.getBase());
        }
        catch (ArchiveException e)
        {
            LOG.warning(e);
        }
    }

    private void cleanupBackups()
    {
        // a) mark them for deletion
        // b) delete them.
        final ArchiveNameGenerator generator = new UniqueDatestampedNameGenerator();

        File[] candidateFilesForCleanup = backupDir.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return !name.endsWith(DELETE_FILE_SUFFIX) && generator.matches(name);
            }
        });

        // the cleanup runs BEFORE the backup is generated.  So, if we want no more than 10 backups, we
        // need the cleanup to ensure that only x - 1 exist.
        BackupCleanupStrategy strategy = new KeepMostRecentXCleanupStrategy(9);

        File[] cleanupTargets = strategy.getCleanupTargets(candidateFilesForCleanup);
        if (cleanupTargets.length > 0)
        {
            for (File cleanupTarget : cleanupTargets)
            {
                if (cleanupTarget.exists())
                {
                    File renamedCleanupTarget = new File(cleanupTarget.getParentFile(), cleanupTarget.getName() + DELETE_FILE_SUFFIX);
                    try
                    {
                        FileSystemUtils.robustRename(cleanupTarget, renamedCleanupTarget);
                    }
                    catch (IOException e)
                    {
                        LOG.warning("Cleaning up backups: " + e.getMessage(), e);
                    }
                }
            }
        }

        // cleanup any files that are marked for deletion that failed to be deleted previously.
        File[] filesMarkedForDeletion = backupDir.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(DELETE_FILE_SUFFIX);
            }
        });

        if (filesMarkedForDeletion != null)
        {
            for (File file : filesMarkedForDeletion)
            {
                try
                {
                    FileSystemUtils.delete(file);
                }
                catch (IOException e)
                {
                    LOG.warning(e);
                }
            }
        }
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setBackupDir(File backupDir)
    {
        this.backupDir = backupDir;
    }

    public void setTmpDirectory(File tmpDirectory)
    {
        this.tmp = tmpDirectory;
    }

    public void add(ArchiveableComponent component)
    {
        this.backupableComponents.add(component);
    }

    public void setBackupableComponents(List<ArchiveableComponent> backupableComponents)
    {
        this.backupableComponents = backupableComponents;
    }
}
