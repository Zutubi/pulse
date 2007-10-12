package com.zutubi.prototype.config.cleanup;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.config.AbstractConfigurationSystemTestCase;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 */
public class ConfigurationCleanupManagerTest extends AbstractConfigurationSystemTestCase
{
    private String nc1Path;
    private String c1Path;

    protected void setUp() throws Exception
    {
        super.setUp();

        CompositeType noCustomTasksType = typeRegistry.register(NoCustomTasksConfiguration.class);
        MapType noCustomTasksMap = new MapType();
        noCustomTasksMap.setCollectionType(noCustomTasksType);
        noCustomTasksMap.setTypeRegistry(typeRegistry);
        configurationPersistenceManager.register("noCustom", noCustomTasksMap);

        CompositeType customTasksType = typeRegistry.register(CustomTasksConfiguration.class);
        MapType customTasksMap = new MapType();
        customTasksMap.setCollectionType(customTasksType);
        customTasksMap.setTypeRegistry(typeRegistry);
        configurationPersistenceManager.register("custom", customTasksMap);
        nc1Path = configurationTemplateManager.insert("noCustom", new NoCustomTasksConfiguration("nc1"));
        c1Path = configurationTemplateManager.insert("custom", new CustomTasksConfiguration("c1"));
    }

    public void testNoCustomTasks()
    {
        DeleteRecordCleanupTask cleanupTask = (DeleteRecordCleanupTask) configurationTemplateManager.getCleanupTasks(nc1Path);
        assertEquals(nc1Path, cleanupTask.getAffectedPath());
        assertEquals(0, cleanupTask.getCascaded().size());
    }

    public void testCustomTask()
    {
        DeleteRecordCleanupTask cleanupTask = (DeleteRecordCleanupTask) configurationTemplateManager.getCleanupTasks(c1Path);
        assertEquals(c1Path, cleanupTask.getAffectedPath());
        assertEquals(1, cleanupTask.getCascaded().size());
        assertTrue(cleanupTask.getCascaded().get(0) instanceof CustomCleanupTask);
    }

    public void testCascadedTasksExecuted()
    {
        CustomCleanupTask root = new CustomCleanupTask("root");
        CustomCleanupTask cascaded = new CustomCleanupTask("root/cascaded");
        CustomCleanupTask casCascaded = new CustomCleanupTask("root/cascaded/cascaded");
        root.addCascaded(cascaded);
        cascaded.addCascaded(casCascaded);

        assertFalse(root.isComplete());
        assertFalse(cascaded.isComplete());
        assertFalse(casCascaded.isComplete());

        configurationCleanupManager.runCleanupTasks(root);
        assertTrue(root.isComplete());
        assertTrue(cascaded.isComplete());
        assertTrue(casCascaded.isComplete());
    }

    public void testAsynchronousExecution() throws InterruptedException
    {
        Semaphore flag = new Semaphore(0);
        CustomCleanupTask root = new CustomCleanupTask("root");
        AsyncCleanupTask async = new AsyncCleanupTask("async", flag);
        root.addCascaded(async);

        configurationCleanupManager.runCleanupTasks(root);

        assertTrue(root.isComplete());
        assertSame(root.executingThread, Thread.currentThread());

        flag.tryAcquire(10, TimeUnit.SECONDS);
        assertNotNull(async.executingThread);
        assertNotSame(async.executingThread, Thread.currentThread());
    }

    @SymbolicName("noCustomTasks")
    public static class NoCustomTasksConfiguration extends AbstractNamedConfiguration
    {
        public NoCustomTasksConfiguration()
        {
        }

        public NoCustomTasksConfiguration(String name)
        {
            super(name);
        }
    }

    @SymbolicName("customTasks")
    public static class CustomTasksConfiguration extends AbstractNamedConfiguration
    {
        public CustomTasksConfiguration()
        {
        }

        public CustomTasksConfiguration(String name)
        {
            super(name);
        }
    }

    public static class CustomTasksConfigurationCleanupTasks
    {
        public List<RecordCleanupTask> getTasks(CustomTasksConfiguration instance)
        {
            return Arrays.<RecordCleanupTask>asList(new CustomCleanupTask(instance.getConfigurationPath()));
        }
    }

    public static class CustomCleanupTask extends RecordCleanupTaskSupport
    {
        private Thread executingThread = null;

        public CustomCleanupTask(String path)
        {
            super(path);
        }

        public void run()
        {
            executingThread = Thread.currentThread();
        }

        public boolean isComplete()
        {
            return executingThread != null;
        }
    }

    public static class AsyncCleanupTask extends RecordCleanupTaskSupport
    {
        private Thread executingThread = null;
        private Semaphore flag;

        public AsyncCleanupTask(String path, Semaphore flag)
        {
            super(path);
            this.flag = flag;
        }

        public boolean isAsynchronous()
        {
            return true;
        }

        public void run()
        {
            executingThread = Thread.currentThread();
            flag.release();
        }
    }
}
