package com.zutubi.pulse.master.build.queue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.Arrays;

public class HeadOfOwnersCanBuildNowQueuePredicateTest extends BaseQueueTestCase
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
        predicate = new HeadOfOwnersCanBuildNowQueuePredicate(buildQueue);

        r1 = new QueuedRequest(createRequest("a"), pendingDependency());
        r2 = new QueuedRequest(createRequest("a"), satisfiedDependency());
        r3 = new QueuedRequest(createRequest("a"));
        r4 = new QueuedRequest(createRequest("a"));

        stub(buildQueue.getQueuedRequests()).toReturn(Arrays.asList(r3, r2, r1));
    }

    public void testRequestAtHeadOfQueue()
    {
        assertTrue(predicate.satisfied(r2));
    }

    public void testRequestNotAtHeadOfQueue()
    {
        assertFalse(predicate.satisfied(r1));
        assertFalse(predicate.satisfied(r3));
    }

    public void testRequestNotInQueue()
    {
        assertFalse(predicate.satisfied(r4));
    }

    private QueuedRequestPredicate pendingDependency()
    {
        return new SatisfiedDependencyPredicate(false);
    }

    private QueuedRequestPredicate satisfiedDependency()
    {
        return new SatisfiedDependencyPredicate(true);
    }

    private class SatisfiedDependencyPredicate implements QueuedRequestPredicate, DependencyPredicate
    {
        private boolean satisfied = false;

        private SatisfiedDependencyPredicate(boolean satisfied)
        {
            this.satisfied = satisfied;
        }

        public Object getOwner()
        {
            return null;
        }

        public boolean satisfied(QueuedRequest queuedRequest)
        {
            return satisfied;
        }
    }
}
