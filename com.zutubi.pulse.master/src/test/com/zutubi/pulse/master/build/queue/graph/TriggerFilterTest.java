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

package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.adt.TreeNode;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Map;

import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.EXTENSION_PROJECT_TRIGGERS;
import static org.mockito.Mockito.*;

public class TriggerFilterTest extends GraphFilterTestCase
{
    private Scheduler scheduler;
    private TriggerFilter filter;
    private Trigger trigger;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        scheduler = mock(Scheduler.class);
        stub(scheduler.getTrigger(anyLong())).toAnswer(new Answer<Trigger>()
        {
            public Trigger answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return trigger;
            }
        });
        filter = new TriggerFilter();
        filter.setScheduler(scheduler);

        trigger = mock(Trigger.class);
    }

    public void testNoTrigger()
    {
        ProjectConfiguration util = project("util");
        Map triggers = (Map) util.getExtensions().get(EXTENSION_PROJECT_TRIGGERS);
        triggers.clear();

        TreeNode<BuildGraphData> node = node(util);

        applyFilter(filter, node);

        assertEquals(1, filter.getToTrim().size());
        assertEquals(node(util), filter.getToTrim().get(0));
    }

    public void testActiveTrigger()
    {
        ProjectConfiguration util = project("util");

        TreeNode<BuildGraphData> node = node(util);

        stub(trigger.isActive()).toReturn(Boolean.TRUE);

        applyFilter(filter, node);

        assertEquals(0, filter.getToTrim().size());
    }

    public void testPausedTrigger()
    {
        ProjectConfiguration util = project("util");

        TreeNode<BuildGraphData> node = node(util);
        stub(trigger.isActive()).toReturn(Boolean.FALSE);

        applyFilter(filter, node);

        assertEquals(1, filter.getToTrim().size());
        assertEquals(node(util), filter.getToTrim().get(0));
    }
}
