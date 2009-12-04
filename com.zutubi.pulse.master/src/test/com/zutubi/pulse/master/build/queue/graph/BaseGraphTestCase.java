package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.util.TreeNode;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.EXTENSION_PROJECT_TRIGGERS;

import java.util.*;

public class BaseGraphTestCase extends ZutubiTestCase
{
    private long nextId = 1;
    protected ProjectManager projectManager;
    protected List<ProjectConfiguration> allConfigs = new LinkedList<ProjectConfiguration>();
    protected Map<Long, Project> idToProject = new HashMap<Long, Project>();

    protected void assertEquals(TreeNode<GraphData> expected, TreeNode<GraphData> got)
    {
        assertEquals(expected.getData().getProject(), got.getData().getProject());

        List<TreeNode<GraphData>> expectedChildren = expected.getChildren();
        List<TreeNode<GraphData>> gotChildren = got.getChildren();
        assertEquals(expectedChildren.size(), gotChildren.size());

        // Order of children does not matter.(
        for (TreeNode<GraphData> expectedChild : expectedChildren)
        {
            final GraphData childData = expectedChild.getData();
            TreeNode<GraphData> gotChild = CollectionUtils.find(gotChildren, new Predicate<TreeNode<GraphData>>()
            {
                public boolean satisfied(TreeNode<GraphData> node)
                {
                    return node.getData().getProject().equals(childData.getProject());
                }
            });

            assertNotNull(gotChild);
            assertEquals(expectedChild, gotChild);
        }
    }

    protected TreeNode<GraphData> node(Project project, TreeNode<GraphData>... children)
    {
        TreeNode<GraphData> node = new TreeNode<GraphData>(new GraphData(project));
        node.addAll(java.util.Arrays.asList(children));
        return node;
    }

    protected TreeNode<GraphData> node(Project project, DependencyConfiguration dependency, TreeNode<GraphData>... children)
    {
        GraphData data = new GraphData(project);
        data.setDependency(dependency);
        TreeNode<GraphData> node = new TreeNode<GraphData>(data);
        node.addAll(java.util.Arrays.asList(children));
        return node;
    }

    protected DependencyConfiguration dependency(Project project, boolean transitive)
    {
        DependencyConfiguration dependency = dependency(project);
        dependency.setTransitive(transitive);
        return dependency;
    }

    protected DependencyConfiguration dependency(Project project, String revision)
    {
        DependencyConfiguration dependency = dependency(project);
        dependency.setRevision(revision);
        return dependency;
    }

    protected DependencyConfiguration dependency(Project project)
    {
        DependencyConfiguration dependencyConfiguration = new DependencyConfiguration();
        dependencyConfiguration.setProject(project.getConfig());
        return dependencyConfiguration;
    }

    protected Project project(String name, DependencyConfiguration... dependencies)
    {
        ProjectConfiguration config = new ProjectConfiguration(name);
        config.setHandle(nextId++);
        config.setProjectId(nextId++);

        config.getDependencies().getDependencies().addAll(Arrays.asList(dependencies));

        Project project = new Project();
        project.setId(config.getProjectId());
        project.setConfig(config);

        DependentBuildTriggerConfiguration triggerConfiguration = new DependentBuildTriggerConfiguration();
        triggerConfiguration.setTriggerId(project.getId());
        HashMap<String, Object> triggers = new HashMap<String, Object>();
        triggers.put("dependent trigger", triggerConfiguration);
        config.addExtension(EXTENSION_PROJECT_TRIGGERS, triggers);

        allConfigs.add(config);
        idToProject.put(project.getId(), project);

        return project;
    }

    protected void removeTriggers(Project project)
    {
        Map<String, Object> triggers = (Map<String, Object>) project.getConfig().getExtensions().get(EXTENSION_PROJECT_TRIGGERS);
        triggers.clear();
    }
}
