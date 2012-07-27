package com.zutubi.pulse.master.scm.polling;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.scheduling.CallbackService;
import com.zutubi.pulse.master.scm.ScmChangeEvent;
import com.zutubi.pulse.master.scm.ScmClientUtils;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.servercore.ShutdownManager;
import com.zutubi.util.*;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.time.Clock;
import com.zutubi.util.time.SystemClock;

import java.util.*;
import java.util.concurrent.*;

/**
 * Polls the scms for changes.
 */
public class PollingService implements Stoppable
{
    private static final Logger LOG = Logger.getLogger(PollingService.class);
    private static final String CALLBACK_NAME = "Polling";
    private static final int DEFAULT_POLL_THREAD_COUNT = 10;
    private static final String PROPERTY_POLLING_THREAD_COUNT = "scm.polling.thread.count";

    private ProjectManager projectManager;
    private CallbackService callbackService;
    private EventManager eventManager;
    private ThreadFactory threadFactory;
    private ShutdownManager shutdownManager;
    private ObjectFactory objectFactory;

    private ScmManager scmManager;

    private Clock clock = new SystemClock();
    private ExecutorService executorService;
    private CompletionService<ProjectPollingState> completionService;
    private final Map<Long, ProjectPollingState> states = Collections.synchronizedMap(new HashMap<Long, ProjectPollingState>());
    private final PollingQueue requestQueue;
    private final List<Long> clearCacheForProjects = new LinkedList<Long>();
    private final Map<Long, String> projectUidCache = new HashMap<Long, String>();

    public PollingService()
    {
        requestQueue = new PollingQueue();
    }

    public void init()
    {
        int pollThreadCount = Integer.getInteger(PROPERTY_POLLING_THREAD_COUNT, DEFAULT_POLL_THREAD_COUNT);
        executorService = Executors.newFixedThreadPool(pollThreadCount, threadFactory);
        completionService = new ExecutorCompletionService<ProjectPollingState>(executorService);

        for (Map.Entry<Long, Revision> entry : projectManager.getLatestBuiltRevisions().entrySet())
        {
            states.put(entry.getKey(), new ProjectPollingState(entry.getKey(), entry.getValue()));
        }

        try
        {
            callbackService.registerCallback(CALLBACK_NAME, new NullaryProcedure()
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

    public void pollForChanges()
    {
        clearCachesIfNecessary();

        List<DependencyTree> allDependencyTrees = generateDependencyTrees();

        // filter those dependency trees that are not ready for polling.
        List<DependencyTree> treesToPoll = CollectionUtils.filter(allDependencyTrees, new Predicate<DependencyTree>()
        {
            public boolean satisfied(DependencyTree dependencyTree)
            {
                return dependencyTree.isReadyToPoll();
            }
        });

        PollingRequestListener requestListener = new PollingRequestListener();
        requestQueue.setListener(requestListener);

        // go through the dependency trees generating the poll requests.
        List<PollingRequest> requests = new LinkedList<PollingRequest>();
        for (DependencyTree tree : treesToPoll)
        {
            for (Project project : tree.getProjectsToPoll())
            {
                // so that we do not make potentially slow calls during the predicate processing, we load
                // the scm uids into a cache now.  This cache will be complete with the necessary uids before
                // the enqueuing (and subsequent reading) occurs.
                loadProjectScmUidIntoCache(project);
                ProjectPollingState state = states.get(project.getId());
                if (state == null)
                {
                    state = new ProjectPollingState(project.getId(), null);
                }

                OneActivePollPerScmPredicate oneActive = objectFactory.buildBean(OneActivePollPerScmPredicate.class, new Class[]{PollingQueue.class, Map.class}, new Object[]{requestQueue, projectUidCache});
                HasNoDependencyBeingPolledPredicate noDepsPolling = objectFactory.buildBean(HasNoDependencyBeingPolledPredicate.class, new Class[]{PollingQueue.class}, new Object[]{requestQueue});
                PollingRequest request = new PollingRequest(project, state, oneActive, noDepsPolling);
                requests.add(request);
            }
        }
        requestQueue.enqueue(requests.toArray(new PollingRequest[requests.size()]));

        List<ProjectPollingState> newStates = requestListener.waitForProcessingToComplete();
        for (ProjectPollingState newState : newStates)
        {
            long projectId = newState.getProjectId();
            ProjectPollingState previousState = states.get(projectId);
            if (previousState != null && newState.changeDetectedSince(previousState))
            {
                ProjectConfiguration projectConfig = projectManager.getProjectConfig(projectId, true);
                if (projectConfig != null)
                {
                    eventManager.publish(new ScmChangeEvent(projectConfig, newState.getLatestRevision(), previousState.getLatestRevision()));
                }
            }

            states.put(projectId, newState);
        }

        clearCachesIfNecessary();
    }

    private List<DependencyTree> generateDependencyTrees()
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

        return trees;
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

    public void setCallbackService(CallbackService callbackService)
    {
        this.callbackService = callbackService;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
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
            this.projects.add(project);
        }

        public boolean contains(Project project)
        {
            return this.projects.contains(project);
        }

        public Collection<? extends Project> getProjects()
        {
            return projects;
        }

        public Collection<? extends Project> getProjectsToPoll()
        {
            return CollectionUtils.filter(projects,
                    new ConjunctivePredicate<Project>(
                            objectFactory.buildBean(IsInitialisedPredicate.class),
                            objectFactory.buildBean(IsMonitorablePredicate.class)
                    )
            );
        }

        public boolean isReadyToPoll()
        {
            return CollectionUtils.find(projects,
                    new ConjunctivePredicate<Project>(
                            objectFactory.buildBean(IsInitialisedPredicate.class),
                            objectFactory.buildBean(IsMonitorablePredicate.class),
                            objectFactory.buildBean(IsReadyToPollPredicate.class)
                    )
            ) != null;
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
            final ProjectPoll poll = objectFactory.buildBean(ProjectPoll.class, new Class[] {Project.class, ProjectPollingState.class, Clock.class}, new Object[] {request.getProject(), request.getState(), clock});
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
                    ProjectPollingState newState = completionService.take().get();
                    newStates.add(newState);
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
