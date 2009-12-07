package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.EXTENSION_PROJECT_TRIGGERS;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.TreeNode;
import com.zutubi.util.junit.ZutubiTestCase;

import java.util.*;

public abstract class BaseGraphTestCase extends ZutubiTestCase
{
    private long nextId = 1;
    protected ProjectManager projectManager;
    protected List<ProjectConfiguration> allConfigs = new LinkedList<ProjectConfiguration>();
    protected Map<Long, Project> idToProject = new HashMap<Long, Project>();

    protected void assertEquals(TreeNode<BuildGraphData> expected, TreeNode<BuildGraphData> got)
    {
        assertEquals(expected.getData().getProject(), got.getData().getProject());

        List<TreeNode<BuildGraphData>> expectedChildren = expected.getChildren();
        List<TreeNode<BuildGraphData>> gotChildren = got.getChildren();
        assertEquals(expectedChildren.size(), gotChildren.size());

        // Order of children does not matter.(
        for (TreeNode<BuildGraphData> expectedChild : expectedChildren)
        {
            final BuildGraphData childData = expectedChild.getData();
            TreeNode<BuildGraphData> gotChild = CollectionUtils.find(gotChildren, new Predicate<TreeNode<BuildGraphData>>()
            {
                public boolean satisfied(TreeNode<BuildGraphData> node)
                {
                    return node.getData().getProject().equals(childData.getProject());
                }
            });

            assertNotNull(gotChild);
            assertEquals(expectedChild, gotChild);
        }
    }

    protected TreeNode<BuildGraphData> node(Project project, TreeNode<BuildGraphData>... children)
    {
        TreeNode<BuildGraphData> node = new TreeNode<BuildGraphData>(new BuildGraphData(project));
        node.addAll(java.util.Arrays.asList(children));
        return node;
    }

    protected TreeNode<BuildGraphData> node(Project project, DependencyConfiguration dependency, TreeNode<BuildGraphData>... children)
    {
        BuildGraphData data = new BuildGraphData(project);
        data.setDependency(dependency);
        TreeNode<BuildGraphData> node = new TreeNode<BuildGraphData>(data);
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
