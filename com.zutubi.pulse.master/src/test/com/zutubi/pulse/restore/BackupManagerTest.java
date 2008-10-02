package com.zutubi.pulse.restore;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.core.util.FileSystemUtils;
import com.zutubi.pulse.master.bootstrap.Data;
import com.zutubi.pulse.model.persistence.mock.MockTriggerDao;
import com.zutubi.pulse.scheduling.CronTrigger;
import com.zutubi.pulse.scheduling.DefaultScheduler;
import com.zutubi.pulse.scheduling.MockSchedulerStrategy;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.servercore.events.system.SystemStartedEvent;
import com.zutubi.pulse.tove.config.project.triggers.CronExpressionValidator;
import com.zutubi.tove.config.MockConfigurationProvider;
import com.zutubi.tove.config.events.PostSaveEvent;
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

    private DefaultRestoreManager restoreManager;

    private MockConfigurationProvider configurationProvider;

    private EventManager eventManager;

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
        
        super.tearDown();
    }

    public void testBackupManagerInit()
    {
        BackupConfiguration config = configurationProvider.get(BackupConfiguration.class);
        config.setEnabled(true);

        BackupManager manager = createAndStartBackupManager();

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
        BackupManager manager = createAndStartBackupManager();
        
        manager.triggerBackup();

        // is this much more than a simple delegation to the restoreManager?.  Maybe we want to place the
        // archive in a special location / file name format?.
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
        manager.setBackupDir(new File(tmp, "backup"));
        manager.setTmpDirectory(new File(tmp, "tmp"));
        manager.add(new NoopArchiveableComponent());
        manager.init();

        // send the system started event.
        eventManager.publish(new SystemStartedEvent(this));
        
        return manager;
    }

}
