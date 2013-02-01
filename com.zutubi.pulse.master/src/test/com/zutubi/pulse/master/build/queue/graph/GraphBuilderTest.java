package com.zutubi.pulse.master.build.queue.graph;

import com.google.common.base.Function;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.CollectionUtils;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.LinkedList;
import java.util.List;

public class GraphBuilderTest extends BaseGraphTestCase
{
    private GraphBuilder builder;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        projectManager = mock(ProjectManager.class);
        stub(projectManager.getAllProjectConfigs(anyBoolean())).toReturn(allConfigs);
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
                List<ProjectConfiguration> configs = (List<ProjectConfiguration>) invocationOnMock.getArguments()[0];
                return CollectionUtils.map(configs, new Function<ProjectConfiguration, Project>()
                {
                    public Project apply(ProjectConfiguration projectConfiguration)
                    {
                        return idToProject.get(projectConfiguration.getProjectId());
                    }
                });
            }
        });
        stub(projectManager.getProject(anyLong(), anyBoolean())).toAnswer(new Answer<Project>()
        {
            public Project answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Long projectId = (Long) invocationOnMock.getArguments()[0];
                return idToProject.get(projectId);
            }
        });

        builder = new GraphBuilder();
        builder.setProjectManager(projectManager);
    }

    public void testSingleProject()
    {
        ProjectConfiguration project = project("projectA");

        assertEquals(node(project), builder.buildUpstreamGraph(project));
        assertEquals(node(project), builder.buildDownstreamGraph(project));
    }

    public void testSingleUpstreamProject()
    {
        ProjectConfiguration util = project("util");
        ProjectConfiguration client = project("client", dependency(util));

        assertEquals(
                node(client,
                        node(util)),
                builder.buildUpstreamGraph(client));
    }

    public void testMultipleUpstreamProjects()
    {
        ProjectConfiguration util = project("util");
        ProjectConfiguration lib = project("lib");
        ProjectConfiguration client = project("client", dependency(util), dependency(lib));

        assertEquals(
                node(client,
                        node(util),
                        node(lib)),
                builder.buildUpstreamGraph(client));
    }

    public void testTransitiveUpstreamProjects()
    {
        ProjectConfiguration util = project("util");
        ProjectConfiguration lib = project("lib", dependency(util));
        ProjectConfiguration client = project("client", dependency(lib));

        assertEquals(
                node(client,
                        node(lib,
                                node(util))),
                builder.buildUpstreamGraph(client));
    }

    public void testMultipleTransitiveUpstreamProjects()
    {
        ProjectConfiguration utilA = project("utilA");
        ProjectConfiguration utilB = project("utilB");
        ProjectConfiguration lib = project("lib", dependency(utilA), dependency(utilB));
        ProjectConfiguration client = project("client", dependency(lib));

        assertEquals(
                node(client,
                        node(lib,
                                node(utilA),
                                node(utilB))),
                builder.buildUpstreamGraph(client));
    }

    public void testSingleDownstreamProject()
    {
        ProjectConfiguration util = project("util");
        ProjectConfiguration client = project("client", dependency(util));

        assertEquals(
                node(util,
                        node(client)),
                builder.buildDownstreamGraph(util));
    }

    public void testMultipleDownStreamProjects()
    {
        ProjectConfiguration util = project("util");
        ProjectConfiguration clientA = project("clientA", dependency(util));
        ProjectConfiguration clientB = project("clientB", dependency(util));

        assertEquals(
                node(util,
                        node(clientA),
                        node(clientB)),
                builder.buildDownstreamGraph(util));
    }

    public void testTransitiveDownstreamProjects()
    {
        ProjectConfiguration util = project("util");
        ProjectConfiguration lib = project("lib", dependency(util));
        ProjectConfiguration client = project("client", dependency(lib));

        assertEquals(
                node(util,
                        node(lib,
                                node(client))),
                builder.buildDownstreamGraph(util));
    }

    public void testMultipleTransitiveDownstreamProjects()
    {
        ProjectConfiguration util = project("util");
        ProjectConfiguration lib = project("lib", dependency(util));
        ProjectConfiguration clientA = project("clientA", dependency(lib));
        ProjectConfiguration clientB = project("clientB", dependency(lib));

        assertEquals(
                node(util,
                        node(lib,
                                node(clientA),
                                node(clientB))),
                builder.buildDownstreamGraph(util));
    }
}
