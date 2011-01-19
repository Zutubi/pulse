package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.model.Project;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.Arrays;

public class HasDependencyOnPredicateTest extends BaseQueueTestCase
{
    private BuildQueue buildQueue;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        buildQueue = mock(BuildQueue.class);
    }

    public void testDependencies()
    {
        Project owner = createProject("a");

        HasDependencyOnPredicate predicate = new HasDependencyOnPredicate(buildQueue, owner);

        QueuedRequest r1 = new QueuedRequest(createRequest("b"));
        QueuedRequest r2 = new QueuedRequest(createRequest("c"));
        QueuedRequest r3 = new QueuedRequest(createRequest("d"));
        QueuedRequest r4 = new QueuedRequest(createRequest("e"));

        addDependency(r1, owner);
        addDependency(r2, r1.getOwner());
        addDependency(r3, r2.getOwner());

        stub(buildQueue.getQueuedRequests()).toReturn(Arrays.asList(r4, r3, r2, r1));

        assertTrue(predicate.satisfied(r1));
        assertTrue(predicate.satisfied(r2));
        assertTrue(predicate.satisfied(r3));
        assertFalse(predicate.satisfied(r4));
    }

    private void addDependency(QueuedRequest request, Object dependentOwner)
    {
        request.addPredicate(new DependencyCompleteQueuePredicate(buildQueue, dependentOwner));
    }
}
