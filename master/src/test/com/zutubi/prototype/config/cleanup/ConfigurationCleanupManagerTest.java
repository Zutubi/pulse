package com.zutubi.prototype.config.cleanup;

import com.zutubi.config.annotations.Reference;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.config.AbstractConfigurationSystemTestCase;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 */
public class ConfigurationCleanupManagerTest extends AbstractConfigurationSystemTestCase
{
    private String nc1Path;
    private String c1Path;
    private String refererPath;
    private String refereePath;
    private String listRefererPath;
    private String listRefereePath;

    protected void setUp() throws Exception
    {
        super.setUp();

        registerMap(NoCustomTasksConfiguration.class, "noCustom");
        registerMap(CustomTasksConfiguration.class, "custom");
        registerMap(Referer.class, "referer");
        registerMap(ListReferer.class, "list");
        registerMap(Referee.class, "referee");

        nc1Path = configurationTemplateManager.insert("noCustom", new NoCustomTasksConfiguration("nc1"));
        c1Path = configurationTemplateManager.insert("custom", new CustomTasksConfiguration("c1"));
        refereePath = configurationTemplateManager.insert("referee", new Referee("ee"));
        listRefereePath = configurationTemplateManager.insert("referee", new Referee("listee"));
        refererPath = configurationTemplateManager.insert("referer", new Referer("er1", configurationTemplateManager.getInstance(refereePath, Referee.class)));
        listRefererPath = configurationTemplateManager.insert("list", new ListReferer("lister", configurationTemplateManager.getInstance(listRefereePath, Referee.class)));
    }

    private void registerMap(Class clazz, String scope) throws TypeException
    {
        CompositeType noCustomTasksType = typeRegistry.register(clazz);
        MapType noCustomTasksMap = new MapType();
        noCustomTasksMap.setCollectionType(noCustomTasksType);
        noCustomTasksMap.setTypeRegistry(typeRegistry);
        configurationPersistenceManager.register(scope, noCustomTasksMap);
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

    public void testReferenceCleanupTask()
    {
        RecordCleanupTask task = configurationTemplateManager.getCleanupTasks(refereePath);
        assertEquals(1, task.getCascaded().size());
        RecordCleanupTask cascaded = task.getCascaded().get(0);
        assertTrue(cascaded instanceof NullifyReferenceCleanupTask);
        assertEquals(PathUtils.getPath(refererPath, "ee"), cascaded.getAffectedPath());
    }

    public void testReferenceCleanupTaskList()
    {
        RecordCleanupTask task = configurationTemplateManager.getCleanupTasks(listRefereePath);
        assertEquals(1, task.getCascaded().size());
        RecordCleanupTask cascaded = task.getCascaded().get(0);
        assertTrue(cascaded instanceof RemoveReferenceCleanupTask);
        assertEquals(PathUtils.getPath(listRefererPath, "ees", "0"), cascaded.getAffectedPath());
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

    @SymbolicName("referer")
    public static class Referer extends AbstractNamedConfiguration
    {
        @Reference(cleanupTaskProvider = "CustomReferenceCleanupTaskProvider")
        private Referee ee;

        public Referer()
        {
        }

        public Referer(String name, Referee ee)
        {
            super(name);
            this.ee = ee;
        }

        public Referee getEe()
        {
            return ee;
        }

        public void setEe(Referee ee)
        {
            this.ee = ee;
        }
    }

    @SymbolicName("listReferer")
    public static class ListReferer extends AbstractNamedConfiguration
    {
        @Reference
        private List<Referee> ees = new LinkedList<Referee>();

        public ListReferer()
        {
        }

        public ListReferer(String name, Referee... ees)
        {
            super(name);
            this.ees.addAll(Arrays.asList(ees));
        }

        public List<Referee> getEes()
        {
            return ees;
        }

        public void setEes(List<Referee> ees)
        {
            this.ees = ees;
        }
    }

    @SymbolicName("referee")
    public static class Referee extends AbstractNamedConfiguration
    {
        public Referee()
        {
        }

        public Referee(String name)
        {
            super(name);
        }
    }

    public static class CustomReferenceCleanupTaskProvider implements ReferenceCleanupTaskProvider
    {
        public RecordCleanupTask getTask(String deletedPath, String referencingPath)
        {
            return new NullifyReferenceCleanupTask(referencingPath, null);
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
