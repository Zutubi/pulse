package com.zutubi.pulse.master.cleanup;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.cleanup.requests.ProjectCleanupRequest;
import com.zutubi.pulse.master.cleanup.requests.UserCleanupRequest;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.util.bean.WiringObjectFactory;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CleanupSchedulerTest extends PulseTestCase
{
    private CleanupScheduler scheduler;
    private EventManager eventManager;
    private CleanupManager cleanupManager;
    private ProjectManager projectManager;
    private WiringObjectFactory objectFactory;
    private List<Project> allProjects = new LinkedList<Project>();

    protected void setUp() throws Exception
    {
        super.setUp();

        eventManager = new DefaultEventManager();

        cleanupManager = mock(CleanupManager.class);
        projectManager = mock(ProjectManager.class);
        objectFactory = new WiringObjectFactory();
        scheduler = new CleanupScheduler();
        scheduler.setEventManager(eventManager);
        scheduler.setCleanupManager(cleanupManager);
        scheduler.setObjectFactory(objectFactory);
        scheduler.setProjectManager(projectManager);
        scheduler.initEventScheduling();

        objectFactory.initProperties(this);
    }

    public void testBuildCompletedEventTriggering()
    {
        Project project = createProject(1, "project");

        buildCompleted(project);

        verify(cleanupManager, times(1)).process(new ProjectCleanupRequest(project));
    }

    public void testPersonalBuildCompletedEventTriggering()
    {
        User user = createUser(1, "user");
        buildCompleted(user);

        verify(cleanupManager, times(1)).process(new UserCleanupRequest(user));
    }

    public void testScheduledCallback()
    {
        scheduler.scheduleProjectCleanup();
        verify(cleanupManager, times(1)).process(new LinkedList<Runnable>());

        Project projectA = createProject(1, "projectA");
        Project projectB = createProject(1, "projectA");
        scheduler.scheduleProjectCleanup();
        verify(cleanupManager, times(1)).process(Arrays.<Runnable>asList(new ProjectCleanupRequest(projectA), new ProjectCleanupRequest(projectB)));
    }

    public void testScheduleProjectCleanupNoProjects()
    {
        stub(projectManager.getProjects(false)).toReturn(new LinkedList<Project>());
        scheduler.scheduleProjectCleanup();

        verify(cleanupManager, times(1)).process(new LinkedList<Runnable>());
    }

    public void testScheduleProjectCleanupSomeProjects()
    {
        Project projectA = createProject(1, "a");
        Project projectB = createProject(2, "b");
        stub(projectManager.getProjects(false)).toReturn(Arrays.asList(projectA, projectB));
        scheduler.scheduleProjectCleanup();

        verify(cleanupManager, times(1)).process(Arrays.asList((Runnable)new ProjectCleanupRequest(projectA), new ProjectCleanupRequest(projectB)));
    }

    private User createUser(int id, String name)
    {
        User user = new User();
        user.setId(id);
        UserConfiguration config = new UserConfiguration();
        config.setName(name);
        user.setConfig(config);
        return user;
    }

    private Project createProject(int id, String name)
    {
        Project project = new Project();
        project.setId(id);
        ProjectConfiguration config = new ProjectConfiguration(name);
        config.setProjectId(id);
        project.setConfig(config);
        allProjects.add(project);

        stub(projectManager.getProjects(false)).toReturn(allProjects);
        stub(projectManager.getProject(id, false)).toReturn(project);
        stub(projectManager.getProject(name, false)).toReturn(project);

        stub(projectManager.getProjectConfig(id, false)).toReturn(config);
        stub(projectManager.getProjectConfig(name, false)).toReturn(config);

        return project;
    }

    private void buildCompleted(User user)
    {
        BuildResult result = mock(BuildResult.class);
        stub(result.isPersonal()).toReturn(true);
        stub(result.getUser()).toReturn(user);

        publish(result);
    }

    private void buildCompleted(Project project)
    {
        BuildResult result = mock(BuildResult.class);
        stub(result.getProject()).toReturn(project);

        publish(result);
    }

    private void publish(BuildResult result)
    {
        BuildCompletedEvent event = new BuildCompletedEvent(this, result, null);
        eventManager.publish(event);
    }
}