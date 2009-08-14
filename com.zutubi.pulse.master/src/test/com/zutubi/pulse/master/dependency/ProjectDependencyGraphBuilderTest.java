package com.zutubi.pulse.master.dependency;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.servercore.events.system.SystemStartedEvent;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.util.TreeNode;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ProjectDependencyGraphBuilderTest extends PulseTestCase
{
    private long nextId = 0;
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
        stub(projectManager.mapConfigsToProjects(anyList())).toAnswer(new Answer<List<Project>>()
        {
            public List<Project> answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                List<ProjectConfiguration> configs = (List<ProjectConfiguration>) invocationOnMock.getArguments()[0];
                return CollectionUtils.map(configs, new Mapping<ProjectConfiguration, Project>()
                {
                    public Project map(ProjectConfiguration projectConfiguration)
                    {
                        return idToProject.get(projectConfiguration.getProjectId());
                    }
                });
            }
        });

        builder.setProjectManager(projectManager);

        EventManager eventManager = new DefaultEventManager();
        builder.setEventManager(eventManager);
        eventManager.publish(new SystemStartedEvent(this));
    }

    public void testUtilLibrary()
    {
        ProjectDependencyGraph graph = builder.build(utilProject);
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

    public void testServerModule()
    {
        ProjectDependencyGraph graph = builder.build(serverProject);
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

    public void testMasterModule()
    {
        ProjectDependencyGraph graph = builder.build(masterProject);
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

    private void assertEquals(TreeNode<Project> expected, TreeNode<Project> got)
    {
        assertSame(expected.getData(), got.getData());

        List<TreeNode<Project>> expectedChildren = expected.getChildren();
        List<TreeNode<Project>> gotChildren = got.getChildren();
        assertEquals(expectedChildren.size(), gotChildren.size());

        // Order of children does not matter.
        for (TreeNode<Project> expectedChild: expectedChildren)
        {
            final Project childProject = expectedChild.getData();
            TreeNode<Project> gotChild = CollectionUtils.find(gotChildren, new Predicate<TreeNode<Project>>()
            {
                public boolean satisfied(TreeNode<Project> node)
                {
                    return node.getData() == childProject;
                }
            });

            assertNotNull(gotChild);
            assertEquals(expectedChild, gotChild);
        }
    }

    private TreeNode<Project> node(Project data, TreeNode<Project>... children)
    {
        TreeNode<Project> node = new TreeNode<Project>(data);
        node.addAll(asList(children));
        return node;
    }

    private Project makeProject(String name, Project... dependencies)
    {
        ProjectConfiguration config = new ProjectConfiguration(name);
        config.setProjectId(nextId++);

        config.getDependencies().getDependencies().addAll(CollectionUtils.map(dependencies, new Mapping<Project, DependencyConfiguration>()
        {
            public DependencyConfiguration map(Project project)
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
