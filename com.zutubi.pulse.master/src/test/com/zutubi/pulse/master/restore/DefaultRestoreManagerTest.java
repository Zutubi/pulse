package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.bootstrap.Data;
import com.zutubi.pulse.master.util.monitor.JobManager;
import com.zutubi.pulse.master.util.monitor.Monitor;
import com.zutubi.pulse.master.util.monitor.Task;
import com.zutubi.pulse.master.util.monitor.TaskFeedback;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.util.List;

public class DefaultRestoreManagerTest extends PulseTestCase
{
    private DefaultRestoreManager manager;
    private JobManager jobManager;
    private File tmpDir;
    private ArchiveFactory factory;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = FileSystemUtils.createTempDir();

        jobManager = new JobManager();

        manager = new DefaultRestoreManager();
        manager.setPaths(new Data(new File(tmpDir, "data")));
        manager.setJobManager(jobManager);

        factory = new ArchiveFactory();
        factory.setTmpDirectory(new File(tmpDir, "tmp"));
    }

    protected void tearDown() throws Exception
    {
        manager = null;
        jobManager = null;
        factory = null;

        removeDirectory(tmpDir);

        super.tearDown();
    }

    public void testRestore() throws ArchiveException
    {
        Archive archive = factory.createArchive();
        
        File archiveFile = factory.exportArchive(archive, new File(tmpDir, "export"));

        manager.add(new NoopArchiveableComponent());

        manager.prepareRestore(archiveFile);
        List<Task> tasks = manager.previewRestore();
        assertEquals(1, tasks.size());

        Monitor monitor = manager.getMonitor();
        assertFalse(monitor.isStarted());

        Task task = tasks.get(0);

        manager.restoreArchive();

        TaskFeedback feedback = monitor.getProgress(task);
        assertTrue(feedback.isFinished());

        manager.getBackedupArchive();
    }

    public void testFailedRestore() throws ArchiveException
    {
        Archive archive = factory.createArchive();

        File archiveFile = factory.exportArchive(archive, new File(tmpDir, "export"));

        manager.add(new FailingArchiveableComponent());

        manager.prepareRestore(archiveFile);
        List<Task> tasks = manager.previewRestore();
        assertEquals(1, tasks.size());

        Task task = tasks.get(0);

        Monitor monitor = manager.getMonitor();
        assertFalse(monitor.isStarted());

        manager.restoreArchive();

        TaskFeedback feedback = monitor.getProgress(task);
        assertTrue(feedback.isFinished());
        assertTrue(feedback.isFailed());

        assertTrue(monitor.isFinished());
        assertTrue(monitor.isFailed());

        manager.getBackedupArchive();
    }

    public void testDoesNotRestoreNonExistant() throws ArchiveException
    {
        Archive archive = factory.createArchive();
        File archiveFile = factory.exportArchive(archive, new File(tmpDir, "export"));

        manager.add(new NonExistantArchiveableComponent());
        manager.prepareRestore(archiveFile);

        assertEquals(0, manager.previewRestore().size());

        // Ensure that the component is not restored (it would throw if it
        // was).
        manager.restoreArchive();
        assertTrue(manager.getMonitor().isFinished());
        assertTrue(manager.getMonitor().isSuccessful());
    }

    private static abstract class TestArchiveableComponent extends AbstractArchiveableComponent
    {
        private String name;

        protected TestArchiveableComponent(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return getName();
        }
    }

    private static class FailingArchiveableComponent extends TestArchiveableComponent
    {
        public FailingArchiveableComponent()
        {
            super("failing");
        }

        public void backup(File archive) throws ArchiveException
        {
            throw new ArchiveException("Expected failure.");
        }

        public void restore(File archive) throws ArchiveException
        {
            throw new ArchiveException("Expected failure.");
        }
    }

    private static class NonExistantArchiveableComponent extends TestArchiveableComponent
    {
        public NonExistantArchiveableComponent()
        {
            super("non-existant");
        }

        public void backup(File archive) throws ArchiveException
        {
        }

        @Override
        public boolean exists(File dir)
        {
            return false;
        }

        public void restore(File archive) throws ArchiveException
        {
            throw new ArchiveException("Should never be called");
        }
    }
}
