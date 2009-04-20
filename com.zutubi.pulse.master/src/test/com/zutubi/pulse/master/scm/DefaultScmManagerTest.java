package com.zutubi.pulse.master.scm;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.EventManager;
import com.zutubi.events.RecordingEventListener;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.PollableScmConfiguration;
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
import static org.mockito.Mockito.*;

import java.util.LinkedList;
import java.util.concurrent.ThreadFactory;

public class DefaultScmManagerTest extends PulseTestCase
{
    private DefaultScmManager scmManager;
    private EventManager eventManager;
    private ConfigurationProvider configurationProvider;
    private ProjectManager projectManager;
    private ScmClientFactory scmClientFactory;
    private ScmContextFactory scmContextFactory;
    private ShutdownManager shutdownManager;
    private ThreadFactory threadFactory;
    private Scheduler scheduler;
    private LinkedList<Project> projects;
    private RecordingEventListener events;

    protected void setUp() throws Exception
    {
        super.setUp();

        configurationProvider = mock(ConfigurationProvider.class);
        stub(configurationProvider.get(GlobalConfiguration.class)).toReturn(new GlobalConfiguration());

        projects = new LinkedList<Project>();
        projectManager = mock(ProjectManager.class);
        stub(projectManager.getProjects(false)).toReturn(projects);

        scmClientFactory = mock(ScmClientFactory.class);

        scmContextFactory = mock(ScmContextFactory.class);
        stub(scmContextFactory.createContext((ProjectConfiguration) anyObject())).toReturn(new ScmContextImpl());

        shutdownManager = mock(ShutdownManager.class);
        scheduler = mock(Scheduler.class);
        eventManager = new DefaultEventManager();

        threadFactory = new PulseThreadFactory();

        scmManager = new DefaultScmManager();
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
    }

    public void testDoNotPollWhenMonitorIsFalse() throws ScmException
    {
        ScmClient client = createProject(1, false, true);

        scmManager.pollActiveScms();

        // ensure that project 1 was not polled
        verify(client, times(0)).getLatestRevision((ScmContext) anyObject());
    }

    public void testPollWhenMonitorIsTrue() throws ScmException
    {
        ScmClient client = createProject(1, true, true);
        stub(client.getLatestRevision((ScmContext) anyObject())).toReturn(new Revision("1"));

        scmManager.pollActiveScms();

        // ensure that project 1 was polled
        verify(client, times(1)).getLatestRevision((ScmContext) anyObject());
    }

    private ScmClient createProject(long id, boolean monitor, boolean initialised) throws ScmException
    {
        PollableScmConfiguration scm = mock(PollableScmConfiguration.class);
        stub(scm.isMonitor()).toReturn(monitor);

        ScmClient client = mock(ScmClient.class);
        stub(client.getUid()).toReturn("serverA");
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
}
