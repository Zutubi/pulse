package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.scheduling.Trigger;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.EXTENSION_PROJECT_TRIGGERS;
import com.zutubi.util.TreeNode;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Map;

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
        Project util = project("util");
        Map triggers = (Map) util.getConfig().getExtensions().get(EXTENSION_PROJECT_TRIGGERS);
        triggers.clear();

        TreeNode<GraphData> node = node(util);

        applyFilter(filter, node);

        assertEquals(1, filter.getToTrim().size());
        assertEquals(node(util), filter.getToTrim().get(0));
    }

    public void testActiveTrigger()
    {
        Project util = project("util");

        TreeNode<GraphData> node = node(util);

        stub(trigger.isActive()).toReturn(Boolean.TRUE);

        applyFilter(filter, node);

        assertEquals(0, filter.getToTrim().size());
    }

    public void testPausedTrigger()
    {
        Project util = project("util");

        TreeNode<GraphData> node = node(util);
        stub(trigger.isActive()).toReturn(Boolean.FALSE);

        applyFilter(filter, node);

        assertEquals(1, filter.getToTrim().size());
        assertEquals(node(util), filter.getToTrim().get(0));
    }
}
