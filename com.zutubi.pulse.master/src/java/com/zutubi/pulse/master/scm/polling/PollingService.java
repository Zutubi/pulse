package com.zutubi.pulse.master.scm.polling;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.Pollable;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.project.events.ProjectStatusEvent;
import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.scm.ScmChangeEvent;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.scm.util.PredicateRequest;
import com.zutubi.pulse.master.scm.util.PredicateRequestQueue;
import com.zutubi.pulse.master.scm.util.PredicateRequestQueueListenerAdapter;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.servercore.ShutdownManager;
import com.zutubi.util.*;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * Polls the scms for changes.
 */
public class PollingService implements Stoppable
{
    private static final Logger LOG = Logger.getLogger(PollingService.class);

    private static final int DEFAULT_POLL_THREAD_COUNT = 10;
    private static final String PROPERTY_POLLING_THREAD_COUNT = "scm.polling.thread.count";

    private ProjectManager projectManager;
    private Scheduler scheduler;
    private EventManager eventManager;
    private ThreadFactory threadFactory;
    private ShutdownManager shutdownManager;
    private ObjectFactory objectFactory;

    private ScmManager scmManager;

    private ExecutorService executorService;
    private final Map<Long, Pair<Long, Revision>> waiting = Collections.synchronizedMap(new HashMap<Long, Pair<Long, Revision>>());
    private final Map<Long, Revision> latestRevisions = new HashMap<Long, Revision>();
    private final PredicateRequestQueue<Project> requestQueue;

    public PollingService()
    {
        requestQueue = new PredicateRequestQueue<Project>();
    }

    public void init()
    {
        int pollThreadCount = Integer.getInteger(PROPERTY_POLLING_THREAD_COUNT, DEFAULT_POLL_THREAD_COUNT);
        executorService = Executors.newFixedThreadPool(pollThreadCount, threadFactory);

        // initialise the latest built revision cache
        latestRevisions.putAll(projectManager.getLatestBuiltRevisions());

        try
        {
            scheduler.registerCallback(new NullaryProcedure()
            {
                public void run()
                {
                    checkForChanges();
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
        latestRevisions.remove(projectId);
        waiting.remove(projectId);
    }

    public void checkForChanges()
    {
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
        for (DependencyTree tree : treesToPoll)
        {
            for (Project project : tree.getProjectsToPoll())
            {
                PredicateRequest<Project> request = new PredicateRequest<Project>(project);
                request.add(objectFactory.buildBean(OneActivePollPerScmPredicate.class, new Class[]{PredicateRequestQueue.class}, new Object[]{requestQueue}));
                request.add(new InvertedPredicate<PredicateRequest<Project>>(objectFactory.buildBean(HasDependencyBeingPolledPredicate.class, new Class[]{PredicateRequestQueue.class}, new Object[]{requestQueue})));
                requestQueue.enqueue(request);
            }
        }

        requestListener.waitForProcessingToComplete();

        List<ScmChangeEvent> scmChanges = requestListener.getScmChanges();

        // publish the new scm change events and record the latest encountered revisions.
        for (ScmChangeEvent change : scmChanges)
        {
            eventManager.publish(change);
            latestRevisions.put(change.getProjectConfiguration().getProjectId(), change.getNewRevision());
        }
    }

    public void checkForChanges(Project project, List<ScmChangeEvent> changes)
    {
        ProjectConfiguration projectConfig = project.getConfig();
        Pollable pollable = (Pollable) projectConfig.getScm();
        long projectId = projectConfig.getProjectId();

        ScmClient client = null;

        try
        {
            long now = System.currentTimeMillis();

            eventManager.publish(new ProjectStatusEvent(this, projectConfig, "Polling SCM for changes..."));
            projectManager.updateLastPollTime(projectId, now);

            ScmContext context = createContext(projectConfig);
            client = createClient(projectConfig.getScm());

            // When was the last time that we checked?  If never, get the latest revision.  We do
            // this under the lock to protect from races with project destruction clearing the
            // latestRevisions cache.
            context.lock();
            Revision previous;
            try
            {
                previous = latestRevisions.get(projectId);
                if (previous == null)
                {
                    previous = client.getLatestRevision(context);
                    // slightly paranoid, but we can not rely on the scm implementations to behave as expected.
                    if (previous == null)
                    {
                        eventManager.publish(new ProjectStatusEvent(this, projectConfig, "Scm failed to return latest revision."));
                        return;
                    }
                    latestRevisions.put(projectId, previous);
                    eventManager.publish(new ProjectStatusEvent(this, projectConfig, "Retrieved initial revision: " + previous.getRevisionString() + " (took " + TimeStamps.getPrettyElapsed(System.currentTimeMillis() - now) + ")."));
                }
            }
            finally
            {
                context.unlock();
            }

            if (pollable.isQuietPeriodEnabled())
            {
                // are we waiting
                if (waiting.containsKey(projectId))
                {
                    long quietTime = waiting.get(projectId).first;
                    if (quietTime < System.currentTimeMillis())
                    {
                        Revision lastChange = waiting.get(projectId).second;
                        Revision latest = getLatestRevisionSince(lastChange, client, context);
                        if (latest != null)
                        {
                            // there has been a commit during the 'quiet period', lets reset the timer.
                            eventManager.publish(new ProjectStatusEvent(this, projectConfig, "Changes detected during quiet period, restarting wait..."));
                            waiting.put(projectId, new Pair<Long, Revision>(System.currentTimeMillis() + pollable.getQuietPeriod() * Constants.MINUTE, latest));
                        }
                        else
                        {
                            // there have been no commits during the 'quiet period', trigger a change.
                            eventManager.publish(new ProjectStatusEvent(this, projectConfig, "Quiet period completed without additional changes."));
                            changes.add(new ScmChangeEvent(projectConfig, latest, previous));
                            waiting.remove(projectId);
                        }
                    }
                    else
                    {
                        eventManager.publish(new ProjectStatusEvent(this, projectConfig, "Still within quiet period."));
                    }
                }
                else
                {
                    Revision latest = getLatestRevisionSince(previous, client, context);
                    if (latest != null)
                    {
                        if (pollable.getQuietPeriod() != 0)
                        {
                            eventManager.publish(new ProjectStatusEvent(this, projectConfig, "Changes detected, starting quiet period wait..."));
                            waiting.put(projectId, new Pair<Long, Revision>(System.currentTimeMillis() + pollable.getQuietPeriod() * Constants.MINUTE, latest));
                        }
                        else
                        {
                            changes.add(new ScmChangeEvent(projectConfig, latest, previous));
                        }
                    }
                }
            }
            else
            {
                Revision latest = getLatestRevisionSince(previous, client, context);
                if (latest != null)
                {
                    changes.add(new ScmChangeEvent(projectConfig, latest, previous));
                }
            }

            eventManager.publish(new ProjectStatusEvent(this, projectConfig, "SCM polling completed (took " + TimeStamps.getPrettyElapsed(System.currentTimeMillis() - now) + ")."));
        }
        catch (ScmException e)
        {
            eventManager.publish(new ProjectStatusEvent(this, projectConfig, "Error polling SCM: " + e.getMessage()));
            LOG.debug(e);

            if (e.isReinitialiseRequired())
            {
                projectManager.makeStateTransition(projectConfig.getProjectId(), Project.Transition.INITIALISE);
            }
        }
        finally
        {
            IOUtils.close(client);
        }
    }

    private ScmContext createContext(ProjectConfiguration projectConfiguration) throws ScmException
    {
        return scmManager.createContext(projectConfiguration);
    }

    private ScmClient createClient(ScmConfiguration config) throws ScmException
    {
        return scmManager.createClient(config);
    }

    private Revision getLatestRevisionSince(Revision revision, ScmClient client, ScmContext context) throws ScmException
    {
        // this assumes that getting the revision since revision x is more efficient than getting the latest revision.
        List<Revision> revisions = client.getRevisions(context, revision, null);
        if (revisions.size() > 0)
        {
            // get the latest revision.
            return revisions.get(revisions.size() - 1);
        }
        return null;
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

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
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
                            objectFactory.buildBean(IsReadyToPollPredicate.class, new Class[]{Long.TYPE}, new Object[]{System.currentTimeMillis()})
                    )
            ) != null;
        }
    }

    private class PollingRequestListener extends PredicateRequestQueueListenerAdapter<Project>
    {
        private final List<Future> futures;
        private final List<ScmChangeEvent> scmChanges;

        private PollingRequestListener()
        {
            futures = new LinkedList<Future>();
            scmChanges = Collections.synchronizedList(new LinkedList<ScmChangeEvent>());
        }

        @Override
        public boolean onActivation(final PredicateRequest<Project> request)
        {
            Future<?> future = executorService.submit(new Runnable()
            {
                public void run()
                {
                    checkForChanges(request.getData(), scmChanges);
                    requestQueue.complete(request);
                }
            });
            synchronized (futures)
            {
                futures.add(future);
            }
            return true;
        }

        public List<ScmChangeEvent> getScmChanges()
        {
            return scmChanges;
        }

        /**
         * This is a blocking call that waits until all of the requests in the queue have been
         * processed before returning.
         */
        public void waitForProcessingToComplete()
        {
            while (requestQueue.hasRequests())
            {
                try
                {
                    // Take a snapshot of the futures we have at the moment since this list
                    // will be changing so long as we have queued requests.
                    List<Future> copyOfFutures;
                    synchronized (futures)
                    {
                        copyOfFutures = new LinkedList<Future>(futures);
                    }

                    ConcurrentUtils.waitForTasks(copyOfFutures, LOG);

                    // Cleanup the futures list, removing those that we have finished waiting for.
                    synchronized (futures)
                    {
                        futures.removeAll(copyOfFutures);
                    }
                }
                catch (InterruptedException e)
                {
                    LOG.warning(e);
                }
            }
        }
    }
}
