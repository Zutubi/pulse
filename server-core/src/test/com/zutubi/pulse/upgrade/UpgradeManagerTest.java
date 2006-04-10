package com.zutubi.pulse.upgrade;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.upgrade.tasks.MockUpgradeTask;
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
        upgradeManager.addTask(new MockUpgradeTask(40));
        upgradeManager.addTask(new MockUpgradeTask(20));

        Version fromVersion = new Version("x", "y", "2");
        Version toVersion = new Version("xx", "yy", "35");

        assertTrue(upgradeManager.isUpgradeRequired(fromVersion, toVersion));

        List<UpgradeTask> preview = upgradeManager.determineRequiredUpgradeTasks(fromVersion, toVersion);
        assertNotNull(preview);
        assertEquals(2, preview.size());
        assertEquals(20, preview.get(0).getBuildNumber());
        assertEquals(30, preview.get(1).getBuildNumber());
    }
}