package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.user.BrowseViewConfiguration;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateHierarchy;
import com.zutubi.tove.config.TemplateNodeImpl;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.*;
import org.mockito.Matchers;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

public class ProjectsModelsHelperTest extends ProjectsModelTestBase
{
    private static final String TEMPLATE_GLOBAL = "global";
    private static final String TEMPLATE_CHILD = "child";

    private static final String LABEL_ODD = "odd";
    private static final String LABEL_LONELY = "lonely";
    private static final String LABEL_STRANGE = "strange";

    private Project p1;
    private Project p2;
    private Project p3;
    private Project cp1;
    private Project cp2;
    private Project cp3;

    private Map<String, ProjectGroup> groups;

    private ProjectsModelsHelper helper;
    private BuildManager buildManager = mock(BuildManager.class);
    private ProjectManager projectManager = mock(ProjectManager.class);
    private ConfigurationTemplateManager configurationTemplateManager = mock(ConfigurationTemplateManager.class);

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        p1 = createProject("p1", LABEL_ODD, LABEL_LONELY);
        p2 = createProject("p2");
        p3 = createProject("p3", LABEL_ODD);
        cp1 = createProject("cp1", LABEL_ODD, LABEL_STRANGE);
        cp2 = createProject("cp2", LABEL_STRANGE);
        cp3 = createProject("cp3");

        // Hierarchy looks like:
        //
        // - global
        //   - template
        //     - cp1 (odd, strange)
        //     - cp2 (strange)
        //     - cp3 (strange)
        //   - p1 (odd, lonely)
        //   - p2
        //   - p3 (odd)
        TemplateNodeImpl globalNode = createNode(TEMPLATE_GLOBAL, false);
        TemplateNodeImpl templateNode = createNode(TEMPLATE_CHILD, false);
        globalNode.addChild(templateNode);

        templateNode.addChild(createNode(cp1.getName(), true));
        templateNode.addChild(createNode(cp2.getName(), true));
        templateNode.addChild(createNode(cp3.getName(), true));

        globalNode.addChild(createNode(p1.getName(), true));
        globalNode.addChild(createNode(p2.getName(), true));
        globalNode.addChild(createNode(p3.getName(), true));

        TemplateHierarchy hierarchy = new TemplateHierarchy(MasterConfigurationRegistry.PROJECTS_SCOPE, globalNode);
        stub(configurationTemplateManager.getTemplateHierarchy(MasterConfigurationRegistry.PROJECTS_SCOPE)).toReturn(hierarchy);
        
        final List<Project> allProjects = Arrays.asList(p1, p2, p3, cp1, cp2, cp3);
        groups = groupProjects(allProjects);

        stub(projectManager.getProjects(false)).toReturn(allProjects);
        stub(projectManager.getAllProjectGroups()).toReturn(groups.values());
        stub(projectManager.getProject(anyString(), eq(true))).toAnswer(new Answer<Object>()
        {
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable
            {
                return CollectionUtils.find(allProjects, new Predicate<Project>()
                {
                    public boolean satisfied(Project project)
                    {
                        return project.getName().equals(invocationOnMock.getArguments()[0]);
                    }
                });
            }
        });

        // We don't care about the builds, they can be tested elsewhere.
        stub(buildManager.queryBuilds((Project) anyObject(), eq(ResultState.getIncompleteStates()), eq(-1), eq(-1), eq(-1), anyInt(), eq(true), eq(false))).toReturn(Collections.<BuildResult>emptyList());
        stub(buildManager.getLatestBuildResultsForProject((Project) anyObject(), anyInt())).toReturn(Collections.<BuildResult>emptyList());

        AccessManager accessManager = mock(AccessManager.class);
        stub(accessManager.hasPermission(anyString(), anyObject())).toReturn(true);

        ActionManager actionManager = mock(ActionManager.class);
        stub(actionManager.getActions(Matchers.<Configuration>anyObject(), anyBoolean(), anyBoolean())).toReturn(Collections.<String>emptyList());

        config = new BrowseViewConfiguration();

        helper = new ProjectsModelsHelper();
        helper.setConfigurationTemplateManager(configurationTemplateManager);
        helper.setProjectManager(projectManager);
        helper.setBuildManager(buildManager);
        helper.setAccessManager(accessManager);
        helper.setActionManager(actionManager);
    }

    private TemplateNodeImpl createNode(String name, boolean concrete)
    {
        return new TemplateNodeImpl(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, name), name, concrete);
    }

    private Map<String, ProjectGroup> groupProjects(List<Project> allProjects)
    {
        Map<String, ProjectGroup> groups = new HashMap<String, ProjectGroup>();
        for (Project p: allProjects)
        {
            for (LabelConfiguration label: p.getConfig().getLabels())
            {
                String labelString = label.getLabel();
                ProjectGroup group = groups.get(labelString);
                if (group == null)
                {
                    group = new ProjectGroup(labelString);
                    groups.put(labelString, group);
                }

                group.add(p);
            }
        }
        return groups;
    }

    public void testNoHierarchy()
    {
        config.setHierarchyShown(false);

        assertProjectsModelLists(getAllFlatGroups(true), helper.createProjectsModels(null, config, urls, true));
    }

    public void testNoHierarchyNoUngrouped()
    {
        config.setHierarchyShown(false);

        assertProjectsModelLists(getAllFlatGroups(false), helper.createProjectsModels(null, config, urls, false));
    }

    public void testHierarchyFull()
    {
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(0);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createHierarchicalGroup(LABEL_LONELY, createTemplates(LABEL_LONELY, TEMPLATE_GLOBAL, p1)),
                createHierarchicalGroup(LABEL_ODD, createTemplates(LABEL_ODD, TEMPLATE_GLOBAL, createTemplates(LABEL_ODD, TEMPLATE_CHILD, cp1), p1, p3)),
                createHierarchicalGroup(LABEL_STRANGE, createTemplates(LABEL_STRANGE, TEMPLATE_GLOBAL, createTemplates(LABEL_STRANGE, TEMPLATE_CHILD, cp1, cp2))),
                createHierarchicalGroup(null, createTemplates(null, TEMPLATE_GLOBAL, createTemplates(null, TEMPLATE_CHILD, cp3), p2))
        );
        assertProjectsModelLists(expectedModels, helper.createProjectsModels(null, config, urls, true));
    }

    public void testHierarchyFullNoUngrouped()
    {
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(0);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createHierarchicalGroup(LABEL_LONELY, createTemplates(LABEL_LONELY, TEMPLATE_GLOBAL, p1)),
                createHierarchicalGroup(LABEL_ODD, createTemplates(LABEL_ODD, TEMPLATE_GLOBAL, createTemplates(LABEL_ODD, TEMPLATE_CHILD, cp1), p1, p3)),
                createHierarchicalGroup(LABEL_STRANGE, createTemplates(LABEL_STRANGE, TEMPLATE_GLOBAL, createTemplates(LABEL_STRANGE, TEMPLATE_CHILD, cp1, cp2)))
        );
        assertProjectsModelLists(expectedModels, helper.createProjectsModels(null, config, urls, false));
    }

    public void testHierarchyOneLevelHidden()
    {
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(1);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createHierarchicalGroup(LABEL_LONELY, p1),
                createHierarchicalGroup(LABEL_ODD, createTemplates(LABEL_ODD, TEMPLATE_CHILD, cp1), p1, p3),
                createHierarchicalGroup(LABEL_STRANGE, createTemplates(LABEL_STRANGE, TEMPLATE_CHILD, cp1, cp2)),
                createHierarchicalGroup(null, createTemplates(null, TEMPLATE_CHILD, cp3), p2)
        );
        assertProjectsModelLists(expectedModels, helper.createProjectsModels(null, config, urls, true));
    }

    public void testHierarchyOneLevelHiddenNoUngrouped()
    {
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(1);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createHierarchicalGroup(LABEL_LONELY, p1),
                createHierarchicalGroup(LABEL_ODD, createTemplates(LABEL_ODD, TEMPLATE_CHILD, cp1), p1, p3),
                createHierarchicalGroup(LABEL_STRANGE, createTemplates(LABEL_STRANGE, TEMPLATE_CHILD, cp1, cp2))
        );
        assertProjectsModelLists(expectedModels, helper.createProjectsModels(null, config, urls, false));
    }
    
    public void testHierarchyTwoLevelsHidden()
    {
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(2);

        assertProjectsModelLists(getAllFlatGroups(true), helper.createProjectsModels(null, config, urls, true));
    }

    public void testHierarchyThreeLevelsHidden()
    {
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(3);

        assertProjectsModelLists(getAllFlatGroups(true), helper.createProjectsModels(null, config, urls, true));
    }

    public void testFilterOutAllGroupsNoHierarchy()
    {
        config.setHierarchyShown(false);

        List<ProjectsModel> expectedModels = Arrays.asList(createFlatGroup(null, cp1, cp2, cp3, p1, p2, p3));
        assertProjectsModelLists(expectedModels, helper.createProjectsModels(null, config, Collections.<LabelProjectTuple>emptySet(), urls, new TruePredicate<Project>(), new FalsePredicate<ProjectGroup>(), true));
    }

    public void testFilterOutAllGroupsNoUngroupedNoHierarchy()
    {
        config.setHierarchyShown(false);

        assertProjectsModelLists(Collections.<ProjectsModel>emptyList(), helper.createProjectsModels(null, config, Collections.<LabelProjectTuple>emptySet(), urls, new TruePredicate<Project>(), new FalsePredicate<ProjectGroup>(), false));
    }

    public void testFilterOutAllGroupsHierarchyFull()
    {
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(0);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createHierarchicalGroup(null, createTemplates(null, TEMPLATE_GLOBAL, createTemplates(null, TEMPLATE_CHILD, cp1, cp2, cp3), p1, p2, p3))
        );
        assertProjectsModelLists(expectedModels, helper.createProjectsModels(null, config, Collections.<LabelProjectTuple>emptySet(), urls, new TruePredicate<Project>(), new FalsePredicate<ProjectGroup>(), true));
    }

    public void testFilterOutAllGroupsNoUngroupedHierarchyFull()
    {
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(0);

        assertProjectsModelLists(Collections.<ProjectsModel>emptyList(), helper.createProjectsModels(null, config, Collections.<LabelProjectTuple>emptySet(), urls, new TruePredicate<Project>(), new FalsePredicate<ProjectGroup>(), false));
    }

    public void testFilterOutAllGroupsHierarchyOneLevelHidden()
    {
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(1);

        List<ProjectsModel> expectedModels = Arrays.asList(createHierarchicalGroup(null, createTemplates(null, TEMPLATE_CHILD, cp1, cp2, cp3), p1, p2, p3));
        assertProjectsModelLists(expectedModels, helper.createProjectsModels(null, config, Collections.<LabelProjectTuple>emptySet(), urls, new TruePredicate<Project>(), new FalsePredicate<ProjectGroup>(), true));
    }

    public void testFilterOutAllGroupsNoUngroupedHierarchyOneLevelHidden()
    {
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(1);

        assertProjectsModelLists(Collections.<ProjectsModel>emptyList(), helper.createProjectsModels(null, config, Collections.<LabelProjectTuple>emptySet(), urls, new TruePredicate<Project>(), new FalsePredicate<ProjectGroup>(), false));
    }

    public void testFilterOutSpecificGroupsNoHierarchy()
    {
        config.setHierarchyShown(false);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createFlatGroup(LABEL_LONELY, p1),
                createFlatGroup(LABEL_STRANGE, cp1, cp2),
                createFlatGroup(null, cp3, p2, p3)
        );

        List<ProjectsModel> gotModels = helper.createProjectsModels(null, config, Collections.<LabelProjectTuple>emptySet(), urls, new TruePredicate<Project>(), new InCollectionPredicate<ProjectGroup>(groups.get(LABEL_LONELY), groups.get(LABEL_STRANGE)), true);
        assertProjectsModelLists(expectedModels, gotModels);
    }

    public void testFilterOutSpecificGroupsNoUngroupedNoHierarchy()
    {
        config.setHierarchyShown(false);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createFlatGroup(LABEL_LONELY, p1),
                createFlatGroup(LABEL_STRANGE, cp1, cp2)
        );

        List<ProjectsModel> gotModels = helper.createProjectsModels(null, config, Collections.<LabelProjectTuple>emptySet(), urls, new TruePredicate<Project>(), new InCollectionPredicate<ProjectGroup>(groups.get(LABEL_LONELY), groups.get(LABEL_STRANGE)), false);
        assertProjectsModelLists(expectedModels, gotModels);
    }

    public void testFilterOutSpecificGroupsHierarchyOneLevelHidden()
    {
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(1);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createHierarchicalGroup(LABEL_LONELY, p1),
                createHierarchicalGroup(LABEL_STRANGE, createTemplates(LABEL_STRANGE, TEMPLATE_CHILD, cp1, cp2)),
                createHierarchicalGroup(null, createTemplates(null, TEMPLATE_CHILD, cp3), p2, p3)
        );

        List<ProjectsModel> gotModels = helper.createProjectsModels(null, config, Collections.<LabelProjectTuple>emptySet(), urls, new TruePredicate<Project>(), new InCollectionPredicate<ProjectGroup>(groups.get(LABEL_LONELY), groups.get(LABEL_STRANGE)), true);
        assertProjectsModelLists(expectedModels, gotModels);
    }

    public void testFilterOutSpecificGroupsNoUngroupedHierarchyOneLevelHidden()
    {
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(1);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createHierarchicalGroup(LABEL_LONELY, p1),
                createHierarchicalGroup(LABEL_STRANGE, createTemplates(LABEL_STRANGE, TEMPLATE_CHILD, cp1, cp2))
        );

        List<ProjectsModel> gotModels = helper.createProjectsModels(null, config, Collections.<LabelProjectTuple>emptySet(), urls, new TruePredicate<Project>(), new InCollectionPredicate<ProjectGroup>(groups.get(LABEL_LONELY), groups.get(LABEL_STRANGE)), false);
        assertProjectsModelLists(expectedModels, gotModels);
    }
    
    public void testFilterOutAllProjectsNoHierarchy()
    {
        config.setHierarchyShown(false);

        assertProjectsModelLists(Collections.<ProjectsModel>emptyList(), helper.createProjectsModels(null, config, Collections.<LabelProjectTuple>emptySet(), urls, new FalsePredicate<Project>(), new TruePredicate<ProjectGroup>(), true));
    }

    public void testFilterOutAllProjectsHierarchyFull()
    {
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(0);

        assertProjectsModelLists(Collections.<ProjectsModel>emptyList(), helper.createProjectsModels(null, config, Collections.<LabelProjectTuple>emptySet(), urls, new FalsePredicate<Project>(), new TruePredicate<ProjectGroup>(), true));
    }

    public void testFilterOutSpecificProjectsNoHierarchy()
    {
        config.setHierarchyShown(false);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createFlatGroup(LABEL_ODD, cp1, p3),
                createFlatGroup(LABEL_STRANGE, cp1, cp2),
                createFlatGroup(null, p2)
        );

        assertProjectsModelLists(expectedModels, helper.createProjectsModels(null, config, Collections.<LabelProjectTuple>emptySet(), urls, new InCollectionPredicate<Project>(p2, p3, cp1, cp2), new TruePredicate<ProjectGroup>(), true));
    }

    public void testFilterOutSpecificProjectsHierarchyOneLevelHidden()
    {
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(1);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createHierarchicalGroup(LABEL_ODD, createTemplates(LABEL_ODD, TEMPLATE_CHILD, cp1), p3),
                createHierarchicalGroup(LABEL_STRANGE, createTemplates(LABEL_STRANGE, TEMPLATE_CHILD, cp1, cp2)),
                createHierarchicalGroup(null, p2)
        );

        assertProjectsModelLists(expectedModels, helper.createProjectsModels(null, config, Collections.<LabelProjectTuple>emptySet(), urls, new InCollectionPredicate<Project>(p2, p3, cp1, cp2), new TruePredicate<ProjectGroup>(), true));
    }

    public void testFilterOutSpecificProjectsAndGroupsNoHierarchy()
    {
        config.setHierarchyShown(false);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createFlatGroup(LABEL_ODD, cp1, p3),
                createFlatGroup(null, cp2, p2)
        );

        assertProjectsModelLists(expectedModels, helper.createProjectsModels(null, config, Collections.<LabelProjectTuple>emptySet(), urls, new InCollectionPredicate<Project>(p2, p3, cp1, cp2), new InCollectionPredicate<ProjectGroup>(groups.get(LABEL_ODD)), true));
    }

    public void testBuildCountApplied()
    {
        config.setBuildsPerProject(3);

        helper.createProjectsModels(null, config, Collections.<LabelProjectTuple>emptySet(), urls, new InCollectionPredicate<Project>(p1), new InCollectionPredicate<ProjectGroup>(groups.get(LABEL_LONELY)), true);
        verify(buildManager).queryBuilds(p1, ResultState.getIncompleteStates(), -1, -1, -1, 3, true, false);
        verify(buildManager).getLatestBuildResultsForProject(p1, 3);
        verify(buildManager).getLatestCompletedBuildResult(p1);
        verifyNoMoreInteractions(buildManager);
    }

    public void testLabellingOfUngroupedSomeGroups()
    {
        List<ProjectsModel> projectsModels = helper.createProjectsModels(null, config, urls, true);
        // Upgrouped projects come last.
        ProjectsModel ungroup = projectsModels.get(projectsModels.size() - 1);
        assertFalse(ungroup.isLabelled());
        assertEquals("ungrouped projects", ungroup.getGroupName());
    }

    public void testLabellingOfUngroupedNoGroups()
    {
        List<ProjectsModel> projectsModels = helper.createProjectsModels(null, config, Collections.<LabelProjectTuple>emptySet(), urls, new TruePredicate<Project>(), new FalsePredicate<ProjectGroup>(), true);

        // When there are no other groups, don't use the "ungrouped" term
        ProjectsModel ungroup = projectsModels.get(projectsModels.size() - 1);
        assertFalse(ungroup.isLabelled());
        assertEquals("projects", ungroup.getGroupName());
    }

    private BuildResult createBuild(Project project, int number)
    {
        return new BuildResult(new ManualTriggerBuildReason(), project, number, false);
    }

    private List<ProjectsModel> getAllFlatGroups(boolean includeUngrouped)
    {
        List<ProjectsModel> flatGroups = new LinkedList<ProjectsModel>(Arrays.asList(
            createFlatGroup(LABEL_LONELY, p1),
            createFlatGroup(LABEL_ODD, cp1, p1, p3),
            createFlatGroup(LABEL_STRANGE, cp1, cp2)
        ));

        if (includeUngrouped)
        {
            flatGroups.add(createFlatGroup(null, cp3, p2));
        }

        return flatGroups;
    }
}
