/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.upgrade;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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

        UpgradeTaskGroup taskGroup = upgrades.get(0);
        assertEquals(2, taskGroup.getTasks().size());
    }

    public void testExecuteUpgrade() throws UpgradeException
    {
        List<UpgradeTaskAdapter> tasks = new LinkedList<UpgradeTaskAdapter>();
        tasks.add(new UpgradeTaskAdapter());
        UpgradeableComponentAdapter component = new UpgradeableComponentAdapter(tasks);

        upgradeManager.add(component);

        upgradeManager.prepareUpgrade();
        upgradeManager.executeUpgrade();

        for (UpgradeTask task : tasks)
        {
            UpgradeTaskAdapter adapter = (UpgradeTaskAdapter) task;
            assertTrue(adapter.isExecuted());
        }

        assertTrue(component.wasStarted());
        assertTrue(component.wasCompleted());
        assertFalse(component.wasAborted());

        assertEquals(1, component.completedTasks.size());
        assertEquals(0, component.failedTasks.size());
        assertEquals(0, component.abortedTasks.size());
    }

    public void testAbortOnFailure() throws UpgradeException
    {
        List<UpgradeTaskAdapter> tasks = new LinkedList<UpgradeTaskAdapter>();
        tasks.add(new UpgradeTaskAdapter(true, true));
        tasks.add(new UpgradeTaskAdapter());
        UpgradeableComponentAdapter component = new UpgradeableComponentAdapter(tasks);

        upgradeManager.add(component);

        upgradeManager.prepareUpgrade();
        upgradeManager.executeUpgrade();

        assertTrue(tasks.get(0).isExecuted());
        assertFalse(tasks.get(1).isExecuted());

        assertTrue(component.wasStarted());
        assertFalse(component.wasCompleted());
        assertTrue(component.wasAborted());

        assertEquals(0, component.completedTasks.size());
        assertEquals(1, component.failedTasks.size());
        assertEquals(1, component.abortedTasks.size());
    }

    public void testNoAbortOnFailure() throws UpgradeException
    {
        List<UpgradeTaskAdapter> tasks = new LinkedList<UpgradeTaskAdapter>();
        tasks.add(new UpgradeTaskAdapter(false, true));
        tasks.add(new UpgradeTaskAdapter());
        UpgradeableComponentAdapter component = new UpgradeableComponentAdapter(tasks);

        upgradeManager.add(component);

        upgradeManager.prepareUpgrade();
        upgradeManager.executeUpgrade();

        assertTrue(tasks.get(0).isExecuted());
        assertTrue(tasks.get(1).isExecuted());

        assertTrue(component.wasStarted());
        assertTrue(component.wasCompleted());
        assertFalse(component.wasAborted());

        assertEquals(1, component.completedTasks.size());
        assertEquals(1, component.failedTasks.size());
        assertEquals(0, component.abortedTasks.size());
    }

    public void testConfigurationViaUpgradeableComponentSource()
    {
        List<UpgradeTaskAdapter> tasks = new LinkedList<UpgradeTaskAdapter>();
        tasks.add(new UpgradeTaskAdapter());
        UpgradeableComponent component = new UpgradeableComponentAdapter(tasks);

        upgradeManager.add(new UpgradeableComponentSourceAdapter(component));
        upgradeManager.prepareUpgrade();
        
        assertTrue(upgradeManager.isUpgradeRequired());

        List<UpgradeTaskGroup> preview = upgradeManager.previewUpgrade();
        assertEquals(1, preview.size());
        assertEquals(1, preview.get(0).getTasks().size());
    }

    public void testCompletedUpgradeComponentCallback()
    {
        List<UpgradeTaskAdapter> tasks = new LinkedList<UpgradeTaskAdapter>();
        tasks.add(new UpgradeTaskAdapter());
        UpgradeableComponentAdapter component = new UpgradeableComponentAdapter(tasks);

        upgradeManager.add(new UpgradeableComponentSourceAdapter(component));
        upgradeManager.prepareUpgrade();

        upgradeManager.executeUpgrade();

        assertTrue(component.wasStarted());
        assertTrue(component.wasCompleted());
    }

    public void testAbortedUpgradeComponentCallback()
    {
        List<UpgradeTaskAdapter> tasks = new LinkedList<UpgradeTaskAdapter>();
        tasks.add(new UpgradeTaskAdapter(true, true));
        UpgradeableComponentAdapter component = new UpgradeableComponentAdapter(tasks);

        upgradeManager.add(new UpgradeableComponentSourceAdapter(component));
        upgradeManager.prepareUpgrade();

        upgradeManager.executeUpgrade();

        assertTrue(component.wasStarted());
        assertTrue(component.wasAborted());
    }

    public void testUpgradeComponentCallbackReceivedByCorrectComponent()
    {
        List<UpgradeTaskAdapter> tasks = new LinkedList<UpgradeTaskAdapter>();
        tasks.add(new UpgradeTaskAdapter());
        upgradeManager.add(new UpgradeableComponentSourceAdapter(new UpgradeableComponentAdapter(tasks)));

        List<UpgradeTaskAdapter> tasks2 = new LinkedList<UpgradeTaskAdapter>();
        tasks2.add(new UpgradeTaskAdapter());
        upgradeManager.add(new UpgradeableComponentAdapter(tasks2));
        
        upgradeManager.prepareUpgrade();

        upgradeManager.executeUpgrade();
    }

    public void testAbortOnFailureAcrossComponents()
    {
        // group 1 fails.
        List<UpgradeTaskAdapter> taskGroupA = Arrays.asList(new UpgradeTaskAdapter(true, true), new UpgradeTaskAdapter());
        UpgradeableComponentAdapter componentA = new UpgradeableComponentAdapter(taskGroupA);
        upgradeManager.add(componentA);

        // group 2 succeeds.
        List<UpgradeTaskAdapter> taskGroupB = Arrays.asList(new UpgradeTaskAdapter(), new UpgradeTaskAdapter());
        UpgradeableComponentAdapter componentB = new UpgradeableComponentAdapter(taskGroupB);
        upgradeManager.add(componentB);

        upgradeManager.prepareUpgrade();
        upgradeManager.executeUpgrade();

        assertTrue(componentA.wasStarted());
        assertFalse(componentA.wasCompleted());
        assertTrue(componentA.wasAborted());

        assertTrue(componentB.wasStarted());
        assertTrue(componentB.wasCompleted());
        assertFalse(componentB.wasAborted());
    }
}