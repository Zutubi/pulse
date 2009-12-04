package com.zutubi.pulse.master.build.queue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.Arrays;

public class OneActiveBuildPerOwnerPredicateTest extends BaseQueueTestCase
{
    private QueuedRequestPredicate predicate;
    private BuildQueue buildQueue;
    private ActivatedRequest r1;
    private QueuedRequest r2;
    private QueuedRequest r3;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        buildQueue = mock(BuildQueue.class);
        predicate = new OneActiveBuildPerOwnerPredicate(buildQueue);

        r1 = new ActivatedRequest(createRequest("a"));
        r2 = new QueuedRequest(createRequest("a"));
        r3 = new QueuedRequest(createRequest("b"));

        stub(buildQueue.getActivatedRequests()).toReturn(Arrays.asList(r1));
    }

    public void testRequestAlreadyActive()
    {
        assertFalse(predicate.satisfied(r2));
    }

    public void testRequestNotActive()
    {
        assertTrue(predicate.satisfied(r3));
    }
}
