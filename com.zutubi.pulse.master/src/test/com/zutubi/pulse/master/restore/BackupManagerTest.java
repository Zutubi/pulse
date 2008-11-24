package com.zutubi.pulse.master.restore;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.master.bootstrap.Data;
import com.zutubi.pulse.master.model.persistence.mock.MockTriggerDao;
import com.zutubi.pulse.master.scheduling.CronTrigger;
import com.zutubi.pulse.master.scheduling.DefaultScheduler;
import com.zutubi.pulse.master.scheduling.MockSchedulerStrategy;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.master.tove.config.project.triggers.CronExpressionValidator;
import com.zutubi.tove.config.MockConfigurationProvider;
import com.zutubi.tove.config.events.PostSaveEvent;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.validation.MockValidationContext;
import com.zutubi.validation.ValidationException;

import java.io.File;
import java.io.IOException;

public class BackupManagerTest extends PulseTestCase
{
    private DefaultScheduler scheduler;

    private DefaultRestoreManager restoreManager;

    private MockConfigurationProvider configurationProvider;

    private EventManager eventManager;

    private File tmp;
    private File backupDir;
    private File backupTmpDir;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();
        backupDir = new File(tmp, "backup");
        backupTmpDir = new File(tmp, "tmp");
        assertTrue(backupTmpDir.mkdirs());

        scheduler = new DefaultScheduler();
        scheduler.setTriggerDao(new MockTriggerDao());
        scheduler.setStrategies(new MockSchedulerStrategy());
        scheduler.start();

        configurationProvider = new MockConfigurationProvider();
        configurationProvider.insert("admin/settings/backup", new BackupConfiguration());

        restoreManager = new DefaultRestoreManager();
        restoreManager.setPaths(new Data(tmp));

        eventManager = new DefaultEventManager();
    }

    protected void tearDown() throws Exception
    {
        restoreManager = null;
        configurationProvider = null;
        scheduler.stop(true);
        scheduler = null;
        
        removeDirectory(tmp);
        tmp = null;
        backupDir = null;
        backupTmpDir = null;

        super.tearDown();
    }

    public void testBackupManagerInit()
    {
        BackupConfiguration config = configurationProvider.get(BackupConfiguration.class);
        config.setEnabled(true);

        createAndStartBackupManager();

        // Ensure that the init of the backup manager registers the expected trigger.
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(BackupManager.TRIGGER_NAME, BackupManager.TRIGGER_GROUP);
        assertNotNull(trigger);
        assertEquals(BackupManager.DEFAULT.getCronSchedule(), trigger.getCron());
        assertTrue(trigger.isScheduled());
    }

    public void testStateOfScheduledTrigger()
    {
        BackupConfiguration config = configurationProvider.get(BackupConfiguration.class);
        config.setEnabled(true);

        createAndStartBackupManager();

        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(BackupManager.TRIGGER_NAME, BackupManager.TRIGGER_GROUP);
    }

    public void testDefaultCronScheduleIsValid() throws ValidationException
    {
        MockValidationContext validationContext = new MockValidationContext();
        CronExpressionValidator v = new CronExpressionValidator();
        v.setValidationContext(validationContext);
        v.validateStringField(BackupManager.DEFAULT.getCronSchedule());

        assertFalse(validationContext.hasErrors());
    }

    public void testBackupCreation()
    {
        BackupManager manager = createAndStartBackupManager();
        
        assertFalse(backupDir.isDirectory());

        manager.triggerBackup();

        // ensure a backup was created.
        assertEquals(1, backupDir.list().length);
    }

    // this is more of an acceptance test with an annoying dependency on timing. The individual components
    // that make up this functionality are tested.
    public void testCleanupOfBackupDirectory() throws IOException, InterruptedException
    {
        BackupManager manager = createAndStartBackupManager();
        manager.triggerBackup();

        File firstBackup = backupDir.listFiles()[0];
        assertTrue(firstBackup.isFile());
        // We need to ensure this first file is the oldest.
        Thread.sleep(1000);

        for (int i = 0; i < 10; i++)
        {
            manager.triggerBackup();
        }

        assertFalse(firstBackup.isFile());
    }

    public void testScheduleOnlyOnEnabled()
    {
        BackupConfiguration config = configurationProvider.get(BackupConfiguration.class);
        config.setEnabled(false);

        createAndStartBackupManager();

        // Ensure that the init of the backup manager registers the expected trigger.
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(BackupManager.TRIGGER_NAME, BackupManager.TRIGGER_GROUP);
        assertNull(trigger);
    }

    public void testConfigurationChange()
    {
        BackupConfiguration config = configurationProvider.get(BackupConfiguration.class);
        config.setEnabled(true);

        // when the backup configuration is changed, we need to ensure that the trigger remains in sync.
        createAndStartBackupManager();

        // make a change to the configuration, triggering a save.
        config.setCronSchedule("0 0 0 * * ?");
        saveConfigurationChange(config);

        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(BackupManager.TRIGGER_NAME, BackupManager.TRIGGER_GROUP);
        assertNotNull(trigger);
        assertEquals("0 0 0 * * ?", trigger.getCron());
    }

    public void testEnableDisable()
    {
        BackupConfiguration config = configurationProvider.get(BackupConfiguration.class);
        config.setEnabled(false);

        // when the backup configuration is changed, we need to ensure that the trigger remains in sync.
        createAndStartBackupManager();

        Trigger trigger = scheduler.getTrigger(BackupManager.TRIGGER_NAME, BackupManager.TRIGGER_GROUP);
        assertNull(trigger);

        config.setEnabled(true);

        saveConfigurationChange(config);

        trigger = scheduler.getTrigger(BackupManager.TRIGGER_NAME, BackupManager.TRIGGER_GROUP);
        assertNotNull(trigger);

        config.setEnabled(false);
        saveConfigurationChange(config);

        trigger = scheduler.getTrigger(BackupManager.TRIGGER_NAME, BackupManager.TRIGGER_GROUP);
        assertNull(trigger);
    }

    public void testEnsureThatBackupProcessCleansUpTmpDirectory()
    {
        BackupManager manager = createAndStartBackupManager();

        assertEquals(0, backupTmpDir.listFiles().length);

        manager.triggerBackup();

        assertEquals(0, backupTmpDir.listFiles().length);
    }

    private void saveConfigurationChange(BackupConfiguration config)
    {
        PostSaveEvent evt = new PostSaveEvent(null, config);
        configurationProvider.sendEvent(evt);
    }

    private BackupManager createAndStartBackupManager()
    {
        BackupManager manager = new BackupManager();
        manager.setScheduler(scheduler);
        manager.setEventManager(eventManager);
        manager.setConfigurationProvider(configurationProvider);
        manager.setBackupDir(backupDir);
        manager.setTmpDirectory(backupTmpDir);
        manager.add(new NoopArchiveableComponent());
        manager.initialiseManager();

        return manager;
    }

}
