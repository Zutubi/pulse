package com.zutubi.pulse.master.build.queue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.Arrays;

public class HeadOfOwnerQueuePredicateTest extends BaseQueueTestCase
{
    private QueuedRequestPredicate predicate;
    private BuildQueue buildQueue;
    private QueuedRequest r1;
    private QueuedRequest r2;
    private QueuedRequest r3;
    private QueuedRequest r4;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        buildQueue = mock(BuildQueue.class);
        predicate = new HeadOfOwnerQueuePredicate(buildQueue);

        r1 = new QueuedRequest(createRequest("a"));
        r2 = new QueuedRequest(createRequest("a"));
        r3 = new QueuedRequest(createRequest("a"));
        r4 = new QueuedRequest(createRequest("a"));

        stub(buildQueue.getQueuedRequests()).toReturn(Arrays.asList(r3, r2, r1));
    }

    public void testRequestAtHeadOfQueue()
    {
        assertTrue(predicate.apply(r1));
    }

    public void testRequestNotAtHeadOfQueue()
    {
        assertFalse(predicate.apply(r2));
        assertFalse(predicate.apply(r3));
    }

    public void testRequestNotInQueue()
    {
        assertFalse(predicate.apply(r4));
    }
}
