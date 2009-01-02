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

/**
 *
 *
 */
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

        Archive preparedArchive = manager.prepareRestore(archiveFile);
        List<Task> tasks = manager.previewRestore();
        assertEquals(1, tasks.size());

        Monitor monitor = manager.getTaskMonitor();
        assertFalse(monitor.isStarted());

        Task task = tasks.get(0);

        manager.restoreArchive();

        TaskFeedback feedback = monitor.getProgress(task);
        assertTrue(feedback.isFinished());

        manager.postRestore();
    }

    public void testFailedRestore() throws ArchiveException
    {
        Archive archive = factory.createArchive();

        File archiveFile = factory.exportArchive(archive, new File(tmpDir, "export"));

        manager.add(new FailingArchiveableComponent());

        Archive preparedArchive = manager.prepareRestore(archiveFile);
        List<Task> tasks = manager.previewRestore();
        assertEquals(1, tasks.size());

        Task task = tasks.get(0);

        Monitor monitor = manager.getTaskMonitor();
        assertFalse(monitor.isStarted());

        manager.restoreArchive();

        TaskFeedback feedback = monitor.getProgress(task);
        assertTrue(feedback.isFinished());
        assertTrue(feedback.isFailed());

        assertTrue(monitor.isFinished());
        assertTrue(monitor.isFailed());

        manager.postRestore();
    }

    private abstract class TestArchiveableComponent extends AbstractArchiveableComponent
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

    private class FailingArchiveableComponent extends TestArchiveableComponent
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
}
