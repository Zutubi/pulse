package com.zutubi.pulse.model;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.BuildCompletedEvent;
import com.zutubi.pulse.model.persistence.BuildResultDao;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.pulse.scheduling.SimpleTrigger;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.scheduling.tasks.CleanupBuilds;
import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.util.logging.Logger;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 *
 */
public class CleanupManager
{
    private static final String CLEANUP_NAME = "cleanup";
    private static final String CLEANUP_GROUP = "services";

    private static final long CLEANUP_FREQUENCY = Constants.HOUR;

    private static final Logger LOG = Logger.getLogger(CleanupManager.class);

    private final EventListener eventListener = new CleanupCallback();

    private EventManager eventManager;
    private Scheduler scheduler;
    private BuildManager buildManager;
    private ProjectManager projectManager;
    private BuildResultDao buildResultDao;

    private LinkedBlockingQueue<CleanupRequest> queue = new LinkedBlockingQueue<CleanupRequest>();
    private Lock executingRequestLock = new ReentrantLock();
    private CleanupRequest executingRequest = null;

    /**
     * Initialise the cleanup manager, registering event listeners and scheduling callbacks.
     */
    public void init()
    {
        // register for events.
        eventManager.register(eventListener);

        // register for scheduled callbacks.
        Trigger trigger = scheduler.getTrigger(CLEANUP_NAME, CLEANUP_GROUP);
        if (trigger == null)
        {
            // initialise the trigger.
            trigger = new SimpleTrigger(CLEANUP_NAME, CLEANUP_GROUP, CLEANUP_FREQUENCY);
            trigger.setTaskClass(CleanupBuilds.class);

            try
            {
                scheduler.schedule(trigger);
            }
            catch (SchedulingException e)
            {
                LOG.severe(e);
            }
        }

        Thread cleanupThread = new Thread(new Runnable()
        {
            @SuppressWarnings({"InfiniteLoopStatement"})
            public void run()
            {
                while(true)
                {
                    try
                    {
                        CleanupRequest request = queue.take();

                        executingRequestLock.lock();
                        executingRequest = request;
                        executingRequestLock.unlock();

                        try
                        {
                            request.cleanup();
                        }
                        catch(Exception e)
                        {
                            LOG.severe(e);
                        }
                        finally
                        {
                            executingRequestLock.lock();
                            executingRequest = null;
                            executingRequestLock.unlock();
                        }
                    }
                    catch (InterruptedException e)
                    {
                        LOG.warning(e);
                    }
                }
            }
        }, "Build Cleanup Service");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    public void cleanupBuilds()
    {
        // Lookup project cleanup info, query for old builds, cleanup where necessary
        List<Project> projects = projectManager.getAllProjects();
        for (Project project : projects)
        {
            cleanupBuilds(project);
        }
    }

    /**
     * Returns true if a cleanup is being run for the specified project, false otherwise.
     *
     * @param project being queried.
     *
     * @return true iff a cleanup is in progress.
     */
    public boolean isCleanupInProgress(Project project)
    {
        // We get a weakly-consistent iterator: safe even if there are
        // concurrent modifications.
        for(CleanupRequest request: queue)
        {
            if(request.isForProject(project))
            {
                return true;
            }
        }

        executingRequestLock.lock();
        try
        {
            return executingRequest != null && executingRequest.isForProject(project);
        }
        finally
        {
            executingRequestLock.unlock();
        }
    }

    /**
     * Execute the configured cleanup rules for the specified project.
     *
     * @param project   the project to be cleaned up.
     */
    private void cleanupBuilds(Project project)
    {
        List<CleanupRule> rules = project.getCleanupRules();
        for (CleanupRule rule : rules)
        {
            cleanupBuilds(project, rule);
        }
     }

    public void cleanupBuilds(Project project, CleanupRule rule)
    {
        ProjectCleanupRequest request = new ProjectCleanupRequest(project, rule);

        // Slight race may lead to duplicates in the queue, but that does not
        // really matter.
        if (!queue.contains(request))
        {
            queue.add(request);
        }
    }

    public void cleanupBuilds(User user)
    {
        PersonalCleanupRequest request = new PersonalCleanupRequest(user);

        // Slight race may lead to duplicates in the queue, but that does not
        // really matter.
        if (!queue.contains(request))
        {
            queue.add(request);
        }
    }

    public void setBuildResultDao(BuildResultDao buildResultDao)
    {
        this.buildResultDao = buildResultDao;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    /**
     * Listen for build completed events, triggering each completed builds projects
     * cleanup routines.
     */
    private class CleanupCallback implements EventListener
    {
        public void handleEvent(Event evt)
        {
            BuildCompletedEvent completedEvent = (BuildCompletedEvent) evt;
            BuildResult result = completedEvent.getResult();

            if(result.isPersonal())
            {
                cleanupBuilds(result.getUser());
            }
            else
            {
                cleanupBuilds(result.getProject());
            }
        }

        public Class[] getHandledEvents()
        {
            return new Class[]{BuildCompletedEvent.class};
        }
    }

    /**
     * Simple interface for request to perform a cleanup.
     */
    private interface CleanupRequest
    {
        void cleanup();
        boolean isForProject(Project p);
    }

    /**
     * A request to execute a single cleanup rule for a project.
     */
    private class ProjectCleanupRequest implements CleanupRequest
    {
        private Project project;
        private CleanupRule rule;

        public ProjectCleanupRequest(Project project, CleanupRule rule)
        {
            this.project = project;
            this.rule = rule;
        }

        public void cleanup()
        {
            List<BuildResult> oldBuilds = rule.getMatchingResults(project, buildResultDao);

            for (BuildResult build : oldBuilds)
            {
                if (rule.getWorkDirOnly())
                {
                    buildManager.cleanupWork(build);
                }
                else
                {
                    buildManager.cleanupResult(build, true);
                }
            }
        }

        public boolean isForProject(Project p)
        {
            return p.equals(project);
        }

        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }

            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            ProjectCleanupRequest that = (ProjectCleanupRequest) o;
            if (!project.equals(that.project))
            {
                return false;
            }

            return rule.equals(that.rule);
        }

        public int hashCode()
        {
            int result;
            result = project.hashCode();
            result = 31 * result + rule.hashCode();
            return result;
        }
    }

    /**
     * A request to cleanup personal builds for some user.
     */
    private class PersonalCleanupRequest implements CleanupRequest
    {
        private User user;

        public PersonalCleanupRequest(User user)
        {
            this.user = user;
        }

        public void cleanup()
        {
            List<BuildResult> results = buildResultDao.getOldestCompletedBuilds(user, user.getMyBuildsCount());
            for(BuildResult result: results)
            {
                buildManager.cleanupResult(result, true);
            }
        }

        public boolean isForProject(Project p)
        {
            return false;
        }

        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }

            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            PersonalCleanupRequest that = (PersonalCleanupRequest) o;
            return user.equals(that.user);
        }

        public int hashCode()
        {
            return user.hashCode();
        }
    }
}
