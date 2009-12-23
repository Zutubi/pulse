package com.zutubi.pulse.master.scm.polling;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.EXTENSION_PROJECT_TRIGGERS;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.stub;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.mockito.Matchers.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A utility class with a set of methods that help simplify the
 * setup of configuration data within unit tests.
 */
public class ProjectTestSupport
{
    private final AtomicInteger nextId = new AtomicInteger(1);

    private ProjectManager projectManager;

    private Map<Long, Project> idToProject;
    private Map<String, Project> nameToProject;
    private List<Project> allProjects;
    private List<ProjectConfiguration> allConfigs;

    public static ProjectTestSupport createSupport(ProjectManager projectManager)
    {
        ProjectTestSupport support = new ProjectTestSupport();
        support.setProjectManager(projectManager);
        support.init();
        return support;
    }

    private ProjectTestSupport()
    {
        idToProject = new HashMap<Long, Project>();
        nameToProject = new HashMap<String, Project>();
        allProjects = new LinkedList<Project>();
        allConfigs = new LinkedList<ProjectConfiguration>();
    }

    private void init()
    {
        stub(projectManager.getProject(anyLong(), anyBoolean())).toAnswer(new Answer<Project>()
        {
            public Project answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Long projectId = (Long) invocationOnMock.getArguments()[0];
                return idToProject.get(projectId);
            }
        });
        stub(projectManager.getProject(anyString(), anyBoolean())).toAnswer(new Answer<Project>()
        {
            public Project answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                String projectName = (String) invocationOnMock.getArguments()[0];
                return nameToProject.get(projectName);
            }
        });
        stub(projectManager.getProjects(anyBoolean())).toReturn(allProjects);

        stub(projectManager.getDownstreamDependencies((ProjectConfiguration) anyObject())).toAnswer(new Answer<List<ProjectConfiguration>>()
        {
            public List<ProjectConfiguration> answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                ProjectConfiguration config = (ProjectConfiguration) invocationOnMock.getArguments()[0];
                List<ProjectConfiguration> result = new LinkedList<ProjectConfiguration>();
                for (ProjectConfiguration project : allConfigs)
                {
                    for (DependencyConfiguration dep : project.getDependencies().getDependencies())
                    {
                        if (dep.getProject().equals(config))
                        {
                            result.add(project);
                            break;
                        }
                    }
                }

                return result;
            }
        });
        stub(projectManager.mapConfigsToProjects(anyList())).toAnswer(new Answer<List<Project>>()
        {
            public List<Project> answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Collection<ProjectConfiguration> configs = (Collection<ProjectConfiguration>) invocationOnMock.getArguments()[0];
                return CollectionUtils.map(configs, new Mapping<ProjectConfiguration, Project>()
                {
                    public Project map(ProjectConfiguration projectConfiguration)
                    {
                        return idToProject.get(projectConfiguration.getProjectId());
                    }
                });
            }
        });
    }

    public Project createProject(String projectName)
    {
        return createProject(projectName, Project.State.IDLE);
    }

    public Project createProject(String projectName, Project.State state, DependencyConfiguration... dependencies)
    {
        if (nameToProject.containsKey(projectName))
        {
            throw new RuntimeException("Project already exists");
        }
        Project project = new Project(state);
        project.setId(nextId.getAndIncrement());
        ProjectConfiguration projectConfiguration = new ProjectConfiguration();
        projectConfiguration.setName(projectName);
        projectConfiguration.setHandle(nextId.getAndIncrement());
        projectConfiguration.setProjectId(project.getId());
        projectConfiguration.getDependencies().getDependencies().addAll(Arrays.asList(dependencies));
        HashMap<String, Object> triggers = new HashMap<String, Object>();
        triggers.put("dependent trigger", new DependentBuildTriggerConfiguration());
        projectConfiguration.addExtension(EXTENSION_PROJECT_TRIGGERS, triggers);
        project.setConfig(projectConfiguration);

        idToProject.put(project.getId(), project);
        nameToProject.put(project.getName(), project);
        allProjects.add(project);
        allConfigs.add(projectConfiguration);

        return project;
    }

    protected DependencyConfiguration dependency(Project project)
    {
        DependencyConfiguration dependencyConfiguration = new DependencyConfiguration();
        dependencyConfiguration.setProject(project.getConfig());
        return dependencyConfiguration;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
