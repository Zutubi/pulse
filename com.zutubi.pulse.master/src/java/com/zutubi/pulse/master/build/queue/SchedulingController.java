package com.zutubi.pulse.master.build.queue;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.SequenceManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.InstanceOfPredicate;
import com.zutubi.util.Predicate;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.bean.ObjectFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The scheduling controller is the main component of the scheduling/queueing system.
 * <p/>
 * It is responsible for coordinating the scheduling process, delegating much of the
 * work to the build request handlers and the internal queue.
 */
public class SchedulingController implements EventListener
{
    protected static final String SEQUENCE_BUILD_ID = "BUILD_ID";

    private static final Logger LOG = Logger.getLogger(SchedulingController.class);
    private static final Messages I18N = Messages.getInstance(SchedulingController.class);

    private final Lock lock = new ReentrantLock();

    private ObjectFactory objectFactory;
    private SequenceManager sequenceManager;
    private ProjectManager projectManager;
    private BuildRequestRegistry buildRequestRegistry;
    private BuildQueue buildQueue;

    private Map<Long, BuildRequestHandler> handlers = new HashMap<Long, BuildRequestHandler>();

    protected void handleBuildRequest(BuildRequestEvent request)
    {
        BuildRequestHandler requestHandler = getRequestHandler(request);
        lock.lock();
        try
        {
            List<QueuedRequest> candidates = new LinkedList<QueuedRequest>(requestHandler.prepare(request));

            List<BuildRequestEvent> allRequests = CollectionUtils.map(candidates, new ExtractRequestMapping<QueuedRequest>());
            lockProjectStates(allRequests);
            try
            {
                CanBuildPredicate canBuild = objectFactory.buildBean(CanBuildPredicate.class);

                List<QueuedRequest> accepted = filterCanBuildRequestsInPlace(candidates);
                List<QueuedRequest> rejected = new LinkedList<QueuedRequest>(candidates);

                boolean canProceed = CollectionUtils.contains(accepted, new HasIdPredicate<QueuedRequest>(request.getId()));
                if (!canProceed)
                {
                    rejected.addAll(accepted);
                    accepted.clear();
                }

                if (accepted.size() > 0)
                {
                    buildQueue.enqueue(accepted);

                    for (QueuedRequest acceptedRequest : accepted)
                    {
                        Project project = getProject(acceptedRequest.getRequest());
                        if (buildQueue.hasRequest(acceptedRequest.getOwner()) && !acceptedRequest.isPersonal())
                        {
                            transitionProjectState(project, Project.Transition.BUILDING);
                        }
                    }
                }

                for (QueuedRequest rejectedRequest : rejected)
                {
                    BuildRequestEvent event = rejectedRequest.getRequest();
                    Project project = projectManager.getProject(event.getProjectId(), false);
                    if (canBuild.satisfied(rejectedRequest))
                    {
                        buildRequestRegistry.requestRejected(event, I18N.format("rejected.related.project.state"));
                    }
                    else
                    {
                        buildRequestRegistry.requestRejected(event, I18N.format("rejected.project.state", new Object[]{project.getState().toString()}));
                    }
                }
            }
            finally
            {
                unlockProjectStates(allRequests);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    private void transitionProjectState(Project project, Project.Transition transition)
    {
        // Check if we need to make the transition.
        if (transition == Project.Transition.BUILDING && project.getState() == Project.State.BUILDING)
        {
            return;
        }

        if (transition == Project.Transition.IDLE && project.getState() == Project.State.IDLE)
        {
            return;
        }

        if (project.isTransitionValid(transition))
        {
            projectManager.makeStateTransition(project.getId(), transition);
        }
        else
        {
            LOG.warning("Requested transition " + transition + " for project " + project.getName() + "("+project.getId()+") is invalid.");
        }
    }

    private List<QueuedRequest> filterCanBuildRequestsInPlace(List<QueuedRequest> candidates)
    {
        CanBuildPredicate<QueuedRequest> canBuild = objectFactory.buildBean(CanBuildPredicate.class);

        // move the candidates that we can build from the rejected list to the accepted list.
        List<QueuedRequest> rejected = new LinkedList<QueuedRequest>(candidates);
        List<QueuedRequest> accepted = CollectionUtils.filterInPlace(rejected, canBuild);

        // Now we need to go through and move any requests that are in the accepted list that
        // depend on a request in the rejected list into the rejected list.  We do this until
        // we identify no new rejects.

        while (true)
        {
            final List<Object> rejectedOwners = CollectionUtils.map(rejected, new ExtractOwnerMapping<QueuedRequest>());
            List<QueuedRequest> acceptedThatDependOnRejected = CollectionUtils.filter(accepted, new Predicate<QueuedRequest>()
            {
                public boolean satisfied(QueuedRequest queuedRequest)
                {
                    List<Object> dependsOn = queuedRequest.getDependentOwners();
                    for (Object owner : dependsOn)
                    {
                        if (rejectedOwners.contains(owner))
                        {
                            return true;
                        }
                    }
                    return false;
                }
            });

            if (acceptedThatDependOnRejected.size() == 0)
            {
                // We did not identify any new rejects this iteration, so we are done.
                break;
            }
            else
            {
                rejected.addAll(acceptedThatDependOnRejected);
                accepted.removeAll(acceptedThatDependOnRejected);
            }
        }

        candidates.removeAll(accepted);
        return accepted;
    }

    protected void handleBuildCompleted(BuildCompletedEvent event)
    {
        BuildRequestHandler requestHandler = getRequestHandler(event);

        lock.lock();
        try
        {
            // if all requests are finished, return true, else return false.
            BuildResult result = event.getBuildResult();
            Object owner = result.getOwner();

            ActivatedRequest requestToComplete = CollectionUtils.find(buildQueue.getActivatedRequests(),
                    new HasMetaIdAndOwnerPredicate<ActivatedRequest>(requestHandler.getMetaBuildId(), owner)
            );

            List<RequestHolder> requestsToComplete = new LinkedList<RequestHolder>();
            requestsToComplete.add(requestToComplete);

            if (!result.succeeded())
            {
                CollectionUtils.filter(buildQueue.getQueuedRequests(), new HasMetaIdPredicate<QueuedRequest>(requestHandler.getMetaBuildId()), requestsToComplete);
            }
            internalCompleteRequests(requestHandler, requestsToComplete);
        }
        finally
        {
            lock.unlock();
        }
    }

    private void internalCompleteRequests(BuildRequestHandler requestHandler, List<RequestHolder> completedRequests)
    {
        List<BuildRequestEvent> requestEvents = CollectionUtils.map(completedRequests, new ExtractRequestMapping<RequestHolder>());

        lockProjectStates(requestEvents);
        try
        {
            lock.lock();
            try
            {
                // pause the activation since we may be making multiple related changes.  We do not
                // accidentally want to activate a request when we cancel its remaining dependency.
                synchronized (buildQueue)
                {
                    try
                    {
                        buildQueue.pauseActivation();

                        // cancel all existing queued requests, complete actiated requests.
                        for (RequestHolder request : completedRequests)
                        {
                            long requestId = request.getRequest().getId();
                            if (request instanceof ActivatedRequest)
                            {
                                buildQueue.complete(requestId);
                            }
                            else
                            {
                                buildQueue.cancel(requestId);
                            }
                        }
                    }
                    finally
                    {
                        buildQueue.resumeActivation();
                    }
                }

                for (BuildRequestEvent completedRequest : requestEvents)
                {
                    Project project = getProject(completedRequest);
                    if (!completedRequest.isPersonal() && !buildQueue.hasRequest(completedRequest.getOwner()))
                    {
                        transitionProjectState(project, Project.Transition.IDLE);
                    }
                }
                long metaBuildId = requestHandler.getMetaBuildId();
                if (buildQueue.getMetaBuildRequests(metaBuildId).size() == 0)
                {
                    handlers.remove(metaBuildId);
                }
            }
            finally
            {
                lock.unlock();
            }
        }
        finally
        {
            unlockProjectStates(requestEvents);
        }
    }

    private void lockProjectStates(List<BuildRequestEvent> requests)
    {
        projectManager.lockProjectStates(getAffectedProjectIds(requests));
    }

    private void unlockProjectStates(List<BuildRequestEvent> requests)
    {
        projectManager.unlockProjectStates(getAffectedProjectIds(requests));
    }

    private Project getProject(BuildRequestEvent request)
    {
        return projectManager.getProject(request.getProjectConfig().getProjectId(), false);
    }

    private long[] getAffectedProjectIds(List<BuildRequestEvent> requests)
    {
        long[] affectedProjects = new long[requests.size()];
        for (int i = 0; i < affectedProjects.length; i++)
        {
            affectedProjects[i] = requests.get(i).getProjectId();
        }
        return affectedProjects;
    }

    private BuildRequestHandler getRequestHandler(BuildCompletedEvent evt)
    {
        if (!handlers.containsKey(evt.getMetaBuildId()))
        {
            // we are processing an existing request, we definately do not
            // expect to not know its handler.
            throw new IllegalArgumentException();
        }

        return handlers.get(evt.getMetaBuildId());
    }

    private BuildRequestHandler getRequestHandler(long id)
    {
        return handlers.get(buildQueue.getRequest(id).getMetaBuildId());
    }

    private BuildRequestHandler getRequestHandler(BuildRequestEvent request)
    {
        if (request.getMetaBuildId() != 0)
        {
            // we are creating a new handler here, we don't expect the request
            // to already be associated with an existing handler.
            throw new IllegalArgumentException("The build request is already associated with another handler.");
        }

        BaseBuildRequestHandler handler;

        if (request.isPersonal())
        {
            handler = objectFactory.buildBean(PersonalBuildRequestHandler.class);
        }
        else
        {
            handler = objectFactory.buildBean(ExtendedBuildRequestHandler.class);
        }

        handler.setBuildQueue(buildQueue);
        handler.setSequence(sequenceManager.getSequence(SEQUENCE_BUILD_ID));
        handler.init();

        long metaBuildId = handler.getMetaBuildId();
        handlers.put(metaBuildId, handler);

        return handler;
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildRequestEvent.class, BuildCompletedEvent.class};
    }

    public void handleEvent(Event event)
    {
        if (event instanceof BuildRequestEvent)
        {
            handleBuildRequest((BuildRequestEvent) event);
        }
        else if (event instanceof BuildCompletedEvent)
        {
            handleBuildCompleted((BuildCompletedEvent) event);
        }
    }

    /**
     * Retrieve a snapshot of the current state of the build queue.
     *
     * @return a snapshot of the build queue
     */
    public BuildQueueSnapshot getSnapshot()
    {
        return buildQueue.getSnapshot();
    }

    /**
     * Cancel the specified build request.  A build request can only be cancelled if it
     * is currently queued.
     *
     * @param id the id of the build request to be cancelled.
     * @return true if the request was cancelled, false otherwise.
     */
    public boolean cancelRequest(long id)
    {
        BuildRequestHandler requestHandler = getRequestHandler(id);

        lock.lock();
        try
        {
            List<RequestHolder> requests = buildQueue.getMetaBuildRequests(requestHandler.getMetaBuildId());
            List<RequestHolder> queuedRequests = CollectionUtils.filter(requests, new InstanceOfPredicate<RequestHolder>(QueuedRequest.class));
            internalCompleteRequests(requestHandler, queuedRequests);
            return queuedRequests.size() > 0;
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Stop the build queue from activating any more requests.
     */
    public void stop()
    {
        buildQueue.stop();
    }

    /**
     * Get the number of currently activated requests.
     *
     * @return the currently activated request count.
     */
    public int getActivedRequestCount()
    {
        return buildQueue.getActivatedRequestCount();
    }

    /**
     * Get the list of build requests that are currently held in the build queue
     * that belong to the specified owner.
     *
     * @param owner the owner of the build requests.
     * @return a list of build requests.
     */
    public List<BuildRequestEvent> getRequestsByOwner(Object owner)
    {
        return buildQueue.getRequestsByOwner(owner);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setSequenceManager(SequenceManager sequenceManager)
    {
        this.sequenceManager = sequenceManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setBuildRequestRegistry(BuildRequestRegistry buildRequestRegistry)
    {
        this.buildRequestRegistry = buildRequestRegistry;
    }

    public void setBuildQueue(BuildQueue buildQueue)
    {
        this.buildQueue = buildQueue;
    }
}
