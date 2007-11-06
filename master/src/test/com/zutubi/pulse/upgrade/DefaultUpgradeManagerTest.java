package com.zutubi.pulse.upgrade;

import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultUpgradeManagerTest extends UpgradeTestCase
{
    private DefaultUpgradeManager upgradeManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        upgradeManager = new DefaultUpgradeManager();
    }

    protected void tearDown() throws Exception
    {
        upgradeManager = null;

        super.tearDown();
    }

    public void testIsUpgradeRequired() throws UpgradeException
    {
        assertFalse(upgradeManager.isUpgradeRequired());

        upgradeManager.add(new UpgradeableComponentAdapter());
        assertFalse(upgradeManager.isUpgradeRequired());

        upgradeManager.add(new UpgradeableComponentAdapter()
        {
            public boolean isUpgradeRequired()
            {
                return true;
            }
        });

        assertTrue(upgradeManager.isUpgradeRequired());
    }

    public void testPreviewUpgrade() throws UpgradeException
    {
        upgradeManager.prepareUpgrade();
        List<UpgradeTaskGroup> upgrades = upgradeManager.previewUpgrade();
        assertEquals(0, upgrades.size());

        List<UpgradeTaskAdapter> tasks = new LinkedList<UpgradeTaskAdapter>();
        tasks.add(new UpgradeTaskAdapter());
        tasks.add(new UpgradeTaskAdapter());
        upgradeManager.add(new UpgradeableComponentAdapter(tasks));

        upgradeManager.prepareUpgrade();
        upgrades = upgradeManager.previewUpgrade();
        assertEquals(1, upgrades.size());
        assertEquals(2, upgrades.get(0).getTasks().size());
    }

    public void testExecuteUpgrade() throws UpgradeException
    {
        List<UpgradeTaskAdapter> tasks = new LinkedList<UpgradeTaskAdapter>();
        tasks.add(new UpgradeTaskAdapter());

        upgradeManager.add(new UpgradeableComponentAdapter(tasks));

        upgradeManager.prepareUpgrade();
        upgradeManager.executeUpgrade();

        for (UpgradeTask task : tasks)
        {
            UpgradeTaskAdapter adapter = (UpgradeTaskAdapter) task;
            assertTrue(adapter.isExecuted());
        }
    }

    public void testAbortOnFailure() throws UpgradeException
    {
        List<UpgradeTaskAdapter> tasks = new LinkedList<UpgradeTaskAdapter>();
        tasks.add(new UpgradeTaskAdapter(true, true));
        tasks.add(new UpgradeTaskAdapter());

        upgradeManager.add(new UpgradeableComponentAdapter(tasks));

        upgradeManager.prepareUpgrade();
        upgradeManager.executeUpgrade();

        assertTrue(tasks.get(0).isExecuted());
        assertFalse(tasks.get(1).isExecuted());
    }

    public void testNoAbortOnFailure() throws UpgradeException
    {
        List<UpgradeTaskAdapter> tasks = new LinkedList<UpgradeTaskAdapter>();
        tasks.add(new UpgradeTaskAdapter(false, true));
        tasks.add(new UpgradeTaskAdapter());

        upgradeManager.add(new UpgradeableComponentAdapter(tasks));

        upgradeManager.prepareUpgrade();
        upgradeManager.executeUpgrade();

        assertTrue(tasks.get(0).isExecuted());
        assertTrue(tasks.get(1).isExecuted());
    }
}