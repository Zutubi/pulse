package com.zutubi.pulse.upgrade;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.util.ObjectUtils;

/**
 * <class-comment/>
 */
public class UpgradeProgressMonitorTest extends PulseTestCase
{
    private UpgradeProgressMonitor monitor;

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

    public void testIndividualTaskStates()
    {
        UpgradeTask taskA = new UpgradeTestCase.UpgradeTaskAdapter();
        UpgradeTask taskB = new UpgradeTestCase.UpgradeTaskAdapter();
        UpgradeTask taskC = new UpgradeTestCase.UpgradeTaskAdapter();

        UpgradeTaskGroup group = new UpgradeTaskGroup();
        group.setTasks(ObjectUtils.asList(taskA, taskB, taskC));

        monitor.setTaskGroups(ObjectUtils.asList(group));

        monitor.start();

        monitor.started(group);

        assertEquals(UpgradeStatus.IN_PROGRESS, monitor.getProgress(group).getStatus());

        assertEquals(UpgradeStatus.PENDING, monitor.getProgress(taskA).getStatus());
        assertEquals(UpgradeStatus.PENDING, monitor.getProgress(taskB).getStatus());
        assertEquals(UpgradeStatus.PENDING, monitor.getProgress(taskC).getStatus());

        monitor.started(taskA);
        assertEquals(UpgradeStatus.IN_PROGRESS, monitor.getProgress(taskA).getStatus());
        assertEquals(UpgradeStatus.PENDING, monitor.getProgress(taskB).getStatus());
        assertEquals(UpgradeStatus.PENDING, monitor.getProgress(taskC).getStatus());

        monitor.completed(taskA);
        assertEquals(UpgradeStatus.COMPLETED, monitor.getProgress(taskA).getStatus());
        assertEquals(UpgradeStatus.PENDING, monitor.getProgress(taskB).getStatus());
        assertEquals(UpgradeStatus.PENDING, monitor.getProgress(taskC).getStatus());

        monitor.started(taskB);
        assertEquals(UpgradeStatus.COMPLETED, monitor.getProgress(taskA).getStatus());
        assertEquals(UpgradeStatus.IN_PROGRESS, monitor.getProgress(taskB).getStatus());
        assertEquals(UpgradeStatus.PENDING, monitor.getProgress(taskC).getStatus());

        monitor.failed(taskB);
        assertEquals(UpgradeStatus.COMPLETED, monitor.getProgress(taskA).getStatus());
        assertEquals(UpgradeStatus.FAILED, monitor.getProgress(taskB).getStatus());
        assertEquals(UpgradeStatus.PENDING, monitor.getProgress(taskC).getStatus());

        monitor.started(taskC);
        assertEquals(UpgradeStatus.COMPLETED, monitor.getProgress(taskA).getStatus());
        assertEquals(UpgradeStatus.FAILED, monitor.getProgress(taskB).getStatus());
        assertEquals(UpgradeStatus.IN_PROGRESS, monitor.getProgress(taskC).getStatus());

        monitor.aborted(taskC);
        assertEquals(UpgradeStatus.COMPLETED, monitor.getProgress(taskA).getStatus());
        assertEquals(UpgradeStatus.FAILED, monitor.getProgress(taskB).getStatus());
        assertEquals(UpgradeStatus.ABORTED, monitor.getProgress(taskC).getStatus());

        monitor.completed(group);

        assertEquals(UpgradeStatus.COMPLETED, monitor.getProgress(group).getStatus());

        monitor.finish();
    }

    public void testPercentageComplete()
    {

    }
}
