package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;

public class OwnerCompleteQueuePredicateTest extends BaseQueueTestCase
{
    private OwnerCompleteQueuePredicate predicate;
    private Project ownerA;
    private Project ownerB;
    private BuildQueue buildQueue;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        buildQueue = objectFactory.buildBean(BuildQueue.class);
        ownerA = createProject("projectA");
        ownerB = createProject("projectB");
    }

    public void testOwnerQueued()
    {
        BuildRequestEvent requestEvent = createRequest(ownerA);
        buildQueue.enqueue(queue(requestEvent));

        QueuedRequest qr = new QueuedRequest(createRequest(ownerB), new OwnerCompleteQueuePredicate(buildQueue, ownerA));
        assertFalse(qr.satisfied());
    }

    public void testOwnerActivated()
    {
        BuildRequestEvent requestEvent = createRequest(ownerA);
        buildQueue.enqueue(active(requestEvent));

        QueuedRequest qr = new QueuedRequest(createRequest(ownerB), new OwnerCompleteQueuePredicate(buildQueue, ownerA));
        assertFalse(qr.satisfied());
    }

    public void testOwnerNotInQueue()
    {
        QueuedRequest qr = new QueuedRequest(createRequest(ownerB), new OwnerCompleteQueuePredicate(buildQueue, ownerA));
        assertTrue(qr.satisfied());
    }
}
