package com.zutubi.pulse.master;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.EventManager;
import com.zutubi.events.EventListener;
import com.zutubi.events.Event;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.UnknownBuildReason;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.api.Configuration;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.LinkedList;

public class DependentBuildControllerTest extends PulseTestCase
{
    private EventManager eventManager;
    private EventLogger logger;
    private DependentBuildController controller;
    private ConfigurationProvider configurationProvider;
    private ProjectManager projectManager;

    private long ID_SEQUENCE = 1;

    private Project projectA;
    private Project projectB;
    private Project projectC;

    private List<DependencyConfiguration> dependencies;

    protected void setUp() throws Exception
    {
        super.setUp();

        logger = new EventLogger();
        eventManager = new DefaultEventManager();
        eventManager.register(logger);

        configurationProvider = mock(ConfigurationProvider.class);
        projectManager = mock(ProjectManager.class);

        controller = new DependentBuildController();
        controller.setEventManager(eventManager);
        controller.setConfigurationProvider(configurationProvider);
        controller.setProjectManager(projectManager);
        controller.init();

        projectA = newProject("projectA");
        projectB = newProject("projectB");
        projectC = newProject("projectC");

        dependencies = new LinkedList<DependencyConfiguration>();
        stub(configurationProvider.getAll(DependencyConfiguration.class)).toReturn(this.dependencies);
    }

    // Usecase 1: If a dependent integration build is successful, it should trigger
    // the projects that depend upon it.

    public void testTriggerOnSuccessfulDependentBuild_ZeroDependents()
    {
        triggerBuild(projectA, true);

        // expect the build controller to generate build requests.
        assertBuildRequestsFor();
    }

    public void testTriggerOnSuccessfulDependentBuild_SingleDependent()
    {
        configureDependencies(projectB, projectA);

        triggerBuild(projectA, true);

        // expect the build controller to generate build requests.
        assertBuildRequestsFor(projectB);
    }

    public void testTriggerOnSuccessfulDependentBuild_MultipleDependents()
    {
        configureDependencies(projectC, projectA);
        configureDependencies(projectB, projectA);

        triggerBuild(projectA, true);

        // expect the build controller to generate build requests.
        assertBuildRequestsFor(projectB, projectC);
    }

    public void testNoTriggerOnUnsuccessfulDependentBuild()
    {
        configureDependencies(projectB, projectA);

        triggerBuild(projectA, false);

        // expect the build controller to generate build requests.
        assertBuildRequestsFor();
    }

    private void triggerBuild(Project project, boolean successful)
    {
        BuildResult result = new BuildResult(new UnknownBuildReason(), project, 1, false);
        if (successful)
        {
            result.success();
        }
        else
        {
            result.failure();
        }
        BuildCompletedEvent evt = new BuildCompletedEvent(this, result, null);
        eventManager.publish(evt);
    }

    private void assertBuildRequestsFor(Project... projects)
    {
        List<Event> requests = new LinkedList<Event>(logger.getLog());
        assertEquals(projects.length, requests.size());

        for (Project project : projects)
        {
            BuildRequestEvent found = null;
            for (Event event: requests)
            {
                BuildRequestEvent request = (BuildRequestEvent) event;
                if (request.getOwner().equals(project))
                {
                    found = request;
                    break;
                }
            }
            if (found != null)
            {
                requests.remove(found);
            }
        }

        assertEquals(0, requests.size());
    }

    private Project newProject(String name)
    {
        ProjectConfiguration configuration = create(ProjectConfiguration.class);
        configuration.setName(name);
        configuration.setProjectId(nextId());
        Project project = new Project();
        project.setConfig(configuration);
        project.setId(configuration.getProjectId());
        stub(projectManager.getProject(configuration.getProjectId(), false)).toReturn(project);
        return project;
    }

    private long nextId()
    {
        return ID_SEQUENCE++;
    }

    private void configureDependencies(Project dependent, Project... dependencies)
    {
        List<DependencyConfiguration> dependencyConfigurations = dependent.getConfig().getDependencies().getDependencies();

        for (Project depenency : dependencies)
        {
            DependencyConfiguration dependencyConfiguration = create(DependencyConfiguration.class);
            dependencyConfiguration.setProject(depenency.getConfig());
            dependencyConfigurations.add(dependencyConfiguration);

            stub(configurationProvider.getAncestorOfType(dependencyConfiguration, ProjectConfiguration.class)).toReturn(dependent.getConfig());
            this.dependencies.add(dependencyConfiguration);
        }
    }

    private <T extends Configuration> T create(Class<T> type)
    {
        try
        {
            T instance = type.newInstance();
            instance.setHandle(nextId());
            return instance;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private class EventLogger implements EventListener
    {
        private List<Event> log = new LinkedList<Event>();

        public void handleEvent(Event event)
        {
            log.add(event);
        }

        public List<Event> getLog()
        {
            return log;
        }

        public Class[] getHandledEvents()
        {
            return new Class[]{BuildRequestEvent.class};
        }
    }
}
