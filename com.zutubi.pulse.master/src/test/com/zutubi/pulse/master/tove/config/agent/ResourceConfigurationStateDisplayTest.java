package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ResourceRequirementConfiguration;
import com.zutubi.util.adt.TreeNode;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ResourceConfigurationStateDisplayTest extends PulseTestCase
{
    private static final ResourceConfiguration RESOURCE_1 = new ResourceConfiguration("resource1");
    private static final ResourceConfiguration RESOURCE_2 = new ResourceConfiguration("resource2");
    private static final ResourceConfiguration RESOURCE_3 = new ResourceConfiguration("resource3");

    private ResourceConfigurationStateDisplay display;
    private List<ProjectConfiguration> projects = new LinkedList<ProjectConfiguration>();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        display = new ResourceConfigurationStateDisplay();
        ProjectManager projectManager = mock(ProjectManager.class);
        doReturn(projects).when(projectManager).getAllProjectConfigs(false);
        display.setProjectManager(projectManager);
        display.setMessages(Messages.getInstance(ResourceConfiguration.class));
    }

    public void testNoProjectsNoResources()
    {
        assertEquals("all projects (all stages)", display.formatCollectionCompatibleStages(Collections.<ResourceConfiguration>emptyList()));
    }

    public void testAllProjectsSatisfied()
    {
        projects.add(createProject("p1", RESOURCE_1.getName()));
        projects.add(createProject("p2", RESOURCE_2.getName()));
        assertEquals("all projects (all stages)", display.formatCollectionCompatibleStages(asList(RESOURCE_1, RESOURCE_2)));
    }

    public void testSomeProjectsAllStagesSatisfied()
    {
        ProjectConfiguration project = createProject("p1", RESOURCE_1.getName());
        addStages(project, createStage("s"));
        projects.add(project);
        projects.add(createProject("p2", "unknown"));
        assertEquals("p1 (all stages)", display.formatCollectionCompatibleStages(asList(RESOURCE_1)));
    }

    public void testProjectWithNoStages()
    {
        projects.add(createProject("p1", RESOURCE_1.getName()));
        projects.add(createProject("p2", "unknown"));
        assertEquals("p1 (all stages)", display.formatCollectionCompatibleStages(asList(RESOURCE_1)));
    }

    public void testProjectWithNoSatisfiableStages()
    {
        ProjectConfiguration project = createProject("p1", RESOURCE_1.getName());
        addStages(project, createStage("s", "unknown"));
        projects.add(project);
        assertEquals("p1 (no stages)", display.formatCollectionCompatibleStages(asList(RESOURCE_1)));
    }

    public void testProjectWithSomeSatisfiableStages()
    {
        ProjectConfiguration project = createProject("p1");
        addStages(project, createStage("s1", RESOURCE_1.getName()));
        addStages(project, createStage("s2", "unknown"));
        projects.add(project);

        TreeNode<String> expected = new TreeNode<String>(null,
                new TreeNode<String>("p1 (1 of 2 stages)",
                        new TreeNode<String>("s1")));
        assertEquals(expected, display.formatCollectionCompatibleStages(asList(RESOURCE_1)));
    }

    public void testMixOfProjects()
    {
        projects.add(createProject("nostages"));

        ProjectConfiguration project = createProject("unsatisfiable", "unknown");
        addStages(project, createStage("s"));
        projects.add(project);

        project = createProject("unsatisfiablestages", RESOURCE_1.getName());
        addStages(project, createStage("unsat", "unknown"));
        projects.add(project);

        project = createProject("somestages", RESOURCE_1.getName());
        addStages(project, createStage("sok", RESOURCE_2.getName()));
        addStages(project, createStage("sok2", RESOURCE_3.getName()));
        addStages(project, createStage("snotok", "unknown"));
        projects.add(project);

        project = createProject("allstages", RESOURCE_1.getName());
        addStages(project, createStage("1", RESOURCE_2.getName()));
        addStages(project, createStage("2", RESOURCE_3.getName()));
        projects.add(project);

        TreeNode<String> expected = new TreeNode<String>(null,
                new TreeNode<String>("allstages (all stages)"),
                new TreeNode<String>("nostages (all stages)"),
                new TreeNode<String>("somestages (2 of 3 stages)",
                        new TreeNode<String>("sok"),
                        new TreeNode<String>("sok2")),
                new TreeNode<String>("unsatisfiablestages (no stages)"));
        assertEquals(expected, display.formatCollectionCompatibleStages(asList(RESOURCE_1, RESOURCE_2, RESOURCE_3)));
    }

    private ProjectConfiguration createProject(String name, String... requiredResources)
    {
        ProjectConfiguration project = new ProjectConfiguration(name);
        for (String resource: requiredResources)
        {
            project.getRequirements().add(createRequirement(resource));
        }

        return project;
    }

    private void addStages(ProjectConfiguration project, BuildStageConfiguration... stages)
    {
        for (BuildStageConfiguration stage: stages)
        {
            project.getStages().put(stage.getName(), stage);
        }
    }

    private BuildStageConfiguration createStage(String name, String... requiredResources)
    {
        BuildStageConfiguration stage = new BuildStageConfiguration(name);
        for (String resource: requiredResources)
        {
            stage.getRequirements().add(createRequirement(resource));
        }

        return stage;
    }

    private ResourceRequirementConfiguration createRequirement(String resource)
    {
        return new ResourceRequirementConfiguration(new ResourceRequirement(resource, false, false));
    }
}
