package com.zutubi.pulse.master.scm.polling;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.EventManager;
import com.zutubi.events.RecordingEventListener;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.PollableScmConfiguration;
import static com.zutubi.pulse.core.test.TestUtils.waitForCondition;
import com.zutubi.pulse.master.model.Project;
import static com.zutubi.pulse.master.model.Project.State.INITIAL;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.project.events.ProjectStatusEvent;
import com.zutubi.pulse.master.scheduling.CallbackService;
import com.zutubi.pulse.master.scm.ScmChangeEvent;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.security.PulseThreadFactory;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.servercore.ShutdownManager;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.*;
import com.zutubi.util.bean.WiringObjectFactory;
import com.zutubi.util.junit.ZutubiTestCase;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class PollingServiceTest extends ZutubiTestCase
{
    private static final Messages I18N = Messages.getInstance(PollingService.class);

    private EventManager eventManager;
    private ThreadFactory threadFactory;
    private ShutdownManager shutdownManager;
    private CallbackService callbackService;
    private ProjectManager projectManager;
    private WiringObjectFactory objectFactory;
    private RecordingEventListener eventListener;
    private ConfigurationProvider configurationProvider;
    private GlobalConfiguration globalConfiguration;
    private PollingService service;

    private TestClock clock;
    private PollingServiceHandle serviceHandle;
    private ProjectTestSupport testSupport;
    private ScmManager scmManager;

    // Keep track of the various mocks we generate so that we are able to
    // run the necessary asserts.
    private Map<Project, ScmServer> scmServerByProject;
    private Map<Project, ScmClient> scmClientsByProject;
    private Map<Long, Revision> latestBuildRevisions;

    /**
     * Used to initialise any scm servers that are not explicitly created by the test case.
     */
    private boolean blockingScmServers;

    /**
     * Used to initialise any projects with an initial latest revision.
     */
    private Revision latestProjectRevision;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        scmServerByProject = new HashMap<Project, ScmServer>();
        scmClientsByProject = new HashMap<Project, ScmClient>();
        latestBuildRevisions = new HashMap<Long, Revision>();

        eventListener = new RecordingEventListener();
        eventManager = new DefaultEventManager();
        eventManager.register(eventListener);

        threadFactory = new PulseThreadFactory();

        globalConfiguration = new GlobalConfiguration();
        configurationProvider = mock(ConfigurationProvider.class);
        stub(configurationProvider.get(GlobalConfiguration.class)).toReturn(globalConfiguration);

        callbackService = mock(CallbackService.class);
        shutdownManager = mock(ShutdownManager.class);
        projectManager = mock(ProjectManager.class);

        testSupport = ProjectTestSupport.createSupport(projectManager);
        stub(projectManager.getLatestBuiltRevisions()).toReturn(latestBuildRevisions);

        scmManager = mock(ScmManager.class);

        clock = new TestClock();

        objectFactory = new WiringObjectFactory();
        objectFactory.initProperties(this);

        service = objectFactory.buildBean(PollingService.class);

        serviceHandle = new PollingServiceHandle(service);
    }

    @Override
    protected void tearDown() throws Exception
    {
        service.stop(true);

        super.tearDown();
    }

    public void testInitialisationRunsExpectedRequests() throws Exception
    {
        serviceHandle.init();

        verify(callbackService, times(1)).registerCallback((NullaryProcedure) anyObject(), anyLong());
        verify(shutdownManager, times(1)).addStoppable(service);
    }

    public void testPollingWithNoProjects() throws Exception
    {
        serviceHandle.init();
        serviceHandle.pollAndWait();

        assertScmChanges();
        assertPolledForChanges();
    }

    public void testSingleProjectWithNoChanges() throws Exception
    {
        Project project = createProject("project");

        serviceHandle.init();
        serviceHandle.pollAndWait();

        assertScmChanges();
        assertPolledForChanges();

        ScmClient client = scmClientsByProject.get(project);
        verify(client, times(1)).getLatestRevision((ScmContext) anyObject());
    }

    public void testSingleProjectWithPreviousBuildButNoChanges() throws Exception
    {
        latestProjectRevision = new Revision(2);

        Project project = createProject("project");

        serviceHandle.init();
        serviceHandle.pollAndWait();

        assertScmChanges();
        assertPolledForChanges(project);
    }

    public void testSingleProjectWithChange() throws Exception
    {
        Project project = createProject("project");

        serviceHandle.init();

        setLatestRevision(project, new Revision(3));

        serviceHandle.pollAndWait();

        assertScmChanges();
        assertPolledForChanges(project);

        ScmClient client = scmClientsByProject.get(project);
        verify(client, times(1)).getLatestRevision((ScmContext) anyObject());
    }

    public void testSingleProjectWithPreviousBuildAndChange() throws Exception
    {
        latestProjectRevision = new Revision(2);

        Project project = createProject("project");

        serviceHandle.init();

        setLatestRevision(project, new Revision(3));

        serviceHandle.pollAndWait();

        assertScmChanges(change(project, 3, 2));
        assertPolledForChanges(project);
    }

    public void testDoNotPollWhenMonitorIsFalse() throws Exception
    {
        latestProjectRevision = new Revision(2);

        createProject("project", false);

        serviceHandle.init();
        serviceHandle.pollAndWait();

        assertScmChanges();
        assertPolledForChanges();
    }

    public void testPollWhenMonitorIsTrue() throws Exception
    {
        latestProjectRevision = new Revision(2);

        Project project = createProject("project", true);

        serviceHandle.init();
        serviceHandle.pollAndWait();

        assertScmChanges();
        assertPolledForChanges(project);
    }

    public void testDoNotPollWhenWithinCheckInterval() throws Exception
    {
        latestProjectRevision = new Revision(2);

        Project project = createProject("project");
        project.setLastPollTime(now());

        serviceHandle.init();
        serviceHandle.pollAndWait();

        assertScmChanges();
        assertPolledForChanges();
    }

    public void testPollWhenOutsideCheckInterval() throws Exception
    {
        latestProjectRevision = new Revision(2);

        Project project = createProject("project");
        project.setLastPollTime(now());

        serviceHandle.init();

        clock.add(10 * Constants.MINUTE);

        serviceHandle.pollAndWait();

        assertScmChanges();
        assertPolledForChanges(project);
    }

    public void testCustomCheckInterval() throws Exception
    {
        latestProjectRevision = new Revision(2);

        Project project = createProject("project");
        project.setLastPollTime(now());
        setCustomPollingInterval(project, 15);

        serviceHandle.init();

        clock.add(5 * Constants.MINUTE);

        serviceHandle.pollAndWait();
        assertScmChanges();
        assertPolledForChanges();

        clock.add(5 * Constants.MINUTE);

        serviceHandle.pollAndWait();
        assertScmChanges();
        assertPolledForChanges();

        clock.add(5 * Constants.MINUTE + 1);

        serviceHandle.pollAndWait();
        assertScmChanges();
        assertPolledForChanges(project);
    }

    public void testPollingIsParallelForDifferentScmServers() throws Exception
    {
        blockingScmServers = true;
        latestProjectRevision = new Revision(2);

        Project projectA = createProject("projectA");
        Project projectB = createProject("projectB");

        serviceHandle.init();
        serviceHandle.startPolling();

        waitForPollingInProgress(projectA, projectB);
        releasePollingProcess(projectA, projectB);

        assertTrue(serviceHandle.waitForPollingComplete());
        assertTrue(serviceHandle.isPollingComplete());

        assertScmChanges();
        assertPolledForChanges(projectA, projectB);
    }

    public void testPollingIsSequentialForSingleScmServer() throws Exception
    {
        // Project A and B communicate with the same scm server and therefore whilst
        // unrelated, there requests must be serialised.

        ScmServer scmServer = new ScmServer("a", true);
        latestProjectRevision = new Revision(2);

        Project projectA = createProject("projectA", scmServer);
        Project projectB = createProject("projectB", scmServer);

        serviceHandle.init();
        serviceHandle.startPolling();

        assertTrue(scmServer.waitForInProgress());
        assertPollingInProgress(scmServer.getActiveProject());        
        releasePollingProcess(scmServer.getActiveProject());

        assertFalse(serviceHandle.isPollingComplete());

        assertTrue(scmServer.waitForInProgress());
        assertPollingInProgress(scmServer.getActiveProject());
        releasePollingProcess(scmServer.getActiveProject());

        assertTrue(serviceHandle.waitForPollingComplete());
        assertTrue(serviceHandle.isPollingComplete());

        assertScmChanges();
        assertPolledForChanges(projectA, projectB);
    }

    public void testDoNotPollProjectsThatAreNotInitialised() throws Exception
    {
        latestProjectRevision = new Revision(2);

        createProject("project", INITIAL);

        serviceHandle.init();
        serviceHandle.pollAndWait();

        assertScmChanges();
        assertPolledForChanges();
    }

    public void testExtendedDependentTreePollingOccursInDependencyOrder() throws Exception
    {
        // Test that the extended dependency tree of the form (utilA,utilB) -> (lib) -> (clientA,clientB)
        // is handled in the correct order.

        blockingScmServers = true;
        latestProjectRevision = new Revision(2);

        Project utilA = createProject("utilA", true);
        Project utilB = createProject("utilB", true);
        Project lib = createProject("lib", true, dependency(utilA), dependency(utilB));
        Project clientA = createProject("clientA", true, dependency(lib));
        Project clientB = createProject("clientB", true, dependency(lib));

        serviceHandle.init();
        serviceHandle.startPolling();

        waitForPollingInProgress(utilA, utilB);
        releasePollingProcess(utilA);
        assertPollingInProgress(utilB);
        releasePollingProcess(utilB);
        waitForPollingInProgress(lib);
        releasePollingProcess(lib);
        waitForPollingInProgress(clientA, clientB);
        releasePollingProcess(clientA, clientB);

        assertTrue(serviceHandle.waitForPollingComplete());
        assertTrue(serviceHandle.isPollingComplete());

        assertScmChanges();
        assertPolledForChanges(utilA, utilB, lib, clientA, clientB);
    }

    public void testDependencyTreePolledIfOneProjectIsReadyToCheck() throws ScmException, InterruptedException, ExecutionException
    {
        // UTIL would not normally be polled because its last poll time was recently. Because it is
        // part of the dependency tree with LIB, this is overridden because LIB is ready for polling.

        latestProjectRevision = new Revision(2);

        Project util = createProject("util", true);
        util.setLastPollTime(now());
        Project lib = createProject("lib", true, dependency(util));
        lib.setLastPollTime(now() - 10 * Constants.MINUTE);

        serviceHandle.init();
        serviceHandle.pollAndWait();

        assertScmChanges();
        assertPolledForChanges(util, lib);
    }

    public void testDependencyTreeNotPolledWhenProjectsWereRecentlyPolled() throws Exception
    {
        // Both projects in the dependency tree were recently polled, so do not poll them again.
        latestProjectRevision = new Revision(2);

        Project util = createProject("util", true);
        util.setLastPollTime(now());
        Project lib = createProject("lib", true, dependency(util));
        lib.setLastPollTime(now());

        serviceHandle.init();
        serviceHandle.pollAndWait();

        assertScmChanges();
        assertPolledForChanges();
    }

    public void testDependencyTreePollExcludesProjectsWithMonitorFalse() throws Exception
    {
        // Only the projects that are marked for monitoring are polled, even if the rest of
        // the dependency tree is being polled.
        latestProjectRevision = new Revision(2);

        Project util = createProject("util", true);
        Project lib = createProject("lib", false, dependency(util));
        Project client = createProject("client", true, dependency(lib));

        serviceHandle.init();
        serviceHandle.pollAndWait();

        assertScmChanges();
        assertPolledForChanges(util, client);
    }

    public void testDependencyOrderingRetainedEvenIfNotAllOfDependencyTreeIsPolled() throws Exception
    {
        // Our dependency tree is split by the fact that the middle project is
        // not being monitored.  This must not impact the correct ordering of the
        // polling.

        latestProjectRevision = new Revision(2);
        blockingScmServers = true;

        Project util = createProject("util", true);
        Project lib = createProject("lib", false, dependency(util));
        Project client = createProject("client", true, dependency(lib));

        serviceHandle.init();
        serviceHandle.startPolling();

        waitForPollingInProgress(util);
        releasePollingProcess(util);
        waitForPollingInProgress(client);
        releasePollingProcess(client);

        assertTrue(serviceHandle.waitForPollingComplete());
        assertTrue(serviceHandle.isPollingComplete());

        assertScmChanges();
        assertPolledForChanges(util, client);
    }

    public void testScmChangesGeneratedInDependencyOrder() throws Exception
    {
        latestProjectRevision = new Revision(1);

        Project util = createProject("util");
        Project lib = createProject("lib", dependency(util));
        Project client = createProject("client", dependency(lib));

        serviceHandle.init();

        setLatestRevision(util, new Revision(4));
        setLatestRevision(client, new Revision(3));
        setLatestRevision(lib, new Revision(5));

        serviceHandle.pollAndWait();

        assertScmChanges(
                change(util, 4, 1),
                change(lib, 5, 1),
                change(client, 3, 1)
        );
        assertPolledForChanges(util, lib, client);
    }

    public void testQuietPeriod() throws Exception
    {
        latestProjectRevision = new Revision(1);

        Project util = createProject("util");
        setQuietPeriod(util, 1);
        serviceHandle.init();

        setLatestRevision(util, new Revision(4));
        serviceHandle.pollAndWait();

        assertScmChanges();
        assertPolledForChanges(util);

        clock.add(1 * Constants.MINUTE + 1);
        serviceHandle.pollAndWait();

        assertScmChanges(change(util, 4, 1));
    }

    public void testQuietPeriodOfZeroIgnored() throws ScmException, ExecutionException, InterruptedException
    {
        latestProjectRevision = new Revision(1);

        Project util = createProject("util");
        setQuietPeriod(util, 0);
        serviceHandle.init();

        setLatestRevision(util, new Revision(4));
        serviceHandle.pollAndWait();

        assertScmChanges(change(util, 4, 1));
        assertPolledForChanges(util);
    }

    public void testChangeDuringQuietPeriod() throws Exception
    {
        latestProjectRevision = new Revision(1);

        Project project = createProject("project");
        setQuietPeriod(project, 2);
        serviceHandle.init();

        setLatestRevision(project, new Revision(3));
        serviceHandle.pollAndWait();
        assertScmChanges();

        clock.add(1 * Constants.MINUTE + 1);
        serviceHandle.pollAndWait();
        assertScmChanges();

        setLatestRevision(project, new Revision(4));
        clock.add(1 * Constants.MINUTE + 1);
        serviceHandle.pollAndWait();
        assertScmChanges();

        clock.add(2 * Constants.MINUTE + 1);
        serviceHandle.pollAndWait();
        assertScmChanges(change(project, 4, 1));
    }

    public void testNoProjectStatusEvents() throws Exception
    {
        serviceHandle.init();
        serviceHandle.pollAndWait();

        assertStatusEvents();
    }

    public void testSingleProjectNoChangeStatusEvents() throws Exception
    {
        latestProjectRevision = new Revision(1);
        Project project = createProject("project");

        serviceHandle.init();
        serviceHandle.pollAndWait();

        assertStatusEvents(
                status(project, "polling.start"),
                status(project, "polling.end", "0 ms")
        );
    }

    public void testSingleProjectWithChangeStatusEvents() throws Exception
    {
        latestProjectRevision = new Revision(1);
        Project project = createProject("project");

        serviceHandle.init();
        setLatestRevision(project, new Revision(2));

        serviceHandle.pollAndWait();

        assertStatusEvents(
                status(project, "polling.start"),
                status(project, "polling.end", "0 ms")
        );
    }

    public void testSingleProjectWithQuietPeriodStatusEvents() throws Exception
    {
        latestProjectRevision = new Revision(1);
        Project project = createProject("project");
        setQuietPeriod(project, 1);
        serviceHandle.init();

        setLatestRevision(project, new Revision(2));

        serviceHandle.pollAndWait();

        clock.add(1 * Constants.MINUTE + 1);

        serviceHandle.pollAndWait();

        assertStatusEvents(
                status(project, "polling.start"),
                status(project, "polling.quiet.start", "2"),
                status(project, "polling.end", "0 ms"),
                status(project, "polling.start"),
                status(project, "polling.quiet.end"),
                status(project, "polling.end", "0 ms")
        );
    }

    // CIB-2291
    public void testPollingScmsWithNullUID() throws ScmException, ExecutionException, InterruptedException
    {
        latestProjectRevision = new Revision(1);
        Project projectA = createProject("projectA");
        Project projectB = createProject("projectB");

        stub(scmClientsByProject.get(projectA).getUid()).toReturn(null);
        stub(scmClientsByProject.get(projectB).getUid()).toReturn(null);

        serviceHandle.init();
        serviceHandle.pollAndWait();

        assertScmChanges();
        assertPolledForChanges(projectA, projectB);
    }

    public void testPollingContinuesOnFailure() throws ScmException, ExecutionException, InterruptedException
    {
        Project util = createProject("util");
        stub(scmClientsByProject.get(util).getLatestRevision((ScmContext) anyObject())).toThrow(
                new RuntimeException("Exception during SCM Polling should not be blocking the queue.")
        );

        serviceHandle.init();
        serviceHandle.pollAndWait();

        // A failure to correctly handle this runtime exception will result in this test hanging.
    }

    private void assertStatusEvents(ProjectStatusEvent... expectedEvents)
    {
        List<ProjectStatusEvent> actualEvents = eventListener.getEventsReceived(ProjectStatusEvent.class);
        assertEquals(expectedEvents.length, actualEvents.size());

        for (int i = 0; i < expectedEvents.length; i++)
        {
            ProjectStatusEvent expectedEvent = expectedEvents[i];
            ProjectStatusEvent actualEvent = actualEvents.get(i);
            assertEquals(expectedEvent.getProjectConfiguration(), actualEvent.getProjectConfiguration());
            assertEquals(expectedEvent.getMessage(), actualEvent.getMessage());
        }
    }

    private void releasePollingProcess(Project... projects)
    {
        List<Project> pollingProjects = Arrays.asList(projects);
        for (Project project : pollingProjects)
        {
            ScmServer scmServer = scmServerByProject.get(project);
            assertTrue(scmServer.isInProgress());
            assertEquals(project, scmServer.getActiveProject());
            scmServer.releaseProcess();
            assertTrue(scmServer.waitForNotInProgress());
        }
    }

    private void assertPollingInProgress(Project... projects)
    {
        List<Project> allProjects = projectManager.getProjects(true);
        List<Project> pollingProjects = Arrays.asList(projects);
        for (Project project : allProjects)
        {
            ScmServer scmServer = scmServerByProject.get(project);
            if (pollingProjects.contains(project))
            {
                assertTrue(scmServer.isInProgress());
                assertEquals(project, scmServer.getActiveProject());
            }
            else
            {
                Project activeProject = scmServer.getActiveProject();
                if (activeProject != null)
                {
                    assertFalse(project.equals(scmServer.getActiveProject()));
                }
            }
        }
    }

    private void waitForPollingInProgress(Project... projects)
    {
        List<Project> allProjects = projectManager.getProjects(true);
        List<Project> pollingProjects = Arrays.asList(projects);
        for (Project project : allProjects)
        {
            ScmServer scmServer = scmServerByProject.get(project);
            if (pollingProjects.contains(project))
            {
                assertTrue(scmServer.waitForInProgress());
                assertEquals(project, scmServer.getActiveProject());
            }
            else
            {
                assertFalse("Project " + project.getName() + " should not be polled", scmServer.waitForInProgress(200));
            }
        }
    }

    private void assertScmChanges(ScmChangeEvent... expectedEvents)
    {
        List<ScmChangeEvent> actualEvents = eventListener.getEventsReceived(ScmChangeEvent.class);
        assertEquals(expectedEvents.length, actualEvents.size());
        for (int i = 0; i < expectedEvents.length; i++)
        {
            ScmChangeEvent expectedEvent = expectedEvents[i];
            ScmChangeEvent actualEvent = actualEvents.get(i);
            assertEquals(expectedEvent.getNewRevision(), actualEvent.getNewRevision());
            assertEquals(expectedEvent.getPreviousRevision(), actualEvent.getPreviousRevision());
            assertEquals(expectedEvent.getProjectConfiguration(), actualEvent.getProjectConfiguration());
        }
    }

    private void assertPolledForChanges(Project... projects) throws ScmException
    {
        List<Project> polledProjects = Arrays.asList(projects);
        List<Project> allProjects = projectManager.getProjects(true);
        for (Project project : allProjects)
        {
            ScmClient client = scmClientsByProject.get(project);
            if (polledProjects.contains(project))
            {
                verify(client, times(1)).getRevisions((ScmContext) anyObject(), (Revision) anyObject(), (Revision) anyObject());
            }
            else
            {
                verify(client, never()).getRevisions((ScmContext) anyObject(), (Revision) anyObject(), (Revision) anyObject());
            }
        }
    }

    private DependencyConfiguration dependency(Project target)
    {
        return testSupport.dependency(target);
    }

    private Project createProject(String name, Project.State state) throws ScmException
    {
        return createProject(name, new ScmServer(name + "_uid", blockingScmServers), state, true);
    }

    private Project createProject(String name, DependencyConfiguration... dependencies) throws ScmException
    {
        return createProject(name, new ScmServer(name + "_uid", blockingScmServers), Project.State.IDLE, true, dependencies);
    }

    private Project createProject(String name, ScmServer server, DependencyConfiguration... dependencies) throws ScmException
    {
        return createProject(name, server, Project.State.IDLE, true, dependencies);
    }

    private Project createProject(String name, boolean monitor, DependencyConfiguration... dependencies) throws ScmException
    {
        return createProject(name, new ScmServer(name + "_uid", blockingScmServers), Project.State.IDLE, monitor, dependencies);
    }

    private Project createProject(String name, final ScmServer server, Project.State state, boolean monitor, DependencyConfiguration... dependencies) throws ScmException
    {
        final Project project = testSupport.createProject(name, state, dependencies);

        PollableScmConfiguration scmConfig = mock(PollableScmConfiguration.class);
        stub(scmConfig.isMonitor()).toReturn(monitor);

        ScmClient scmClient = mock(ScmClient.class);
        stub(scmManager.createClient(scmConfig)).toReturn(scmClient);

        ScmContextImpl context = new ScmContextImpl();
        context.setProjectHandle(project.getConfig().getHandle());
        context.setProjectName(project.getName());
        stub(scmManager.createContext(project.getConfig())).toReturn(context);

        scmClientsByProject.put(project, scmClient);

        project.getConfig().setScm(scmConfig);

        stub(scmClient.getUid()).toAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return server.getUid();
            }
        });
        stub(scmClient.getLatestRevision((ScmContext) anyObject())).toAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return server.getLatestRevision(project);
            }
        });
        stub(scmClient.getRevisions((ScmContext) anyObject(), (Revision) anyObject(), (Revision) anyObject())).toAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Revision from = (Revision) invocationOnMock.getArguments()[1];
                Revision to = (Revision) invocationOnMock.getArguments()[2];
                return server.getRevisions(project, from, to);
            }
        });

        scmServerByProject.put(project, server);

        if (latestProjectRevision != null)
        {
            setLatestRevision(project, latestProjectRevision);
        }

        return project;
    }

    private void setQuietPeriod(Project project, int period)
    {
        PollableScmConfiguration scm = (PollableScmConfiguration) project.getConfig().getScm();
        stub(scm.getQuietPeriod()).toReturn(period);
        stub(scm.isQuietPeriodEnabled()).toReturn(true);
    }

    private void setCustomPollingInterval(Project project, int interval)
    {
        PollableScmConfiguration scm = (PollableScmConfiguration) project.getConfig().getScm();
        stub(scm.getPollingInterval()).toReturn(interval);
        stub(scm.isCustomPollingInterval()).toReturn(true);
    }

    private void setLatestRevision(Project project, final Revision revision) throws ScmException
    {
        latestBuildRevisions.put(project.getId(), revision);

        ScmServer server = scmServerByProject.get(project);

        server.setLatestRevision(project, revision);
    }

    private ScmChangeEvent change(Project project, int newRevision, int oldRevision)
    {
        return new ScmChangeEvent(project.getConfig(), new Revision(newRevision), new Revision(oldRevision));
    }

    private ProjectStatusEvent status(Project project, String key, Object... args)
    {
        return new ProjectStatusEvent(this, project.getConfig(), I18N.format(key, args));
    }

    private Long now()
    {
        return clock.getCurrentTimeMillis();
    }

    /**
     * A wrapper around the polling service object that executes the polling
     * on a separate thread and allows us to query the status of the polling.
     */
    private class PollingServiceHandle
    {
        private PollingService service;
        private ExecutorService executor;
        private Future result;

        private PollingServiceHandle(PollingService service)
        {
            this.service = service;
        }

        public void init()
        {
            this.service.init();
        }

        public void startPolling() throws InterruptedException
        {
            executor = Executors.newFixedThreadPool(1, threadFactory);
            result = executor.submit(new Runnable()
            {
                public void run()
                {
                    service.pollForChanges();
                }
            });
        }

        public void pollAndWait() throws InterruptedException, ExecutionException
        {
            startPolling();
            waitForPollingComplete();
        }

        private boolean isPollingComplete()
        {
            return result.isDone();
        }

        private boolean waitForPollingComplete() throws ExecutionException, InterruptedException
        {
            result.get();
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
     * It implements a set of methods that will block until manually
     * released.  If an attempt is made to call one of these methods
     * whilst it is already in progress, an error is generated.
     */
    private class ScmServer
    {
        private static final long TIMEOUT = 2000;

        private String uid;
        private boolean blocking = false;
        private Semaphore entrySemaphore;
        private Semaphore processSemaphore;

        private boolean wasInactive = true;

        private Map<Project, Revision> latestRevisionByProject;
        private Project activeProject;

        private ScmServer(String uid, boolean blocking)
        {
            this.uid = uid;
            this.blocking = blocking;
            this.entrySemaphore = new Semaphore(1);
            this.processSemaphore = new Semaphore(0);

            this.latestRevisionByProject = new HashMap<Project, Revision>();
        }

        public synchronized String getUid()
        {
            return uid;
        }

        public synchronized void setLatestRevision(Project project, Revision revision)
        {
            latestRevisionByProject.put(project, revision);
        }

        private <T> T execute(NullaryFunction<T> f, Project project) throws InterruptedException
        {
            if (!entrySemaphore.tryAcquire())
            {
                throw new RuntimeException("Request already in progress.");
            }
            synchronized (this)
            {
                activeProject = project;
            }
            try
            {

                if (blocking)
                {
                    processSemaphore.acquire();
                }

                return f.process();
            }
            finally
            {
                synchronized (this)
                {
                    wasInactive = true;
                    activeProject = null;
                }
                entrySemaphore.release();
            }
        }

        public Revision getLatestRevision(final Project project) throws InterruptedException
        {
            return execute(new NullaryFunction<Revision>()
            {
                public Revision process()
                {
                    synchronized (ScmServer.this)
                    {
                        return latestRevisionByProject.get(project);
                    }
                }
            }, project);
        }

        public List<Revision> getRevisions(final Project project, final Revision from, final Revision to) throws InterruptedException
        {
            return execute(new NullaryFunction<List<Revision>>()
            {
                public List<Revision> process()
                {
                    Revision latestRevision;
                    synchronized (ScmServer.this)
                    {
                        latestRevision = latestRevisionByProject.get(project);
                    }

                    if (latestRevision == null)
                    {
                        return Arrays.asList();
                    }
                    if (from == null)
                    {
                        return Arrays.asList(latestRevision);
                    }
                    if (Integer.valueOf(from.getRevisionString()) < Integer.valueOf(latestRevision.getRevisionString()))
                    {
                        return Arrays.asList(latestRevision);
                    }
                    return Arrays.asList();
                }
            }, project);
        }

        public synchronized boolean isInProgress()
        {
            return entrySemaphore.availablePermits() == 0;
        }

        public synchronized Project getActiveProject()
        {
            return activeProject;
        }

        public boolean waitForNotInProgress()
        {
            try
            {
                waitForCondition(new Condition()
                {
                    public boolean satisfied()
                    {
                        synchronized (ScmServer.this)
                        {
                            return wasInactive;
                        }
                    }
                }, TIMEOUT, "");

                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        }

        public boolean waitForInProgress()
        {
            return waitForInProgress(TIMEOUT);
        }

        public boolean waitForInProgress(long timeout)
        {
            try
            {
                waitForCondition(new Condition()
                {
                    public boolean satisfied()
                    {
                        return isInProgress();
                    }
                }, timeout, "");

                synchronized (this)
                {
                    wasInactive = false;
                }

                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        }

        public synchronized void releaseProcess()
        {
            if (!blocking)
            {
                throw new RuntimeException("No need to release a non-blocking server.");
            }
            processSemaphore.release();
        }
    }
}
