package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.Sequence;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

public class ExtendedBuildRequestHandlerTest extends BaseQueueTestCase
{
    private ExtendedBuildRequestHandler handler;

    @SuppressWarnings({"UnusedDeclaration"})
    private Sequence sequence;
    @SuppressWarnings({"UnusedDeclaration"})
    private BuildQueue buildQueue;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        sequence = new Sequence()
        {
            public long getNext()
            {
                return nextId.getAndIncrement();
            }
        };
        buildQueue = mock(BuildQueue.class);
        objectFactory.initProperties(this);

        handler = objectFactory.buildBean(ExtendedBuildRequestHandler.class);
        handler.init();
    }

    public void testRebuildZeroDependenciesQueue()
    {
        Project project = createProject("a");
        List<QueuedRequest> toQueue = handler.prepare(createRebuildRequest(project));

        assertRequestMatchesProjects(toQueue, Arrays.asList(project));
    }

    public void testZeroDownstreamBuildsQueued()
    {
        Project project = createProject("a");
        List<QueuedRequest> toQueue = handler.prepare(createRequest(project));

        assertRequestMatchesProjects(toQueue, Arrays.asList(project));
    }

    public void testRebuildOneDependencyQueued()
    {
        Project projectA = createProject("a");
        Project projectB = createProject("b", dependency(projectA));

        List<QueuedRequest> toQueue = handler.prepare(createRebuildRequest(projectB));

        // verify that the enqueued requests contain correctly configured requests as per the list of projects
        assertRequestMatchesProjects(toQueue, Arrays.asList(projectA, projectB));
    }

    public void testOneDownstreamBuildsQueued()
    {
        Project projectA = createProject("a");
        Project projectB = createProject("b", dependency(projectA));

        List<QueuedRequest> toQueue = handler.prepare(createRequest(projectA));

        assertRequestMatchesProjects(toQueue, Arrays.asList(projectA, projectB));
    }

    public void testRebuildMultipleDependenciesQueued()
    {
        Project projectA = createProject("a");
        Project projectB = createProject("b", dependency(projectA));
        Project projectC = createProject("c", dependency(projectB));

        List<QueuedRequest> toQueue = handler.prepare(createRebuildRequest(projectC));

        // verify that the enqueued requests contain correctly configured requests as per the list of projects
        assertRequestMatchesProjects(toQueue, Arrays.asList(projectA, projectB, projectC));
    }

    public void testMultipleDownstreamBuildsQueued()
    {
        Project projectA = createProject("a");
        Project projectB = createProject("b", dependency(projectA));
        Project projectC = createProject("c", dependency(projectB));

        List<QueuedRequest> toQueue = handler.prepare(createRequest(projectA));

        // verify that the enqueued requests contain correctly configured requests as per the list of projects
        assertRequestMatchesProjects(toQueue, Arrays.asList(projectA, projectB, projectC));
    }

    public void testHandlerAssignedMetaBuildIdToRequest()
    {
        BuildRequestEvent requestEvent = createRequest("c");
        assertEquals(0, requestEvent.getMetaBuildId());
        handler.prepare(requestEvent);
        assertFalse(handler.getMetaBuildId() == 0);
        assertEquals(handler.getMetaBuildId(), requestEvent.getMetaBuildId());
    }

    public void testOriginalRequestIsAssociatedWithAppropriateQueuedRequest()
    {
        BuildRequestEvent originalRequest = createRequest("a");
        List<QueuedRequest> toQueue = handler.prepare(originalRequest);

        // verify that the enqueued requests contain correctly generated BuildRequestEvent instances.
        for (int i = 0; i < toQueue.size() - 1; i++)
        {
            QueuedRequest request = toQueue.get(i);
            assertFalse(originalRequest.getId() == request.getRequest().getId());
            assertTrue(originalRequest.getMetaBuildId() == request.getRequest().getMetaBuildId());
        }

        QueuedRequest lastQueuedRequest = toQueue.get(toQueue.size() - 1);
        assertTrue(originalRequest.getId() == lastQueuedRequest.getRequest().getId());
        assertTrue(originalRequest.getMetaBuildId() == lastQueuedRequest.getRequest().getMetaBuildId());
    }

    public void testProjectRequestOnlyQueuedOnce()
    {
        Project util = createProject("util");
        Project libA = createProject("libA", dependency(util));
        Project libB = createProject("libB", dependency(util));
        Project client = createProject("client", dependency(libA), dependency(libB));

        List<QueuedRequest> toQueue = handler.prepare(createRebuildRequest(client));
        assertEquals(4, toQueue.size());
    }

    private void assertRequestMatchesProjects(List<QueuedRequest> requests, List<Project> projects)
    {
        assertEquals(projects.size(), requests.size());

        for (int i = 0; i < projects.size(); i++)
        {
            // check that the ordering of the list is as expected by comparing the
            // expected project.
            BuildRequestEvent request = requests.get(i).getRequest();
            assertEquals(projects.get(i).getConfig(), request.getProjectConfig());

            // get the list of projects we are depending on so that we can verify that
            // the expected predicates have been set in the queued request.
            List<?> dependencyOwners = getDependencyOwners(projects.get(i));

            List<QueuedRequestPredicate> predicates = requests.get(i).getPredicates();
            // predicate size is one per dependency + 2 for the head of queue and one per owner.
            assertEquals(predicates.size(), dependencyOwners.size() + 2);

            // make sure that the dependency predicates map as expected.
            for (Predicate<QueuedRequest> predicate : predicates)
            {
                if (predicate instanceof OwnerCompleteQueuePredicate)
                {
                    Object owner = ((OwnerCompleteQueuePredicate) predicate).getOwner();
                    //noinspection SuspiciousMethodCalls
                    dependencyOwners.remove(owner);
                }
            }

            // are there any dependencies that did not have a matching predicate?
            assertTrue(dependencyOwners.isEmpty());
        }
    }

    private List<Project> getDependencyOwners(Project project)
    {
        return CollectionUtils.map(project.getConfig().getDependencies().getDependencies(), new Mapping<DependencyConfiguration, Project>()
        {
            public Project map(DependencyConfiguration dependency)
            {
                return projectManager.getProject(dependency.getProject().getProjectId(), false);
            }
        });

    }
}
