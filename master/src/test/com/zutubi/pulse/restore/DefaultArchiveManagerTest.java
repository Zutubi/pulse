package com.zutubi.pulse.restore;

import com.zutubi.pulse.bootstrap.Data;
import com.zutubi.pulse.monitor.Monitor;
import com.zutubi.pulse.monitor.Task;
import com.zutubi.pulse.monitor.TaskFeedback;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.util.List;

/**
 *
 *
 */
public class DefaultArchiveManagerTest extends PulseTestCase
{
    private DefaultArchiveManager manager;

    private File tmpDir;
    private ArchiveFactory factory;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = FileSystemUtils.createTempDir();

        manager = new DefaultArchiveManager();
        manager.setPaths(new Data(new File(tmpDir, "data")));

        factory = new ArchiveFactory();
        factory.setTmpDirectory(new File(tmpDir, "tmp"));
        factory.setArchiveDirectory(new File(tmpDir, "archive"));
    }

    protected void tearDown() throws Exception
    {
        manager = null;

        removeDirectory(tmpDir);

        super.tearDown();
    }

    public void testRestore() throws ArchiveException
    {
        Archive archive = factory.createArchive();
        
        File archiveFile = factory.exportArchive(archive);

        manager.add(new NoopArchiveableComponent());

        Monitor monitor = manager.getTaskMonitor();
        assertFalse(monitor.isStarted());

        Archive preparedArchive = manager.prepareRestore(archiveFile);
        List<Task> tasks = manager.previewRestore();
        assertEquals(1, tasks.size());

        Task task = tasks.get(0);

        manager.restoreArchive();

        TaskFeedback feedback = monitor.getProgress(task);
        assertTrue(feedback.isFinished());

        manager.postRestore();
    }

    public void testFailedRestore() throws ArchiveException
    {
        Archive archive = factory.createArchive();

        File archiveFile = factory.exportArchive(archive);

        manager.add(new FailingArchiveableComponent());

        Monitor monitor = manager.getTaskMonitor();
        assertFalse(monitor.isStarted());

        Archive preparedArchive = manager.prepareRestore(archiveFile);
        List<Task> tasks = manager.previewRestore();
        assertEquals(1, tasks.size());

        Task task = tasks.get(0);

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
