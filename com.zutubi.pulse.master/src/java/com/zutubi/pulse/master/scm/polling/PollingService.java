package com.zutubi.pulse.master.scm.polling;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.scheduling.CallbackService;
import com.zutubi.pulse.master.scm.ScmChangeEvent;
import com.zutubi.pulse.master.scm.ScmClientUtils;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.servercore.ShutdownManager;
import com.zutubi.util.Constants;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.time.Clock;
import com.zutubi.util.time.SystemClock;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;

/**
 * Polls the scms for changes.
 */
public class PollingService implements Stoppable
{
    private static final Logger LOG = Logger.getLogger(PollingService.class);
    
    private static final String CALLBACK_NAME = "Polling";
    
    private static final int DEFAULT_POLL_THREAD_COUNT = 10;
    private static final String PROPERTY_POLLING_THREAD_COUNT = "scm.polling.thread.count";
    private static final int DEFAULT_ACTIVE_POLLS_PER_SCM = 5;
    private static final String PROPERTY_ACTIVE_POLLS_PER_SCM = "scm.polling.active.polls.per.scm";

    private ProjectManager projectManager;
    private BuildManager buildManager;
    private CallbackService callbackService;
    private EventManager eventManager;
    private ThreadFactory threadFactory;
    private ShutdownManager shutdownManager;
    private ObjectFactory objectFactory;

    private ScmManager scmManager;

    private Clock clock = new SystemClock();
    private int activePollsPerScmLimit = Integer.getInteger(PROPERTY_ACTIVE_POLLS_PER_SCM, DEFAULT_ACTIVE_POLLS_PER_SCM);
    private ExecutorService executorService;
    private CompletionService<ProjectPollingState> completionService;
    private final Map<Long, ProjectPollingState> states = Collections.synchronizedMap(new HashMap<Long, ProjectPollingState>());
    private final PollingQueue requestQueue;
    private final List<Long> clearCacheForProjects = new LinkedList<Long>();
    private final Map<Long, String> projectUidCache = new HashMap<Long, String>();
    
    private LimitActivePollsPerScmPredicate limitActivePollsPerScmPredicate;
    private HasNoDependencyBeingPolledPredicate noDependencyBeingPolledPredicate;

    public PollingService()
    {
        requestQueue = new PollingQueue();
    }

    public void init()
    {
        limitActivePollsPerScmPredicate = objectFactory.buildBean(LimitActivePollsPerScmPredicate.class, activePollsPerScmLimit, requestQueue, projectUidCache);
        noDependencyBeingPolledPredicate = objectFactory.buildBean(HasNoDependencyBeingPolledPredicate.class, requestQueue);

        int pollThreadCount = Integer.getInteger(PROPERTY_POLLING_THREAD_COUNT, DEFAULT_POLL_THREAD_COUNT);
        executorService = Executors.newFixedThreadPool(pollThreadCount, threadFactory);
        completionService = new ExecutorCompletionService<ProjectPollingState>(executorService);

        for (Map.Entry<Long, Revision> entry : projectManager.getLatestBuiltRevisions().entrySet())
        {
            states.put(entry.getKey(), new ProjectPollingState(entry.getKey(), entry.getValue()));
        }

        try
        {
            callbackService.registerCallback(CALLBACK_NAME, new Runnable()
            {
                public void run()
                {
                    pollForChanges();
                }
            }, Constants.MINUTE);
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }

        shutdownManager.addStoppable(this);
    }

    public void stop(boolean force)
    {
        if (executorService != null)
        {
            executorService.shutdownNow();
        }
    }

    public void clearCache(long projectId)
    {
        synchronized (clearCacheForProjects)
        {
            clearCacheForProjects.add(projectId);
        }
    }

    public void pollForChanges()
    {
        LOG.finest("Begin poll cycle");
        clearCachesIfNecessary();

        PollingRequestListener requestListener = new PollingRequestListener();
        requestQueue.setListener(requestListener);

        queuePollRequests(generateReadyDependencyTrees());

        LOG.finest("Waiting for polling to complete.");
        List<ProjectPollingState> newStates = requestListener.waitForProcessingToComplete();
        LOG.finest("Polling completed with states:");
        for (ProjectPollingState newState : newStates)
        {
            long projectId = newState.getProjectId();
            ProjectPollingState previousState = states.get(projectId);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("    " + newState + (previousState == null ? "" : " (previously: " + previousState + ")"));
            }

            if (previousState != null && newState.changeDetectedSince(previousState))
            {
                ProjectConfiguration projectConfig = projectManager.getProjectConfig(projectId, true);
                if (projectConfig != null)
                {
                    LOG.finest("        raising change event");
                    eventManager.publish(new ScmChangeEvent(projectConfig, newState.getLatestRevision(), previousState.getLatestRevision()));
                }
            }

            states.put(projectId, newState);
        }
        LOG.finest("End polling states.");

        clearCachesIfNecessary();
        LOG.finest("End poll cycle");
    }

    private void clearCachesIfNecessary()
    {
        synchronized (clearCacheForProjects)
        {
            for (long projectId : clearCacheForProjects)
            {
                states.remove(projectId);
                projectUidCache.remove(projectId);
            }
            clearCacheForProjects.clear();
        }
    }

    private Iterable<DependencyTree> generateReadyDependencyTrees()
    {
        Set<Project> identified = new HashSet<Project>();

        List<DependencyTree> trees = new LinkedList<DependencyTree>();
        List<Project> projects = projectManager.getProjects(false);
        for (Project project : projects)
        {
            if (identified.contains(project))
            {
                continue;
            }
            DependencyTree newTree = new DependencyTree();
            trees.add(newTree);

            addDependenciesToSet(project, newTree);

            identified.addAll(newTree.getProjects());
        }

        Iterable<DependencyTree> readyTrees = Iterables.filter(trees, new Predicate<DependencyTree>()
        {
            public boolean apply(DependencyTree dependencyTree)
            {
                return dependencyTree.isReadyToPoll();
            }
        });

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Ready dependency trees:");
            for (DependencyTree tree: readyTrees)
            {
                LOG.finest("    " + tree.toString());
            }
            LOG.finest("End ready dependency trees.");
        }
        return readyTrees;
    }

    private void addDependenciesToSet(Project project, DependencyTree tree)
    {
        // Work our way through the dependency tree looking up and downstream of every node.
        // The first time we encounter a node, we look up and downstream of it. Any other time
        // we encounter it, we ignore it and stop following that path.  We pick up all of the
        // necessary nodes because we always look up and downstream of a node the first time we
        // find it.
        if (!tree.contains(project))
        {
            tree.add(project);

            // upstream.
            ProjectConfiguration config = project.getConfig();
            for (DependencyConfiguration dependency : config.getDependencies().getDependencies())
            {
                addDependenciesToSet(projectManager.getProject(dependency.getProject().getProjectId(), false), tree);
            }

            // downstream.
            List<ProjectConfiguration> downstreamProjects = projectManager.getDownstreamDependencies(project.getConfig());
            for (ProjectConfiguration downstreamProject : downstreamProjects)
            {
                addDependenciesToSet(projectManager.getProject(downstreamProject.getProjectId(), false), tree);
            }
        }
    }

    private void queuePollRequests(Iterable<DependencyTree> treesToPoll)
    {
        // go through the dependency trees generating the poll requests.
        List<PollingRequest> requests = new LinkedList<PollingRequest>();
        for (DependencyTree tree : treesToPoll)
        {
            for (Project project : tree.getProjectsToPoll())
            {
                // So that we do not make potentially slow calls during the predicate processing,
                // we load the scm uids into a cache now.  This cache will be complete with the
                // necessary uids before the queuing (and subsequent reading) occurs.
                loadProjectScmUidIntoCache(project);
                ProjectPollingState state = states.get(project.getId());
                if (state == null)
                {
                    // In the reinitialise case we may have a previous build revision to base off
                    // (CIB-2970).  We need to add this to the states map so it is there when the
                    // poll completes (to be compared against what the poll discovered).
                    state = new ProjectPollingState(project.getId(), buildManager.getPreviousRevision(project));
                    states.put(project.getId(), state);
                }

                PollingRequest request = new PollingRequest(project, state, limitActivePollsPerScmPredicate, noDependencyBeingPolledPredicate);
                requests.add(request);
            }
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Queuing poll requests: " + requests);
        }

        requestQueue.enqueue(requests.toArray(new PollingRequest[requests.size()]));
    }

    public void loadProjectScmUidIntoCache(Project project)
    {
        final long key = project.getId();

        if (!projectUidCache.containsKey(key))
        {
            try
            {
                ScmClientUtils.withScmClient(project.getConfig(), project.getState(), scmManager, new ScmClientUtils.ScmContextualAction<Object>()
                {
                    public Object process(ScmClient client, ScmContext context) throws ScmException
                    {
                        String projectUid = client.getUid(context);
                        if (projectUid != null)
                        {
                            projectUidCache.put(key, projectUid);
                        }

                        return null;
                    }
                });
            }
            catch (ScmException e)
            {
                // noop.
            }
        }
    }

    public void setCallbackService(CallbackService callbackService)
    {
        this.callbackService = callbackService;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    public void setShutdownManager(ShutdownManager shutdownManager)
    {
        this.shutdownManager = shutdownManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setClock(Clock clock)
    {
        this.clock = clock;
    }

    public void setActivePollsPerScmLimit(int activePollsPerScmLimit)
    {
        this.activePollsPerScmLimit = activePollsPerScmLimit;
    }

    /**
     * A value object that holds a set of projects that are related via
     * dependencies.  This object is also able to answer simple questions
     * that relate to the set of projects as a group.
     */
    private class DependencyTree
    {
        private Set<Project> projects = new TreeSet<Project>(new Comparator<Project>()
        {
            public int compare(Project project1, Project project2)
            {
                // There is guaranteed to be a dependency relationship one way
                // or the other, so we never return 0 for different projects.
                if (project1.getConfig().isDependentOn(project2.getConfig()))
                {
                    return 1;
                }
                else if (project1.equals(project2))
                {
                    return 0;
                }
                else
                {
                    return -1;
                }
            }
        });

        public void add(Project project)
        {
            projects.add(project);
        }

        public boolean contains(Project project)
        {
            return projects.contains(project);
        }

        public Collection<? extends Project> getProjects()
        {
            return projects;
        }

        public Iterable<? extends Project> getProjectsToPoll()
        {
            return Iterables.filter(projects,
                    Predicates.and(
                            objectFactory.buildBean(IsInitialisedPredicate.class),
                            objectFactory.buildBean(IsMonitorablePredicate.class)
                    )
            );
        }

        public boolean isReadyToPoll()
        {
            return Iterables.any(projects,
                    Predicates.and(
                            objectFactory.buildBean(IsInitialisedPredicate.class),
                            objectFactory.buildBean(IsMonitorablePredicate.class),
                            objectFactory.buildBean(IsReadyToPollPredicate.class)
                    )
            );
        }

        @Override
        public String toString()
        {
            return projects.toString();
        }
    }

    /**
     * A simple implementation of the PredicateRequestQueueListener interface that
     * triggers a check for changes when a request is activated.
     *
     * @see PollingService#pollForChanges()
     */
    private class PollingRequestListener implements PollingActivationListener
    {
        public void onActivation(final PollingRequest request)
        {
            final ProjectPoll poll = objectFactory.buildBean(ProjectPoll.class, request.getProject(), request.getState(), clock);
            completionService.submit(new Callable<ProjectPollingState>()
            {
                public ProjectPollingState call() throws Exception
                {
                    try
                    {
                        return poll.call();
                    }
                    finally
                    {
                        requestQueue.complete(request);
                    }
                }
            });
        }

        /**
         * This is a blocking call that waits until all of the requests in the queue have been
         * processed before returning.
         *
         * @return the new polling states for all processed projects
         */
        public List<ProjectPollingState> waitForProcessingToComplete()
        {
            final List<ProjectPollingState> newStates = new LinkedList<ProjectPollingState>();

            while (requestQueue.hasRequests())
            {
                try
                {
                    newStates.add(completionService.take().get());
                }
                catch (InterruptedException e)
                {
                    LOG.warning(e);
                }
                catch (ExecutionException e)
                {
                    LOG.severe(e);
                }
            }

            return newStates;
        }
    }
}
