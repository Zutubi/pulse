package com.zutubi.pulse.master;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.api.Configuration;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.LinkedList;
import java.util.List;

public class DependentBuildControllerTest extends PulseTestCase
{
    private EventManager eventManager;
    private ConfigurationProvider configurationProvider;
    private ProjectManager projectManager;

    private long ID_SEQUENCE = 1;

    private Project projectA;
    private Project projectB;
    private Project projectC;

    private List<DependencyConfiguration> dependencies;
    private List<ProjectConfiguration> triggeredProjects = new LinkedList<ProjectConfiguration>();

    protected void setUp() throws Exception
    {
        super.setUp();

        eventManager = new DefaultEventManager();
        configurationProvider = mock(ConfigurationProvider.class);
        projectManager = mock(ProjectManager.class);
        stub(projectManager.isProjectValid((Project) anyObject())).toReturn(true);
        doAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                triggeredProjects.add((ProjectConfiguration) invocationOnMock.getArguments()[0]);
                return null;
            }
        }).when(projectManager).triggerBuild((ProjectConfiguration)anyObject(), anyList(), (BuildReason)anyObject(), (Revision) anyObject(), anyString(), anyBoolean(), anyBoolean());

        DependentBuildController controller = new DependentBuildController();
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
        assertTriggersFor();
    }

    public void testTriggerOnSuccessfulDependentBuild_SingleDependent()
    {
        configureDependencies(projectB, projectA);

        triggerBuild(projectA, true);

        // expect the build controller to generate build requests.
        assertTriggersFor(projectB);
    }

    public void testTriggerOnSuccessfulDependentBuild_MultipleDependents()
    {
        configureDependencies(projectC, projectA);
        configureDependencies(projectB, projectA);

        triggerBuild(projectA, true);

        // expect the build controller to generate build requests.
        assertTriggersFor(projectB, projectC);
    }

    public void testNoTriggerOnUnsuccessfulDependentBuild()
    {
        configureDependencies(projectB, projectA);

        triggerBuild(projectA, false);

        // expect the build controller to generate build requests.
        assertTriggersFor();
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

    private void assertTriggersFor(Project... projects)
    {
        assertEquals(projects.length, triggeredProjects.size());

        for (Project project : projects)
        {
            assertTrue(triggeredProjects.remove(project.getConfig()));
        }

        assertEquals(0, triggeredProjects.size());
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
}
