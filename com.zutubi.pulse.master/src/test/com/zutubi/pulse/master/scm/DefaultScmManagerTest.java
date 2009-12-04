package com.zutubi.pulse.master.scm;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.EventManager;
import com.zutubi.events.RecordingEventListener;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.PollableScmConfiguration;
import static com.zutubi.pulse.core.test.TestUtils.waitForCondition;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.security.PulseThreadFactory;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.servercore.ShutdownManager;
import com.zutubi.pulse.servercore.events.system.SystemStartedEvent;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.Condition;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class DefaultScmManagerTest extends PulseTestCase
{
    private static final long TIMEOUT = 2000;

    private ProjectManager projectManager;
    private ScmClientFactory scmClientFactory;
    private ThreadFactory threadFactory;
    private LinkedList<Project> projects;
    private ScmManagerHandle scmManagerHandle;
    private RecordingEventListener events;

    protected void setUp() throws Exception
    {
        super.setUp();

        ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);
        stub(configurationProvider.get(GlobalConfiguration.class)).toReturn(new GlobalConfiguration());

        projects = new LinkedList<Project>();
        projectManager = mock(ProjectManager.class);
        stub(projectManager.getProjects(false)).toReturn(projects);

        scmClientFactory = mock(ScmClientFactory.class);

        ScmContextFactory scmContextFactory = mock(ScmContextFactory.class);
        stub(scmContextFactory.createContext((ProjectConfiguration) anyObject())).toAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return new ScmContextImpl();
            }
        });

        ShutdownManager shutdownManager = mock(ShutdownManager.class);
        Scheduler scheduler = mock(Scheduler.class);
        EventManager eventManager = new DefaultEventManager();

        threadFactory = new PulseThreadFactory();

        DefaultScmManager scmManager = new DefaultScmManager();
        scmManager.setEventManager(eventManager);
        scmManager.setConfigurationProvider(configurationProvider);
        scmManager.setProjectManager(projectManager);
        scmManager.setScmClientFactory(scmClientFactory);
        scmManager.setScmContextFactory(scmContextFactory);
        scmManager.setShutdownManager(shutdownManager);
        scmManager.setThreadFactory(threadFactory);
        scmManager.setScheduler(scheduler);
        scmManager.init();

        // trigger the scmManager init.
        eventManager.publish(new SystemStartedEvent(this));

        events = new RecordingEventListener();
        eventManager.register(events);

        scmManagerHandle = new ScmManagerHandle(scmManager);
    }

    @Override
    protected void tearDown() throws Exception
    {
        scmManagerHandle.stop();

        super.tearDown();
    }

    public void testDoNotPollWhenMonitorIsFalse() throws ScmException, InterruptedException
    {
        ScmClient client = createProject(1, false, true);

        scmManagerHandle.pollAndWait();

        // ensure that project 1 was not polled
        verify(client, times(0)).getLatestRevision((ScmContext) anyObject());
    }

    public void testPollWhenMonitorIsTrue() throws ScmException, InterruptedException
    {
        ScmClient client = createProject(1, true, true);
        stub(client.getLatestRevision((ScmContext) anyObject())).toReturn(new Revision("1"));

        scmManagerHandle.pollAndWait();

        // ensure that project 1 was polled
        verify(client, times(1)).getLatestRevision((ScmContext) anyObject());
    }

    public void testPollingIsParallelForDifferentScmServers() throws ScmException, InterruptedException
    {
        ScmServer serverA = new ScmServer("a", true);
        ScmServer serverB = new ScmServer("b", true);

        ScmClient clientA = stubClientWithServer(createProject(1, true, true), serverA);
        ScmClient clientB = stubClientWithServer(createProject(2, true, true), serverB);

        scmManagerHandle.startPolling();

        // ensure that polling on two different servers is active at the same time.
        assertTrue(serverA.waitForInProgress(1));
        assertTrue(serverB.waitForInProgress(1));

        serverA.releaseProcess();
        serverB.releaseProcess();

        assertTrue(scmManagerHandle.waitForPollingComplete());

        verify(clientA, times(1)).getLatestRevision((ScmContext) anyObject());
        verify(clientB, times(1)).getLatestRevision((ScmContext) anyObject());
        assertTrue(scmManagerHandle.isPollingComplete());
    }

    public void testPollingIsSequentialForSingleScmServer() throws ScmException, InterruptedException
    {
        ScmServer serverA = new ScmServer("a", true);

        ScmClient clientA = stubClientWithServer(createProject(1, true, true), serverA);
        ScmClient clientB = stubClientWithServer(createProject(2, true, true), serverA);

        scmManagerHandle.startPolling();

        assertTrue(serverA.waitForInProgress(1));
        assertFalse(serverA.waitForInProgress(2));
        verify(clientA, times(1)).getLatestRevision((ScmContext) anyObject());
        verify(clientB, times(0)).getLatestRevision((ScmContext) anyObject());

        serverA.releaseProcess();

        assertFalse(scmManagerHandle.isPollingComplete());

        assertTrue(serverA.waitForInProgress(2));
        verify(clientB, times(1)).getLatestRevision((ScmContext) anyObject());
        serverA.releaseProcess();

        assertTrue(scmManagerHandle.waitForPollingComplete());
        assertTrue(scmManagerHandle.isPollingComplete());
    }

    public void testScmChangeEventDetails() throws ScmException, InterruptedException
    {
        ScmServer serverA = new ScmServer("a");
        stubClientWithServer(createProject(1, true, true), serverA);

        scmManagerHandle.pollAndWait();
        assertEquals(0, events.getReceivedCount(ScmChangeEvent.class));

        scmManagerHandle.pollAndWait();
        List<ScmChangeEvent> changeEvents = events.getEventsReceived(ScmChangeEvent.class);
        assertEquals(1, changeEvents.size());
        ScmChangeEvent change = changeEvents.get(0);
        assertEquals(new Revision(0), change.getPreviousRevision());
        assertEquals(new Revision(1), change.getNewRevision());
    }

    private ScmClient stubClientWithServer(final ScmClient client, final ScmServer server) throws ScmException
    {
        stub(client.getUid()).toAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return server.getUid();
            }
        });
        stub(client.getLatestRevision((ScmContext) anyObject())).toAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return server.getLatestRevision();
            }
        });
        stub(client.getRevisions((ScmContext) anyObject(), (Revision) anyObject(), (Revision) anyObject())).toAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Revision from = (Revision) invocationOnMock.getArguments()[1];
                Revision to = (Revision) invocationOnMock.getArguments()[2];
                return server.getRevisions(from, to);
            }
        });
        return client;
    }

    private ScmClient createProject(long id, boolean monitor, boolean initialised) throws ScmException
    {
        PollableScmConfiguration scm = mock(PollableScmConfiguration.class);
        stub(scm.isMonitor()).toReturn(monitor);

        ScmClient client = mock(ScmClient.class);
        stub(scmClientFactory.createClient(scm)).toReturn(client);

        ProjectConfiguration config = new ProjectConfiguration();
        config.setProjectId(id);
        config.setName(Long.toString(id));
        config.setScm(scm);

        Project project = mock(Project.class);
        stub(project.getId()).toReturn(id);
        stub(project.getConfig()).toReturn(config);
        stub(project.isInitialised()).toReturn(initialised);

        projects.addLast(project);
        stub(projectManager.getProject(id, false)).toReturn(project);

        return client;
    }

    /**
     * A wrapper around the scm manager object that executes the scm managers polling
     * on a separate thread and allows us to query the status of the polling.
     */
    private class ScmManagerHandle
    {
        private ScmManager scmManager;
        private ExecutorService executor;
        private Future result;

        private ScmManagerHandle(ScmManager scmManager)
        {
            this.scmManager = scmManager;
        }

        public void startPolling() throws InterruptedException
        {
            executor = Executors.newFixedThreadPool(1, threadFactory);
            result = executor.submit(new Runnable()
            {
                public void run()
                {
                    scmManager.pollActiveScms();
                }
            });
        }

        public void pollAndWait() throws InterruptedException
        {
            startPolling();
            waitForPollingComplete();
        }

        private boolean isPollingComplete()
        {
            return result.isDone();
        }

        private boolean waitForPollingComplete()
        {
            try
            {
                result.get();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return result.isDone();
        }

        public void stop()
        {
            executor.shutdownNow();
        }
    }

    /**
     * A test scm server implementation.
     * <p/>
     * It implements a get latest revision method that will block until manually
     * released.  If an attempt is made to get the latest revision while another
     * request is already in progress, the attempt will be aborted.
     */
    private class ScmServer
    {
        private String uid;
        private boolean blocking = false;
        private Semaphore entrySemaphore;
        private Semaphore processSemaphore;

        private long nextRevision = 0;

        private int requestId = 0;

        private ScmServer(String uid)
        {
            this(uid, false);
        }

        private ScmServer(String uid, boolean blocking)
        {
            this.uid = uid;
            this.blocking = blocking;
            this.entrySemaphore = new Semaphore(1);
            this.processSemaphore = new Semaphore(0);
        }

        public String getUid()
        {
            return uid;
        }

        public Revision getLatestRevision() throws InterruptedException
        {
            if (!entrySemaphore.tryAcquire())
            {
                throw new RuntimeException("Request already in progress.");
            }
            try
            {
                requestId++;
                if (blocking)
                {
                    processSemaphore.acquire();
                }

                return new Revision(nextRevision());
            }
            finally
            {
                entrySemaphore.release();
            }
        }

        public boolean isInProgress(int requestId)
        {
            return entrySemaphore.availablePermits() == 0 && this.requestId == requestId;
        }

        public boolean waitForInProgress(final int requestId)
        {
            try
            {
                waitForCondition(new Condition()
                {
                    public boolean satisfied()
                    {
                        return isInProgress(requestId);
                    }
                }, TIMEOUT, "");

                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        }

        public void releaseProcess()
        {
            if (!blocking)
            {
                throw new RuntimeException("No need to release a non-blocking server.");
            }
            processSemaphore.release();
        }

        private long nextRevision()
        {
            return nextRevision++;
        }

        public List<Revision> getRevisions(Revision from, Revision to)
        {
            List<Revision> revisions = new LinkedList<Revision>();
            for (long i = Long.valueOf(from.getRevisionString()) + 1;i <= nextRevision; i++)
            {
                revisions.add(new Revision(i));
            }
            return revisions;
        }
    }
}
