package com.zutubi.pulse.master.scm.polling;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.Predicate;

import java.util.List;
import java.util.Arrays;

public class PollingQueueTest extends PulseTestCase
{
    private PollingQueue queue;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        queue = new PollingQueue();
        queue.setListener(new PollingActivationListener()
        {
            public void onActivation(PollingRequest request)
            {
                
            }
        });
    }

    public void testRequestsAreQueuedInOrder()
    {
        PollingRequest r1 = queueRequest();
        PollingRequest r2 = queueRequest();
        PollingRequest r3 = queueRequest();
        PollingRequest r4 = queueRequest();

        queue.enqueue(r1, r2, r3, r4);

        List<PollingRequest> queuedRequests = queue.getSnapshot().getQueuedRequests();
        assertEquals(r4, queuedRequests.get(0));
        assertEquals(r3, queuedRequests.get(1));
        assertEquals(r2, queuedRequests.get(2));
        assertEquals(r1, queuedRequests.get(3));
    }

    public void testCancelQueuedRequest()
    {
        PollingRequest r1 = queueRequest();

        queue.enqueue(r1);
        assertQueued(r1);

        List<PollingRequest> dequeued = queue.dequeue(r1);
        assertEquals(1, dequeued.size());
        assertEquals(r1, dequeued.get(0));
        assertQueued();
    }

    public void testCancelActivatedRequest()
    {
        PollingRequest r1 = activeRequest();

        queue.enqueue(r1);
        assertActivated(r1);

        assertEquals(0, queue.dequeue(r1).size());
        assertActivated(r1);
    }

    public void testCancelUnknownRequest()
    {
        PollingRequest r1 = activeRequest();

        assertEquals(0, queue.dequeue(r1).size());
    }

    public void testCompleteQueuedRequest()
    {
        PollingRequest r1 = queueRequest();

        queue.enqueue(r1);
        assertQueued(r1);

        assertEquals(0, queue.complete(r1).size());
        assertQueued(r1);
    }

    public void testCompleteActivatedRequest()
    {
        PollingRequest r1 = activeRequest();

        queue.enqueue(r1);
        assertActivated(r1);

        List<PollingRequest> completed = queue.complete(r1);
        assertEquals(1, completed.size());
        assertEquals(r1, completed.get(0));
        assertActivated();
    }

    public void testCompleteUnknownRequest()
    {
        PollingRequest r1 = activeRequest();

        assertEquals(0, queue.complete(r1).size());
    }

    public void testActivateCalled()
    {
        final boolean[] called = new boolean[]{false};
        queue.setListener(new PollingActivationListener()
        {
            public void onActivation(PollingRequest request)
            {
                called[0] = true;
            }
        });

        PollingRequest r1 = activeRequest();
        queue.enqueue(r1);

        assertTrue(called[0]);
    }

    private PollingRequest activeRequest()
    {
        return new PollingRequest(new Project(), new ActivateThisRequest());
    }

    private PollingRequest queueRequest()
    {
        return new PollingRequest(new Project(), new QueueThisRequest());
    }

    private void assertQueued(PollingRequest... requests)
    {
        assertItemsSame(Arrays.asList(requests), queue.getSnapshot().getQueuedRequests());
    }

    private void assertActivated(PollingRequest... requests)
    {
        assertItemsSame(Arrays.asList(requests), queue.getSnapshot().getActivatedRequests());
    }

    private class QueueThisRequest implements Predicate<PollingRequest>
    {
        public boolean satisfied(PollingRequest request)
        {
            return false;
        }
    }

    private class ActivateThisRequest implements Predicate<PollingRequest>
    {
        public boolean satisfied(PollingRequest request)
        {
            return true;
        }
    }

}
