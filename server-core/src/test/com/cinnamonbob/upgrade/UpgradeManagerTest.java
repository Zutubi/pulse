package com.cinnamonbob.upgrade;

import junit.framework.*;
import com.cinnamonbob.upgrade.tasks.MockUpgradeTask;

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

    public void testNoopUpgrade()
    {
        upgradeManager.addTask(new MockUpgradeTask());
        assertTrue(upgradeManager.isUpgradeRequired());
        List<UpgradeTask> executedTasks = upgradeManager.executeUpgrade();
        assertNotNull(executedTasks);
        assertEquals(1, executedTasks.size());
        assertFalse(upgradeManager.isUpgradeRequired());
    }

    public void testUpgradeTaskSelection()
    {
        upgradeManager.addTask(new MockUpgradeTask(30));
        upgradeManager.addTask(new MockUpgradeTask(20));
        upgradeManager.addTask(new MockUpgradeTask(40));

        assertTrue(upgradeManager.isUpgradeRequired());
        List<UpgradeTask> executedTasks = upgradeManager.executeUpgrade();
        assertNotNull(executedTasks);
        assertEquals(3, executedTasks.size());
        assertEquals(20, executedTasks.get(0).getBuildNumber());
        assertEquals(30, executedTasks.get(1).getBuildNumber());
        assertEquals(40, executedTasks.get(2).getBuildNumber());
    }
}