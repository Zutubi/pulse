package com.zutubi.pulse.restore;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.scheduling.CronTrigger;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.util.logging.Logger;

/**
 *
 *
 */
public class BackupManager
{
    private static final Logger LOG = Logger.getLogger(BackupManager.class);

    public static final String TRIGGER_NAME = "automated.backup";

    public static final String TRIGGER_GROUP = "admin";

    // default backup details in case no configuration is stored.
    public BackupConfiguration DEFAULT = new BackupConfiguration();

    private ConfigurationProvider configurationProvider;

    private ArchiveManager archiveManager;

    private Scheduler scheduler;

    public void init()
    {
        // initialise the automated backups with the scheduler

        // load the persistence configuration.
        BackupConfiguration persistent = configurationProvider.get(BackupConfiguration.class);

        if (persistent.isEnabled())
        {
            // check if the trigger exists. if not, create and schedule.
            Trigger trigger = scheduler.getTrigger(TRIGGER_NAME, TRIGGER_GROUP);
            if (trigger == null)
            {
                // initialise the trigger.
                trigger = new CronTrigger(persistent.getCronSchedule(), TRIGGER_NAME, TRIGGER_GROUP);
                trigger.setTaskClass(AutomatedBackupTask.class);

                try
                {
                    scheduler.schedule(trigger);
                }
                catch (SchedulingException e)
                {
                    LOG.severe(e);
                }
            }
        }

        // register the configuration listener.
/*
        TypeListener<BackupConfiguration> listener = new TypeAdapter<BackupConfiguration>(BackupConfiguration.class)
        {
            public void postSave(BackupConfiguration instance, boolean nested)
            {
                try
                {
                    CronTrigger trigger = (CronTrigger) scheduler.getTrigger(TRIGGER_NAME, TRIGGER_GROUP);
                    trigger.setCron(instance.getCronSchedule());

                    scheduler.update(trigger);
                }
                catch (SchedulingException e)
                {
                    LOG.warning(e);
                }
            }
        };
        listener.register(configurationProvider, false);
*/
    }

    public void triggerBackup()
    {
        try
        {
            Archive archive = archiveManager.createArchive();

            // do we want to do anything more with it than what the archive manager has already done?
            // Should we be moving some of the logic here?... where is that boundry...
            // - how do we ensure the consistency of the archive - prevent changes to the configuration while
            //   we are generating the archive.  NOTE: this should not take very long - or at least the snapshotting
            //   process of the configuration should not...

        }
        catch (ArchiveException e)
        {
            LOG.warning(e);
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

    public void setArchiveManager(ArchiveManager archiveManager)
    {
        this.archiveManager = archiveManager;
    }
}
