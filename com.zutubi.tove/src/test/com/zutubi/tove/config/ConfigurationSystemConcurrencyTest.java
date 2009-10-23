package com.zutubi.tove.config;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.RandomUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 */
public class ConfigurationSystemConcurrencyTest extends AbstractConfigurationSystemTestCase
{
    private static final String NORMAL_SCOPE = "normal";

    protected void setUp() throws Exception
    {
        super.setUp();

        CompositeType typeA = typeRegistry.register(MockA.class);
        MapType mapA = new MapType(typeA, typeRegistry);
        configurationPersistenceManager.register(NORMAL_SCOPE, mapA);
    }

    public void testConcurrentInsertsAndDeletes() throws InterruptedException
    {
        runFor(500, new Inserter(), new Deleter());
    }

    public void testConcurrentInsertsAndGets() throws InterruptedException
    {
        runFor(500, new Inserter(), new Getter());
    }

    public void testConcurrentInsertsDeletesAndGets() throws InterruptedException
    {
        runFor(500, new Inserter(), new Deleter(), new Getter());
    }

    private void runFor(long millis, Worker... workers) throws InterruptedException
    {
        VerifyingListener listener = new VerifyingListener();
        listener.register(configurationProvider, true);
        WorkerGroup group = new WorkerGroup(workers);
        group.start();
        Thread.sleep(millis);
        group.stop();
        group.join();
    }

    public static class VerifyingListener extends TypeListener<MockA>
    {
        private Set<Long> existing = new HashSet<Long>();
        private Set<Long> postExisting = new HashSet<Long>();
        private String error;

        public VerifyingListener()
        {
            super(MockA.class);
        }

        public void insert(MockA instance)
        {
            if(!existing.add(instance.getHandle()))
            {
                setError("Duplicate insert handle");
            }
        }

        public void postInsert(MockA instance)
        {
            if(!existing.contains(instance.getHandle()))
            {
                setError("Post insert before insert");
            }

            if(!postExisting.add(instance.getHandle()))
            {
                setError("Duplicate post insert handle");
            }
        }

        public void delete(MockA instance)
        {
            if(!existing.remove(instance.getHandle()))
            {
                setError("Delete before insert");
            }
        }

        public void postDelete(MockA instance)
        {
            if(postExisting.contains(instance.getHandle()))
            {
                setError("Post delete before delete");
            }
            
            if(!postExisting.remove(instance.getHandle()))
            {
                setError("Post delete before insert");
            }
        }

        public void save(MockA instance, boolean nested)
        {
            if(!existing.contains(instance.getHandle()))
            {
                setError("Save non-existant");
            }
        }

        public void postSave(MockA instance, boolean nested)
        {
            if(!postExisting.contains(instance.getHandle()))
            {
                setError("Post-save non-existant");
            }
        }

        public String getError()
        {
            return error;
        }

        private void setError(String error)
        {
            if(this.error == null)
            {
                this.error = error;
            }
        }
    }

    @SymbolicName("MockA")
    public static class MockA extends AbstractNamedConfiguration
    {
        public MockA()
        {
        }

        public MockA(String name)
        {
            super(name);
        }
    }

    private static class WorkerGroup
    {
        private AtomicBoolean running = new AtomicBoolean(false);
        private WorkerThread[] workerThreads;

        public WorkerGroup(Worker... workers)
        {
            workerThreads = CollectionUtils.mapToArray(workers, new Mapping<Worker, WorkerThread>()
            {
                public WorkerThread map(Worker worker)
                {
                    return new WorkerThread(new WorkerRunner(worker, running));
                }
            }, new WorkerThread[workers.length]);
        }

        public void start()
        {
            running.set(true);
            for (WorkerThread thread : workerThreads)
            {
                thread.start();
            }
        }

        public void stop()
        {
            running.set(false);
        }

        public void join() throws InterruptedException
        {
            for (WorkerThread workerThread : workerThreads)
            {
                workerThread.join();
                WorkerRunner runner = workerThread.getRunner();
                Exception error = runner.getError();
                if (error != null)
                {
                    error.printStackTrace();
                    fail(runner.getWorker().getClass().getSimpleName() + " got error: " + error.getMessage());
                }
            }
        }
    }

    private static class WorkerThread extends Thread
    {
        private WorkerRunner runner;

        public WorkerThread(WorkerRunner runner)
        {
            super(runner);
            this.runner = runner;
        }

        public WorkerRunner getRunner()
        {
            return runner;
        }
    }

    private static class WorkerRunner implements Runnable
    {
        private Worker worker;
        private AtomicBoolean running;
        protected Exception error;

        public WorkerRunner(Worker worker, AtomicBoolean running)
        {
            this.worker = worker;
            this.running = running;
        }

        public void run()
        {
            try
            {
                while (running.get())
                {
                    worker.doWorkUnit();
                }
            }
            catch (Exception e)
            {
                error = e;
            }
        }

        public Worker getWorker()
        {
            return worker;
        }

        public Exception getError()
        {
            return error;
        }
    }

    private static interface Worker
    {
        public void doWorkUnit() throws Exception;
    }

    private class Getter implements Worker
    {
        public void doWorkUnit()
        {
            Collection<MockA> instances = configurationTemplateManager.getAllInstances(MockA.class);
            for (MockA a : instances)
            {
                assertNotNull(a);
            }
        }
    }

    private class Inserter implements Worker
    {
        public void doWorkUnit()
        {
            MockA a = new MockA(RandomUtils.randomString(20));
            configurationTemplateManager.insert(NORMAL_SCOPE, a);
        }
    }

    private class Deleter implements Worker
    {
        private final String DELETE_PATH = PathUtils.getPath(NORMAL_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT);

        public void doWorkUnit() throws Exception
        {
            Thread.sleep(30);
            configurationTemplateManager.deleteAll(DELETE_PATH);
        }
    }
}
