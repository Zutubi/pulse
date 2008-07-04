package com.zutubi.pulse.restore;

import com.zutubi.prototype.config.MockConfigurationProvider;
import com.zutubi.pulse.bootstrap.Data;
import com.zutubi.pulse.model.persistence.mock.MockTriggerDao;
import com.zutubi.pulse.prototype.config.project.triggers.CronExpressionValidator;
import com.zutubi.pulse.scheduling.CronTrigger;
import com.zutubi.pulse.scheduling.DefaultScheduler;
import com.zutubi.pulse.scheduling.MockSchedulerStrategy;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.validation.MockValidationContext;
import com.zutubi.validation.ValidationException;

import java.io.File;

/**
 *
 *
 */
public class BackupManagerTest extends PulseTestCase
{
    private DefaultScheduler scheduler;

    private DefaultArchiveManager archiveManager;

    private MockConfigurationProvider configurationProvider;

    private File tmp;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();

        scheduler = new DefaultScheduler();
        scheduler.setTriggerDao(new MockTriggerDao());
        scheduler.setStrategies(new MockSchedulerStrategy());
        scheduler.start();

        configurationProvider = new MockConfigurationProvider();
        configurationProvider.insert("admin/settings/backup", new BackupConfiguration());

        archiveManager = new DefaultArchiveManager();
        archiveManager.setPaths(new Data(tmp));
    }

    protected void tearDown() throws Exception
    {
        archiveManager = null;
        configurationProvider = null;
        scheduler.stop(true);
        scheduler = null;
        
        removeDirectory(tmp);
        tmp = null;
        
        super.tearDown();
    }

    public void testBackupManagerInit()
    {
        BackupConfiguration config = configurationProvider.get(BackupConfiguration.class);
        config.setEnabled(true);

        BackupManager manager = new BackupManager();
        manager.setScheduler(scheduler);
        manager.setConfigurationProvider(configurationProvider);
        manager.init();

        // Ensure that the init of the backup manager registers the expected trigger.
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(BackupManager.TRIGGER_NAME, BackupManager.TRIGGER_GROUP);
        assertNotNull(trigger);
        assertEquals(manager.DEFAULT.getCronSchedule(), trigger.getCron());
        assertTrue(trigger.isScheduled());
    }

    public void testDefaultCronScheduleIsValid() throws ValidationException
    {
        BackupManager manager = new BackupManager();

        MockValidationContext validationContext = new MockValidationContext();
        CronExpressionValidator v = new CronExpressionValidator();
        v.setValidationContext(validationContext);
        v.validateStringField(manager.DEFAULT.getCronSchedule());

        assertFalse(validationContext.hasErrors());
    }

    public void testBackupCreation()
    {
        BackupManager manager = new BackupManager();
        manager.setArchiveManager(archiveManager);
        
        manager.triggerBackup();

        // is this much more than a simple delegation to the archiveManager?.  Maybe we want to place the
        // archive in a special location / file name format?.
    }

    public void testScheduleOnlyOnEnabled()
    {
        BackupManager manager = new BackupManager();
        manager.setScheduler(scheduler);
        manager.setConfigurationProvider(configurationProvider);
        manager.init();

        // Ensure that the init of the backup manager registers the expected trigger.
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(BackupManager.TRIGGER_NAME, BackupManager.TRIGGER_GROUP);
        assertNull(trigger);
    }

    public void testConfigurationChange()
    {
/*
        // when the backup configuration is changed, we need to ensure that the trigger remains in sync.
        BackupManager manager = new BackupManager();
        manager.setScheduler(scheduler);
        manager.setConfigurationProvider(configurationProvider);
        manager.init();

        // make a change to the configuration, triggering a save.
        BackupConfiguration config = new BackupConfiguration();
        config.setCronSchedule("0 0 0 * * ?");
        PostSaveEvent evt = new PostSaveEvent(null, config);
        configurationProvider.sendEvent(evt);

        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(BackupManager.TRIGGER_NAME, BackupManager.TRIGGER_GROUP);
        assertNotNull(trigger);
        assertEquals("0 0 0 * * ?", trigger.getCron());
*/
    }
}
