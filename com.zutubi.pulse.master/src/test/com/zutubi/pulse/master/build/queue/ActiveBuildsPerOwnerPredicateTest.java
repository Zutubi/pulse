package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.Project;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.LinkedList;
import java.util.List;

public class ActiveBuildsPerOwnerPredicateTest extends BaseQueueTestCase
{
    private BuildQueue buildQueue;
    private List<ActivatedRequest> activeRequests;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        activeRequests = new LinkedList<ActivatedRequest>();
        buildQueue = mock(BuildQueue.class);

        stub(buildQueue.getActivatedRequests()).toReturn(activeRequests);
    }

    public void testAllowActive()
    {
        activateRequest(createRequest("a"));
        activateRequest(createRequest("a"));

        assertFalse(canActivate(createRequest("a"), 0));
        assertFalse(canActivate(createRequest("a"), 1));
        assertFalse(canActivate(createRequest("a"), 2));
        assertTrue(canActivate(createRequest("a"), 3));
    }

    public void testPredicateBoundToOwner()
    {
        activateRequest(createRequest("a"));
        assertTrue(canActivate(createRequest("b"), 1));
    }

    private boolean canActivate(BuildRequestEvent request, int concurrentBuilds)
    {
        Project project = (Project) request.getOwner();
        project.getConfig().getOptions().setConcurrentBuilds(concurrentBuilds);
        
        QueuedRequestPredicate predicate = new ActiveBuildsPerOwnerPredicate(buildQueue);
        return predicate.apply(new QueuedRequest(request));
    }

    private void activateRequest(BuildRequestEvent request)
    {
        activeRequests.add(new ActivatedRequest(request));
    }
}
