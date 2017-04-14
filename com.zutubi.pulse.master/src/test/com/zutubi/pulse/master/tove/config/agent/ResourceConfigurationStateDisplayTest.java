/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ResourceRequirementConfiguration;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ResourceConfigurationStateDisplayTest extends PulseTestCase
{
    private static final ResourceConfiguration RESOURCE_1 = new ResourceConfiguration("resource1");
    private static final ResourceConfiguration RESOURCE_2 = new ResourceConfiguration("resource2");
    private static final ResourceConfiguration RESOURCE_3 = new ResourceConfiguration("resource3");

    private ResourceConfigurationStateDisplay display;
    private List<ProjectConfiguration> projects = new ArrayList<>();

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
        assertEquals("p1 (all stages)", display.formatCollectionCompatibleStages(singletonList(RESOURCE_1)));
    }

    public void testProjectWithNoStages()
    {
        projects.add(createProject("p1", RESOURCE_1.getName()));
        projects.add(createProject("p2", "unknown"));
        assertEquals("p1 (all stages)", display.formatCollectionCompatibleStages(singletonList(RESOURCE_1)));
    }

    public void testProjectWithNoSatisfiableStages()
    {
        ProjectConfiguration project = createProject("p1", RESOURCE_1.getName());
        addStages(project, createStage("s", "unknown"));
        projects.add(project);
        assertEquals("p1 (no stages)", display.formatCollectionCompatibleStages(singletonList(RESOURCE_1)));
    }

    public void testProjectWithSomeSatisfiableStages()
    {
        ProjectConfiguration project = createProject("p1");
        addStages(project, createStage("s1", RESOURCE_1.getName()));
        addStages(project, createStage("s2", "unknown"));
        projects.add(project);

        Map<String, List<String>> expected = new HashMap<>();
        expected.put("p1 (1 of 2 stages)", singletonList("s1"));
        assertEquals(expected, display.formatCollectionCompatibleStages(singletonList(RESOURCE_1)));
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

        Map<String, List<String>> expected = new HashMap<>();
        expected.put("allstages (all stages)", Collections.<String>emptyList());
        expected.put("nostages (all stages)", Collections.<String>emptyList());
        expected.put("somestages (2 of 3 stages)", asList("sok", "sok2"));
        expected.put("unsatisfiablestages (no stages)", Collections.<String>emptyList());
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
