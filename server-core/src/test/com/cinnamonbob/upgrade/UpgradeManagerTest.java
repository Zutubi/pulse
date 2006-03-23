package com.cinnamonbob.upgrade;

import com.cinnamonbob.Version;
import com.cinnamonbob.upgrade.tasks.MockUpgradeTask;
import junit.framework.TestCase;

import java.util.List;

/**
 * <class-comment/>
 */
public class UpgradeManagerTest extends TestCase
{
    private DefaultUpgradeManager upgradeManager;

    public UpgradeManagerTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        upgradeManager = new DefaultUpgradeManager();
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.

        super.tearDown();
    }

    public void testUpgradeTaskSelection()
    {
        upgradeManager.addTask(new MockUpgradeTask(30));
        upgradeManager.addTask(new MockUpgradeTask(20));
        upgradeManager.addTask(new MockUpgradeTask(40));

        Version fromVersion = new Version("x", "y", "2");
        Version toVersion = new Version("xx", "yy", "55");

        assertTrue(upgradeManager.isUpgradeRequired(fromVersion, toVersion));

        List<UpgradeTask> preview = upgradeManager.previewUpgrade(fromVersion, toVersion);
        assertNotNull(preview);
        assertEquals(3, preview.size());

        List<UpgradeTask> executedTasks = upgradeManager.executeUpgrade(fromVersion, toVersion);
        assertNotNull(executedTasks);
        assertEquals(3, executedTasks.size());
        assertEquals(20, executedTasks.get(0).getBuildNumber());
        assertEquals(30, executedTasks.get(1).getBuildNumber());
        assertEquals(40, executedTasks.get(2).getBuildNumber());
    }
}