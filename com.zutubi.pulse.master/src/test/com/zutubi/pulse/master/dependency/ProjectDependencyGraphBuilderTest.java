package com.zutubi.pulse.master.dependency;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.adt.TreeNode;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class ProjectDependencyGraphBuilderTest extends PulseTestCase
{
    private long nextId = 1;
    private List<ProjectConfiguration> allConfigs = new LinkedList<ProjectConfiguration>();
    private Map<Long, Project> idToProject = new HashMap<Long, Project>();

    private Project utilProject;
    private Project coreProject;
    private Project devProject;
    private Project serverProject;
    private Project slaveProject;
    private Project masterProject;

    private ProjectDependencyGraphBuilder builder = new ProjectDependencyGraphBuilder();

    @Override
    @SuppressWarnings({"unchecked"})
    protected void setUp() throws Exception
    {
        super.setUp();

        utilProject = makeProject("util");
        coreProject = makeProject("core", utilProject);
        devProject = makeProject("dev", utilProject, coreProject);
        serverProject = makeProject("server", utilProject, coreProject);
        slaveProject = makeProject("slave", utilProject, serverProject);
        masterProject = makeProject("master", utilProject, serverProject);

        ProjectManager projectManager = mock(ProjectManager.class);
        stub(projectManager.getAllProjectConfigs(anyBoolean())).toReturn(allConfigs);
        stub(projectManager.getDownstreamDependencies((ProjectConfiguration) anyObject())).toAnswer(new Answer<List<ProjectConfiguration>>()
        {
            public List<ProjectConfiguration> answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                ProjectConfiguration config = (ProjectConfiguration) invocationOnMock.getArguments()[0];
                List<ProjectConfiguration> result = new LinkedList<ProjectConfiguration>();
                for (ProjectConfiguration project: allConfigs)
                {
                    for (DependencyConfiguration dep: project.getDependencies().getDependencies())
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
                return newArrayList(Iterables.transform(configs, new Function<ProjectConfiguration, Project>()
                {
                    public Project apply(ProjectConfiguration projectConfiguration)
                    {
                        return idToProject.get(projectConfiguration.getProjectId());
                    }
                }));
            }
        });

        builder.setProjectManager(projectManager);
    }

    public void testUtilLibraryFull()
    {
        ProjectDependencyGraph graph = builder.build(utilProject, ProjectDependencyGraphBuilder.TransitiveMode.FULL);
        assertEquals(node(utilProject), graph.getUpstreamRoot());
        assertEquals(
                node(utilProject,
                        node(coreProject,
                                node(devProject),
                                node(serverProject,
                                        node(slaveProject),
                                        node(masterProject))),
                        node(devProject),
                        node(serverProject,
                                node(slaveProject),
                                node(masterProject)),
                        node(slaveProject),
                        node(masterProject)),
                graph.getDownstreamRoot());
    }

    public void testUtilLibraryTrimmed()
    {
        ProjectDependencyGraph graph = builder.build(utilProject, ProjectDependencyGraphBuilder.TransitiveMode.TRIM_DUPLICATES);
        assertEquals(node(utilProject), graph.getUpstreamRoot());
        assertEquals(
                node(utilProject,
                        node(coreProject,
                                node(devProject),
                                node(serverProject,
                                        node(slaveProject),
                                        node(masterProject))),
                        filteredNode(devProject),
                        filteredNode(serverProject),
                        filteredNode(slaveProject),
                        filteredNode(masterProject)),
                graph.getDownstreamRoot());
    }

    public void testUtilLibraryRemoved()
    {
        ProjectDependencyGraph graph = builder.build(utilProject, ProjectDependencyGraphBuilder.TransitiveMode.REMOVE_DUPLICATES);
        assertEquals(node(utilProject), graph.getUpstreamRoot());
        assertEquals(
                node(utilProject,
                        node(coreProject,
                                node(devProject),
                                node(serverProject,
                                        node(slaveProject),
                                        node(masterProject)))),
                graph.getDownstreamRoot());
    }

    public void testUtilLibraryNone()
    {
        ProjectDependencyGraph graph = builder.build(utilProject, ProjectDependencyGraphBuilder.TransitiveMode.NONE);
        assertEquals(node(utilProject), graph.getUpstreamRoot());
        assertEquals(
                node(utilProject,
                        node(coreProject),
                        node(devProject),
                        node(serverProject),
                        node(slaveProject),
                        node(masterProject)),
                graph.getDownstreamRoot());
    }

    public void testServerModuleFull()
    {
        ProjectDependencyGraph graph = builder.build(serverProject, ProjectDependencyGraphBuilder.TransitiveMode.FULL);
        assertEquals(
                node(serverProject,
                        node(coreProject,
                                node(utilProject)),
                        node(utilProject)),
                graph.getUpstreamRoot());

        assertEquals(
                node(serverProject,
                        node(slaveProject),
                        node(masterProject)),
                graph.getDownstreamRoot());
    }

    public void testServerModuleTrimmed()
    {
        ProjectDependencyGraph graph = builder.build(serverProject, ProjectDependencyGraphBuilder.TransitiveMode.TRIM_DUPLICATES);
        assertEquals(
                node(serverProject,
                        node(coreProject,
                                node(utilProject)),
                        filteredNode(utilProject)),
                graph.getUpstreamRoot());

        assertEquals(
                node(serverProject,
                        node(slaveProject),
                        node(masterProject)),
                graph.getDownstreamRoot());
    }

    public void testServerModuleRemoved()
    {
        ProjectDependencyGraph graph = builder.build(serverProject, ProjectDependencyGraphBuilder.TransitiveMode.REMOVE_DUPLICATES);
        assertEquals(
                node(serverProject,
                        node(coreProject,
                                node(utilProject))),
                graph.getUpstreamRoot());

        assertEquals(
                node(serverProject,
                        node(slaveProject),
                        node(masterProject)),
                graph.getDownstreamRoot());
    }

    public void testServerModuleNone()
    {
        ProjectDependencyGraph graph = builder.build(serverProject, ProjectDependencyGraphBuilder.TransitiveMode.NONE);
        assertEquals(
                node(serverProject,
                        node(coreProject),
                        node(utilProject)),
                graph.getUpstreamRoot());

        assertEquals(
                node(serverProject,
                        node(slaveProject),
                        node(masterProject)),
                graph.getDownstreamRoot());
    }

    public void testMasterModuleFull()
    {
        ProjectDependencyGraph graph = builder.build(masterProject, ProjectDependencyGraphBuilder.TransitiveMode.FULL);
        assertEquals(
                node(masterProject,
                        node(serverProject,
                                node(coreProject,
                                        node(utilProject)),
                                node(utilProject)),
                        node(utilProject)),
                graph.getUpstreamRoot());

        assertEquals(node(masterProject), graph.getDownstreamRoot());
    }

    public void testMasterModuleTrimmed()
    {
        ProjectDependencyGraph graph = builder.build(masterProject, ProjectDependencyGraphBuilder.TransitiveMode.TRIM_DUPLICATES);
        assertEquals(
                node(masterProject,
                        node(serverProject,
                                node(coreProject,
                                        node(utilProject)),
                                filteredNode(utilProject)),
                        filteredNode(utilProject)),
                graph.getUpstreamRoot());

        assertEquals(node(masterProject), graph.getDownstreamRoot());
    }

    public void testMasterModuleRemoved()
    {
        ProjectDependencyGraph graph = builder.build(masterProject, ProjectDependencyGraphBuilder.TransitiveMode.REMOVE_DUPLICATES);
        assertEquals(
                node(masterProject,
                        node(serverProject,
                                node(coreProject,
                                        node(utilProject)))),
                graph.getUpstreamRoot());

        assertEquals(node(masterProject), graph.getDownstreamRoot());
    }

    public void testMasterModuleNone()
    {
        ProjectDependencyGraph graph = builder.build(masterProject, ProjectDependencyGraphBuilder.TransitiveMode.NONE);
        assertEquals(
                node(masterProject,
                        node(serverProject),
                        node(utilProject)),
                graph.getUpstreamRoot());

        assertEquals(node(masterProject), graph.getDownstreamRoot());
    }

    public void testCycle()
    {
        DependencyConfiguration dependencyOnMaster = new DependencyConfiguration();
        dependencyOnMaster.setProject(masterProject.getConfig());
        utilProject.getConfig().getDependencies().getDependencies().add(dependencyOnMaster);
        // Non-explosion is sufficient.
        builder.build(masterProject, ProjectDependencyGraphBuilder.TransitiveMode.FULL);
    }


    private void assertEquals(TreeNode<DependencyGraphData> expected, TreeNode<DependencyGraphData> got)
    {
        assertEquals(expected.getData(), got.getData());

        List<TreeNode<DependencyGraphData>> expectedChildren = expected.getChildren();
        List<TreeNode<DependencyGraphData>> gotChildren = got.getChildren();
        assertEquals(expectedChildren.size(), gotChildren.size());

        // Order of children does not matter.
        for (TreeNode<DependencyGraphData> expectedChild: expectedChildren)
        {
            final DependencyGraphData childData = expectedChild.getData();
            TreeNode<DependencyGraphData> gotChild = find(gotChildren, new Predicate<TreeNode<DependencyGraphData>>()
            {
                public boolean apply(TreeNode<DependencyGraphData> node)
                {
                    return node.getData().equals(childData);
                }
            }, null);

            assertNotNull(gotChild);
            assertEquals(expectedChild, gotChild);
        }
    }

    private TreeNode<DependencyGraphData> node(Project project, TreeNode<DependencyGraphData>... children)
    {
        TreeNode<DependencyGraphData> node = new TreeNode<DependencyGraphData>(new DependencyGraphData(project));
        node.addAll(asList(children));
        return node;
    }

    private TreeNode<DependencyGraphData> filteredNode(Project project, TreeNode<DependencyGraphData>... children)
    {
        DependencyGraphData data = new DependencyGraphData(project);
        data.markSubtreeFiltered();
        TreeNode<DependencyGraphData> node = new TreeNode<DependencyGraphData>(data);
        node.addAll(asList(children));
        return node;
    }

    private Project makeProject(String name, Project... dependencies)
    {
        ProjectConfiguration config = new ProjectConfiguration(name);
        config.setHandle(nextId++);
        config.setProjectId(nextId++);

        config.getDependencies().getDependencies().addAll(transform(asList(dependencies), new Function<Project, DependencyConfiguration>()
        {
            public DependencyConfiguration apply(Project project)
            {
                DependencyConfiguration dependencyConfiguration = new DependencyConfiguration();
                dependencyConfiguration.setProject(project.getConfig());
                return dependencyConfiguration;
            }
        }));

        allConfigs.add(config);

        Project project = new Project();
        project.setId(config.getProjectId());
        project.setConfig(config);
        idToProject.put(project.getId(), project);

        return project;
    }
}
