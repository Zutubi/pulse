package com.zutubi.pulse.master.build.queue;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.events.build.BuildCommencingEvent;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.SequenceManager;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;

/**
 * The scheduling controller is the main component of the scheduling/queueing system.
 * <p/>
 * It is responsible for coordinating the scheduling process, delegating much of the
 * work to the build request handlers and the internal queue.
 */
public class SchedulingController
{
    protected static final String SEQUENCE_BUILD_ID = "BUILD_ID";

    private static final Logger LOG = Logger.getLogger(SchedulingController.class);
    private static final Messages I18N = Messages.getInstance(SchedulingController.class);

    private final Lock lock = new ReentrantLock();
    
    private boolean running = true;
    
    private ObjectFactory objectFactory;
    private SequenceManager sequenceManager;
    private ProjectManager projectManager;
    private BuildRequestRegistry buildRequestRegistry;
    private BuildQueue buildQueue;
    private AccessManager accessManager;

    private Map<Long, BuildRequestHandler> handlers = new HashMap<Long, BuildRequestHandler>();

    public boolean isRunning()
    {
        lock.lock();
        try
        {
            return running;
        }
        finally
        {
            lock.unlock();
        }
    }

    public void pause()
    {
        lock.lock();
        try
        {
            LOG.info("Build queue paused by '" + SecurityUtils.getLoggedInUsername() + "'");
            running = false;
        }
        finally
        {
            lock.unlock();
        }        
    }

    public void resume()
    {
        lock.lock();
        try
        {
            LOG.info("Build queue resumed by '" + SecurityUtils.getLoggedInUsername() + "'");
            running = true;
        }
        finally
        {
            lock.unlock();
        }        
    }

    /**
     * Handles a request for a build.  Events are forwarded directly here by the
     * {@link FatController}.
     * 
     * @param request the request event to handle
     */
    void handleBuildRequest(final BuildRequestEvent request)
    {
        BuildRequestHandler requestHandler = getRequestHandler(request);
        lock.lock();
        try
        {
            if (running)
            {
                final List<QueuedRequest> candidates = new LinkedList<QueuedRequest>(requestHandler.prepare(request));
    
                List<BuildRequestEvent> allRequests = newArrayList(transform(candidates, new ExtractRequestFunction<QueuedRequest>()));
                projectManager.runUnderProjectLocks(new Runnable()
                {
                    public void run()
                    {
                        CanBuildPredicate canBuild = objectFactory.buildBean(CanBuildPredicate.class);
    
                        List<QueuedRequest> accepted = filterCanBuildRequestsInPlace(candidates);
                        List<QueuedRequest> rejected = new LinkedList<QueuedRequest>(candidates);
    
                        // can only proceed if the original request was accepted.
                        boolean canProceed = any(accepted, new HasIdPredicate<QueuedRequest>(request.getId()));
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
                            if (canBuild.apply(rejectedRequest))
                            {
                                buildRequestRegistry.requestRejected(event, I18N.format("rejected.related.project.state"));
                            }
                            else
                            {
                                buildRequestRegistry.requestRejected(event, I18N.format("rejected.project.state", project.getState().toString()));
                            }
                        }
                    }
                }, getAffectedProjectIds(allRequests));
            }
            else
            {
                buildRequestRegistry.requestRejected(request, I18N.format("rejected.paused"));
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
        @SuppressWarnings({"unchecked"})
        CanBuildPredicate<QueuedRequest> canBuild = objectFactory.buildBean(CanBuildPredicate.class);

        List<QueuedRequest> accepted = newLinkedList(Iterables.filter(candidates, canBuild));
        List<QueuedRequest> rejected = newLinkedList(Iterables.filter(candidates, not(canBuild)));

        // Now we need to go through and move any requests that are in the accepted list that
        // depend on a request in the rejected list into the rejected list.  We do this until
        // we identify no new rejects.

        while (true)
        {
            final List<Object> rejectedOwners = newArrayList(transform(rejected, new ExtractOwnerFunction<QueuedRequest>()));
            Collection<QueuedRequest> acceptedThatDependOnRejected = filter(accepted, new Predicate<QueuedRequest>()
            {
                public boolean apply(QueuedRequest queuedRequest)
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

    /**
     * Handles a build commencing.    Events are forwarded directly here by the
     * {@link FatController}.
     * 
     * @param event the commencing event to handle
     */
    void handleBuildCommencing(BuildCommencingEvent event)
    {
        buildQueue.commencing(event.getBuildResult().getId());
    }

    /**
     * Handles a build completing.  Events are forwarded directly here by the {@link FatController}.
     * 
     * @param event the completed event to handle
     */
    void handleBuildCompleted(BuildCompletedEvent event)
    {
        if (!handlers.containsKey(event.getMetaBuildId()))
        {
            // We are processing an existing request, we definitely do not
            // expect to not know its handler.
            throw new IllegalArgumentException("Unknown meta build handler.");
        }

        BuildRequestHandler requestHandler = handlers.get(event.getMetaBuildId());

        lock.lock();
        try
        {
            // if all requests are finished, return true, else return false.
            BuildResult result = event.getBuildResult();
            Object owner = result.getOwner();

            ActivatedRequest requestToComplete = find(buildQueue.getActivatedRequests(),
                    new HasMetaIdAndOwnerPredicate<ActivatedRequest>(requestHandler.getMetaBuildId(), owner),
                    null
            );

            List<RequestHolder> requestsToComplete = new LinkedList<RequestHolder>();
            requestsToComplete.add(requestToComplete);

            if (!result.healthy())
            {
                // Identify the queued requests that depend on this failed build so they can be cancelled.
                @SuppressWarnings("unchecked")
                Predicate<QueuedRequest> toCancelPredicate = Predicates.and(
                        new HasMetaIdPredicate<QueuedRequest>(requestHandler.getMetaBuildId()),
                        new HasDependencyOnPredicate(buildQueue, requestToComplete.getOwner())
                );
                
                requestsToComplete.addAll(filter(buildQueue.getQueuedRequests(), toCancelPredicate));
            }
            internalCompleteRequests(requestHandler, requestsToComplete);
        }
        finally
        {
            lock.unlock();
        }
    }

    private void internalCompleteRequests(final BuildRequestHandler requestHandler, final Collection<RequestHolder> completedRequests)
    {
        final List<BuildRequestEvent> requestEvents = newArrayList(transform(completedRequests, new ExtractRequestFunction<RequestHolder>()));

        lock.lock();
        try
        {
            projectManager.runUnderProjectLocks(new Runnable()
            {
                public void run()
                {
                    // pause the activation since we may be making multiple related changes.  We do not
                    // accidentally want to activate a request when we cancel its remaining dependency.
                    synchronized (buildQueue)
                    {
                        try
                        {
                            buildQueue.pauseActivation();

                            // cancel all existing queued requests, complete activated requests.
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
            }, getAffectedProjectIds(requestEvents));
        }
        finally
        {
            lock.unlock();
        }
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
     * is currently queued.  Enforces the cancel build permission (or, for all requests,
     * server admin permission).
     *
     * @param id the id of the build request to be cancelled, -1 to cancel all requests
     * @return true if a request was cancelled, false otherwise.
     */
    public boolean cancelRequest(long id)
    {
        int cancelledCount = 0;
        if (id == -1)
        {
            accessManager.ensurePermission(AccessManager.ACTION_ADMINISTER, null);

            lock.lock();
            try
            {
                for (QueuedRequest request : buildQueue.getQueuedRequests())
                {
                    cancelledCount += cancelRequestsForMetaBuild(handlers.get(request.getMetaBuildId()));
                }
            }
            finally
            {
                lock.unlock();
            }
        }
        else
        {
            BuildRequestEvent request = buildQueue.getRequest(id);
            if (request != null)
            {
                accessManager.ensurePermission(ProjectConfigurationActions.ACTION_CANCEL_BUILD, request);

                lock.lock();
                try
                {
                    cancelledCount = cancelRequestsForMetaBuild(handlers.get(request.getMetaBuildId()));
                }
                finally
                {
                    lock.unlock();
                }
            }
        }

        return cancelledCount > 0;
    }

    private int cancelRequestsForMetaBuild(BuildRequestHandler requestHandler)
    {
        if (requestHandler != null)
        {
            List<RequestHolder> requests = buildQueue.getMetaBuildRequests(requestHandler.getMetaBuildId());
            Collection<RequestHolder> queuedRequests = filter(requests, Predicates.instanceOf(QueuedRequest.class));
            internalCompleteRequests(requestHandler, queuedRequests);
            return queuedRequests.size();
        }
        else
        {
            return 0;
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

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }
}
