package com.zutubi.pulse.upgrade;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.upgrade.tasks.MockUpgradeTask;
import com.zutubi.util.ObjectUtils;

/**
 * <class-comment/>
 */
public class UpgradeProgressMonitorTest extends PulseTestCase
{
    private UpgradeProgressMonitor monitor;

    public UpgradeProgressMonitorTest()
    {
    }

    public UpgradeProgressMonitorTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        monitor = new UpgradeProgressMonitor();
    }

    protected void tearDown() throws Exception
    {
        monitor = null;
        super.tearDown();
    }

    public void testUpgradeTaskNameCanBeReused()
    {
        UpgradeTask a = new MockUpgradeTask();
        UpgradeTask b = new MockUpgradeTask();
        assertEquals(a.getName(), b.getName());

        monitor.setTasks(ObjectUtils.asList(a, b));

        monitor.start(a);

        assertEquals(UpgradeTaskProgress.IN_PROGRESS, monitor.getTaskProgress(a).getStatus());
        assertEquals(UpgradeTaskProgress.PENDING, monitor.getTaskProgress(b).getStatus());

        monitor.complete(a);

        assertEquals(UpgradeTaskProgress.COMPLETE, monitor.getTaskProgress(a).getStatus());
        assertEquals(UpgradeTaskProgress.PENDING, monitor.getTaskProgress(b).getStatus());

        monitor.start(b);

        assertEquals(UpgradeTaskProgress.COMPLETE, monitor.getTaskProgress(a).getStatus());
        assertEquals(UpgradeTaskProgress.IN_PROGRESS, monitor.getTaskProgress(b).getStatus());

        monitor.failed(b);

        assertEquals(UpgradeTaskProgress.COMPLETE, monitor.getTaskProgress(a).getStatus());
        assertEquals(UpgradeTaskProgress.FAILED, monitor.getTaskProgress(b).getStatus());
    }
}
