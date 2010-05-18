package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.io.File;
import java.io.IOException;

public class KeepMostRecentXCleanupStrategyTest extends PulseTestCase
{
    private File tmp;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = createTempDirectory();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        super.tearDown();
    }

    public void testEmptyCandidateList()
    {
        BackupCleanupStrategy strategy = new KeepMostRecentXCleanupStrategy(10);
        File[] targets = strategy.getCleanupTargets(null);
        assertNotNull(targets);
        assertEquals(0, targets.length);

        targets = strategy.getCleanupTargets(new File[0]);
        assertNotNull(targets);
        assertEquals(0, targets.length);
    }

    public void testCandidateListLessThanX() throws IOException
    {
        createCandidateFiles(2);

        BackupCleanupStrategy strategy = new KeepMostRecentXCleanupStrategy(10);
        File[] targets = strategy.getCleanupTargets(tmp.listFiles());
        assertNotNull(targets);
        assertEquals(0, targets.length);
    }

    public void testCandidateListEqualToX() throws IOException
    {
        createCandidateFiles(2);

        BackupCleanupStrategy strategy = new KeepMostRecentXCleanupStrategy(2);
        File[] targets = strategy.getCleanupTargets(tmp.listFiles());
        assertNotNull(targets);
        assertEquals(0, targets.length);
    }

    public void testCandidateListGreaterThanX() throws IOException
    {
        createCandidateFiles(3);

        BackupCleanupStrategy strategy = new KeepMostRecentXCleanupStrategy(2);
        File[] targets = strategy.getCleanupTargets(tmp.listFiles());
        assertNotNull(targets);
        assertEquals(1, targets.length);
        assertEquals("backup-0", targets[0].getName());
    }

    public void testXEqualsZero() throws IOException
    {
        createCandidateFiles(2);

        BackupCleanupStrategy strategy = new KeepMostRecentXCleanupStrategy(0);
        File[] targets = strategy.getCleanupTargets(tmp.listFiles());
        assertNotNull(targets);
        assertEquals(2, targets.length);
    }

    public void testMostRecentReturned() throws IOException
    {
        createCandidateFiles(5);

        BackupCleanupStrategy strategy = new KeepMostRecentXCleanupStrategy(3);
        File[] targets = strategy.getCleanupTargets(tmp.listFiles());
        assertNotNull(targets);
        assertEquals(2, targets.length);
        assertEquals("backup-0", targets[0].getName());
        assertEquals("backup-1", targets[1].getName());
    }

    private void createCandidateFiles(int x) throws IOException
    {
        for (int i = 0; i < x; i++)
        {
            createTmpFile("backup-" + i).setLastModified(System.currentTimeMillis() - (x - i) * 1000);
        }
    }

    private File createTmpFile(String fileName) throws IOException
    {
        File f = new File(tmp, fileName);
        f.createNewFile();
        return f;
    }

}
