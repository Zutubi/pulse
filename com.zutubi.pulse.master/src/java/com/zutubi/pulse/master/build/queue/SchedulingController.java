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
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    private ObjectFactory objectFactory;
    private SequenceManager sequenceManager;
    private ProjectManager projectManager;
    private BuildRequestRegistry buildRequestRegistry;
    private BuildQueue buildQueue;

    private Map<Long, BuildRequestHandler> handlers = new HashMap<Long, BuildRequestHandler>();

    /**
     * Handle a build request.
     *
     * @param request
     */
    protected void handleBuildRequest(BuildRequestEvent request)
    {
        BuildRequestHandler requestHandler = getRequestHandler(request);
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (requestHandler)
        {
            List<QueuedRequest> requestsToQueue = requestHandler.prepare(request);

            List<BuildRequestEvent> preparedRequests = CollectionUtils.map(requestsToQueue, new ExtractRequestMapping());

            // a) lock the necessary projects before moving on.
            lockProjectStates(preparedRequests);
            try
            {
                // Can this build proceed?
                List<BuildRequestEvent> rejectedRequests = new LinkedList<BuildRequestEvent>();
                List<BuildRequestEvent> acceptedRequests = new LinkedList<BuildRequestEvent>(preparedRequests);

                filterCanBuild(acceptedRequests, rejectedRequests);
                if (rejectedRequests.size() == 0) // yes it can.
                {
                    // Send the requests on there way.
                    buildQueue.enqueue(requestsToQueue);

                    for (BuildRequestEvent preparedRequest : preparedRequests)
                    {
                        Project project = getProject(preparedRequest);
                        if (buildQueue.hasRequest(preparedRequest.getOwner()) && !preparedRequest.isPersonal())
                        {
                            if (project.isTransitionValid(Project.Transition.BUILDING))
                            {
                                projectManager.makeStateTransition(preparedRequest.getProjectId(), Project.Transition.BUILDING);
                            }
                        }
                    }
                }
                else
                {
                    // Update the build request registry.
                    for (BuildRequestEvent acceptedRequest : acceptedRequests)
                    {
                        buildRequestRegistry.requestRejected(acceptedRequest, I18N.format("rejected.related.project.state"));
                    }
                    for (BuildRequestEvent rejectedRequest : rejectedRequests)
                    {
                        Project project = projectManager.getProject(rejectedRequest.getProjectId(), false);
                        buildRequestRegistry.requestRejected(rejectedRequest, I18N.format("rejected.project.state", new Object[]{project.getState().toString()}));
                    }
                }
            }
            finally
            {
                unlockProjectStates(preparedRequests);
            }
        }
    }

    /**
     * Handle a build completed event.
     *
     * @param event
     */
    protected void handleBuildCompleted(BuildCompletedEvent event)
    {
        BuildRequestHandler requestHandler = getRequestHandler(event);

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (requestHandler)
        {
            // if all requests are finished, return true, else return false.
            BuildResult result = event.getBuildResult();
            Project completedProject = result.getProject();

            ActivatedRequest requestToComplete = CollectionUtils.find(buildQueue.getActivatedRequests(),
                    new RequestsByMetaIdAndOwnerPredicate(requestHandler.getMetaBuildId(), completedProject)
            );

            List<RequestHolder> requestsToComplete = new LinkedList<RequestHolder>();
            requestsToComplete.add(requestToComplete);

            if (!result.succeeded())
            {
                requestsToComplete.addAll(CollectionUtils.filter(buildQueue.getQueuedRequests(), new RequestsByMetaIdPredicate(requestHandler.getMetaBuildId())));
            }

            internalCompleteRequests(requestHandler, requestsToComplete);
        }
    }

    private void internalCompleteRequests(BuildRequestHandler requestHandler, List<RequestHolder> completedRequests)
    {
        List<BuildRequestEvent> requestEvents = CollectionUtils.map(completedRequests, new ExtractRequestMapping());

        // a) lock the necessary projects before moving on.
        lockProjectStates(requestEvents);
        try
            {
                //noinspection SynchronizeOnNonFinalField
            synchronized (buildQueue)
            {
                // pause the activation since we may be making multiple related changes.  We do not
                // accidentally want to activate a request when we cancel its remaining dependency.
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

                for (BuildRequestEvent completedRequest : requestEvents)
                {
                    Project project = getProject(completedRequest);
                    if (!buildQueue.hasRequest(completedRequest.getOwner()) && !completedRequest.isPersonal())
                    {
                        if (project.isTransitionValid(Project.Transition.IDLE))
                        {
                            projectManager.makeStateTransition(project.getId(), Project.Transition.IDLE);
                        }
                    }
                }
                long metaBuildId = requestHandler.getMetaBuildId();
                if (buildQueue.getMetaBuildRequests(metaBuildId).size() == 0)
                {
                    handlers.remove(metaBuildId);
                }
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

    private void filterCanBuild(List<BuildRequestEvent> acceptedRequests, List<BuildRequestEvent> rejectedRequests)
    {
        for (BuildRequestEvent request : acceptedRequests)
        {
            if (!canBuild(request))
            {
                rejectedRequests.add(request);
            }
        }
        acceptedRequests.removeAll(rejectedRequests);
    }

    private Project getProject(BuildRequestEvent request)
    {
        return projectManager.getProject(request.getProjectConfig().getProjectId(), false);
    }

    private boolean canBuild(BuildRequestEvent request)
    {
        Project project = projectManager.getProject(request.getProjectConfig().getProjectId(), false);
        return canBuild(project,  request);
    }

    private boolean canBuild(Project project, BuildRequestEvent request)
    {
        return project.getState().acceptTrigger(request.isPersonal());
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
     * @param id    the id of the build request to be cancelled.
     *
     * @return true if the request was cancelled, false otherwise.
     */
    public boolean cancelRequest(long id)
    {
        BuildRequestHandler requestHandler = getRequestHandler(id);
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (requestHandler)
        {
            //noinspection SynchronizeOnNonFinalField
            synchronized (buildQueue)
            {
                List<RequestHolder> requests = buildQueue.getMetaBuildRequests(requestHandler.getMetaBuildId());
                List<RequestHolder> queuedRequests = CollectionUtils.filter(requests, new InstanceOfPredicate(QueuedRequest.class));
                internalCompleteRequests(requestHandler, queuedRequests);
                return queuedRequests.size() > 0;
            }
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
     *
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
