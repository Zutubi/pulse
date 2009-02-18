package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.user.BrowseViewConfiguration;
import com.zutubi.pulse.master.tove.config.user.ProjectsSummaryConfiguration;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateHierarchy;
import com.zutubi.tove.config.TemplateNode;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.*;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

public class ProjectsModelsHelperTest extends PulseTestCase
{
    private static final String TEMPLATE_GLOBAL = "global";
    private static final String TEMPLATE_CHILD = "child";

    private static final String LABEL_ODD = "odd";
    private static final String LABEL_LONELY = "lonely";
    private static final String LABEL_STRANGE = "strange";

    private long nextId = 1;

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
        TemplateNode globalNode = createNode(TEMPLATE_GLOBAL, false);
        TemplateNode templateNode = createNode(TEMPLATE_CHILD, false);
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
        stub(buildManager.getLatestBuildResultsForProject((Project) anyObject(), anyInt())).toReturn(Collections.<BuildResult>emptyList());

        helper = new ProjectsModelsHelper();
        helper.setConfigurationTemplateManager(configurationTemplateManager);
        helper.setProjectManager(projectManager);
        helper.setBuildManager(buildManager);
    }

    private Project createProject(String name, String... labels)
    {
        ProjectConfiguration configuration = new ProjectConfiguration();
        configuration.setName(name);
        configuration.setLabels(CollectionUtils.map(labels, new Mapping<String, LabelConfiguration>()
        {
            public LabelConfiguration map(String s)
            {
                LabelConfiguration label = new LabelConfiguration();
                label.setLabel(s);
                return label;
            }
        }));

        Project project = new Project();
        project.setId(nextId++);
        project.setConfig(configuration);
        return project;
    }

    private TemplateNode createNode(String name, boolean concrete)
    {
        return new TemplateNode(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, name), name, concrete);
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
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(false);

        assertProjectsModelLists(getAllFlatGroups(true), helper.createProjectsModels(config, true));
    }

    public void testNoHierarchyNoUngrouped()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(false);

        assertProjectsModelLists(getAllFlatGroups(false), helper.createProjectsModels(config, false));
    }

    public void testHierarchyFull()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(0);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createHierarchicalGroup(LABEL_LONELY, TEMPLATE_GLOBAL, p1),
                createHierarchicalGroup(LABEL_ODD, TEMPLATE_GLOBAL, p1, p3, TEMPLATE_CHILD, cp1),
                createHierarchicalGroup(LABEL_STRANGE, TEMPLATE_GLOBAL, TEMPLATE_CHILD, cp1, cp2),
                createHierarchicalGroup(null, TEMPLATE_GLOBAL, p2, TEMPLATE_CHILD, cp3)
        );
        assertProjectsModelLists(expectedModels, helper.createProjectsModels(config, true));
    }

    public void testHierarchyFullNoUngrouped()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(0);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createHierarchicalGroup(LABEL_LONELY, TEMPLATE_GLOBAL, p1),
                createHierarchicalGroup(LABEL_ODD, TEMPLATE_GLOBAL, p1, p3, TEMPLATE_CHILD, cp1),
                createHierarchicalGroup(LABEL_STRANGE, TEMPLATE_GLOBAL, TEMPLATE_CHILD, cp1, cp2)
        );
        assertProjectsModelLists(expectedModels, helper.createProjectsModels(config, false));
    }

    public void testHierarchyOneLevelHidden()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(1);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createHierarchicalGroup(LABEL_LONELY, p1),
                createHierarchicalGroup(LABEL_ODD, p1, p3, TEMPLATE_CHILD, cp1),
                createHierarchicalGroup(LABEL_STRANGE, TEMPLATE_CHILD, cp1, cp2),
                createHierarchicalGroup(null, p2, TEMPLATE_CHILD, cp3)
        );
        assertProjectsModelLists(expectedModels, helper.createProjectsModels(config, true));
    }

    public void testHierarchyOneLevelHiddenNoUngrouped()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(1);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createHierarchicalGroup(LABEL_LONELY, p1),
                createHierarchicalGroup(LABEL_ODD, p1, p3, TEMPLATE_CHILD, cp1),
                createHierarchicalGroup(LABEL_STRANGE, TEMPLATE_CHILD, cp1, cp2)
        );
        assertProjectsModelLists(expectedModels, helper.createProjectsModels(config, false));
    }
    
    public void testHierarchyTwoLevelsHidden()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(2);

        assertProjectsModelLists(getAllFlatGroups(true), helper.createProjectsModels(config, true));
    }

    public void testHierarchyThreeLevelsHidden()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(3);

        assertProjectsModelLists(getAllFlatGroups(true), helper.createProjectsModels(config, true));
    }

    public void testFilterOutAllGroupsNoHierarchy()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(false);

        List<ProjectsModel> expectedModels = Arrays.asList(createFlatGroup(null, cp1, cp2, cp3, p1, p2, p3));
        assertProjectsModelLists(expectedModels, helper.createProjectsModels(config, new TruePredicate<Project>(), new FalsePredicate<ProjectGroup>(), true));
    }

    public void testFilterOutAllGroupsNoUngroupedNoHierarchy()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(false);

        assertProjectsModelLists(Collections.<ProjectsModel>emptyList(), helper.createProjectsModels(config, new TruePredicate<Project>(), new FalsePredicate<ProjectGroup>(), false));
    }

    public void testFilterOutAllGroupsHierarchyFull()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(0);

        List<ProjectsModel> expectedModels = Arrays.asList(createHierarchicalGroup(null, TEMPLATE_GLOBAL, p1, p2, p3, TEMPLATE_CHILD, cp1, cp2, cp3));
        assertProjectsModelLists(expectedModels, helper.createProjectsModels(config, new TruePredicate<Project>(), new FalsePredicate<ProjectGroup>(), true));
    }

    public void testFilterOutAllGroupsNoUngroupedHierarchyFull()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(0);

        assertProjectsModelLists(Collections.<ProjectsModel>emptyList(), helper.createProjectsModels(config, new TruePredicate<Project>(), new FalsePredicate<ProjectGroup>(), false));
    }

    public void testFilterOutAllGroupsHierarchyOneLevelHidden()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(1);

        List<ProjectsModel> expectedModels = Arrays.asList(createHierarchicalGroup(null, p1, p2, p3, TEMPLATE_CHILD, cp1, cp2, cp3));
        assertProjectsModelLists(expectedModels, helper.createProjectsModels(config, new TruePredicate<Project>(), new FalsePredicate<ProjectGroup>(), true));
    }

    public void testFilterOutAllGroupsNoUngroupedHierarchyOneLevelHidden()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(1);

        assertProjectsModelLists(Collections.<ProjectsModel>emptyList(), helper.createProjectsModels(config, new TruePredicate<Project>(), new FalsePredicate<ProjectGroup>(), false));
    }

    public void testFilterOutSpecificGroupsNoHierarchy()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(false);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createFlatGroup(LABEL_LONELY, p1),
                createFlatGroup(LABEL_STRANGE, cp1, cp2),
                createFlatGroup(null, cp3, p2, p3)
        );

        List<ProjectsModel> gotModels = helper.createProjectsModels(config, new TruePredicate<Project>(), new InCollectionPredicate<ProjectGroup>(groups.get(LABEL_LONELY), groups.get(LABEL_STRANGE)), true);
        assertProjectsModelLists(expectedModels, gotModels);
    }

    public void testFilterOutSpecificGroupsNoUngroupedNoHierarchy()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(false);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createFlatGroup(LABEL_LONELY, p1),
                createFlatGroup(LABEL_STRANGE, cp1, cp2)
        );

        List<ProjectsModel> gotModels = helper.createProjectsModels(config, new TruePredicate<Project>(), new InCollectionPredicate<ProjectGroup>(groups.get(LABEL_LONELY), groups.get(LABEL_STRANGE)), false);
        assertProjectsModelLists(expectedModels, gotModels);
    }

    public void testFilterOutSpecificGroupsHierarchyOneLevelHidden()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(1);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createHierarchicalGroup(LABEL_LONELY, p1),
                createHierarchicalGroup(LABEL_STRANGE, TEMPLATE_CHILD, cp1, cp2),
                createHierarchicalGroup(null, p2, p3, TEMPLATE_CHILD, cp3)
        );

        List<ProjectsModel> gotModels = helper.createProjectsModels(config, new TruePredicate<Project>(), new InCollectionPredicate<ProjectGroup>(groups.get(LABEL_LONELY), groups.get(LABEL_STRANGE)), true);
        assertProjectsModelLists(expectedModels, gotModels);
    }

    public void testFilterOutSpecificGroupsNoUngroupedHierarchyOneLevelHidden()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(1);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createHierarchicalGroup(LABEL_LONELY, p1),
                createHierarchicalGroup(LABEL_STRANGE, TEMPLATE_CHILD, cp1, cp2)
        );

        List<ProjectsModel> gotModels = helper.createProjectsModels(config, new TruePredicate<Project>(), new InCollectionPredicate<ProjectGroup>(groups.get(LABEL_LONELY), groups.get(LABEL_STRANGE)), false);
        assertProjectsModelLists(expectedModels, gotModels);
    }
    
    public void testFilterOutAllProjectsNoHierarchy()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(false);

        assertProjectsModelLists(Collections.<ProjectsModel>emptyList(), helper.createProjectsModels(config, new FalsePredicate<Project>(), new TruePredicate<ProjectGroup>(), true));
    }

    public void testFilterOutAllProjectsHierarchyFull()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(0);

        assertProjectsModelLists(Collections.<ProjectsModel>emptyList(), helper.createProjectsModels(config, new FalsePredicate<Project>(), new TruePredicate<ProjectGroup>(), true));
    }

    public void testFilterOutSpecificProjectsNoHierarchy()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(false);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createFlatGroup(LABEL_ODD, cp1, p3),
                createFlatGroup(LABEL_STRANGE, cp1, cp2),
                createFlatGroup(null, p2)
        );

        assertProjectsModelLists(expectedModels, helper.createProjectsModels(config, new InCollectionPredicate<Project>(p2, p3, cp1, cp2), new TruePredicate<ProjectGroup>(), true));
    }

    public void testFilterOutSpecificProjectsHierarchyOneLevelHidden()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(true);
        config.setHiddenHierarchyLevels(1);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createHierarchicalGroup(LABEL_ODD, p3, TEMPLATE_CHILD, cp1),
                createHierarchicalGroup(LABEL_STRANGE, TEMPLATE_CHILD, cp1, cp2),
                createHierarchicalGroup(null, p2)
        );

        assertProjectsModelLists(expectedModels, helper.createProjectsModels(config, new InCollectionPredicate<Project>(p2, p3, cp1, cp2), new TruePredicate<ProjectGroup>(), true));
    }

    public void testFilterOutSpecificProjectsAndGroupsNoHierarchy()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setHierarchyShown(false);

        List<ProjectsModel> expectedModels = Arrays.asList(
                createFlatGroup(LABEL_ODD, cp1, p3),
                createFlatGroup(null, cp2, p2)
        );

        assertProjectsModelLists(expectedModels, helper.createProjectsModels(config, new InCollectionPredicate<Project>(p2, p3, cp1, cp2), new InCollectionPredicate<ProjectGroup>(groups.get(LABEL_ODD)), true));
    }

    public void testBuildCountApplied()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setBuildsPerProject(3);
        
        helper.createProjectsModels(config, new InCollectionPredicate<Project>(p1), new InCollectionPredicate<ProjectGroup>(groups.get(LABEL_LONELY)), true);
        verify(buildManager).getLatestBuildResultsForProject(p1, 3);
        verifyNoMoreInteractions(buildManager);
    }

    public void testAtLeastTwoBuildsRetrieved()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        config.setBuildsPerProject(1);
        
        helper.createProjectsModels(config, new InCollectionPredicate<Project>(p1), new InCollectionPredicate<ProjectGroup>(groups.get(LABEL_LONELY)), true);
        verify(buildManager).getLatestBuildResultsForProject(p1, 2);
        verifyNoMoreInteractions(buildManager);
    }

    public void testLabellingOfUngroupedSomeGroups()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        List<ProjectsModel> projectsModels = helper.createProjectsModels(config, true);
        // Upgrouped projects come last.
        ProjectsModel ungroup = projectsModels.get(projectsModels.size() - 1);
        assertFalse(ungroup.isLabelled());
        assertEquals("ungrouped projects", ungroup.getGroupName());
    }

    public void testLabellingOfUngroupedNoGroups()
    {
        ProjectsSummaryConfiguration config = new BrowseViewConfiguration();
        List<ProjectsModel> projectsModels = helper.createProjectsModels(config, new TruePredicate<Project>(), new FalsePredicate<ProjectGroup>(), true);

        // When there are no other groups, don't use the "ungrouped" term
        ProjectsModel ungroup = projectsModels.get(projectsModels.size() - 1);
        assertFalse(ungroup.isLabelled());
        assertEquals("projects", ungroup.getGroupName());
    }

    private void assertProjectsModelLists(List<ProjectsModel> expectedModels, List<ProjectsModel> gotModels)
    {
        assertEquals(expectedModels.size(), gotModels.size());
        for (int i = 0; i < expectedModels.size(); i++)
        {
            assertProjectsModels(expectedModels.get(i), gotModels.get(i));
        }
    }

    private void assertProjectsModels(ProjectsModel expected, ProjectsModel got)
    {
        assertEquals(expected.isLabelled(), got.isLabelled());
        if (expected.isLabelled())
        {
            assertEquals(expected.getGroupName(), got.getGroupName());
        }

        assertTemplateModels(expected.getRoot(), got.getRoot());
    }

    private void assertModels(ProjectModel expected, ProjectModel got)
    {
        assertEquals(expected.getName(), got.getName());
        assertSame(expected.getClass(), got.getClass());

        if (expected instanceof TemplateProjectModel)
        {
            assertTemplateModels((TemplateProjectModel) expected, (TemplateProjectModel) got);
        }
        else
        {
            assertConcreteModels((ConcreteProjectModel) expected, (ConcreteProjectModel) got);
        }
    }

    private void assertTemplateModels(TemplateProjectModel expected, TemplateProjectModel got)
    {
        List<ProjectModel> expectedChildren = expected.getChildren();
        List<ProjectModel> gotChildren = got.getChildren();
        assertEquals(expectedChildren.size(), gotChildren.size());
        for (int i = 0; i < expectedChildren.size(); i++)
        {
            assertModels(expectedChildren.get(i), gotChildren.get(i));
        }
    }

    private void assertConcreteModels(ConcreteProjectModel expected, ConcreteProjectModel got)
    {
        assertSame(expected.getProject(), got.getProject());
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

    /**
     * A simple way to create a hierarchical group when templates have only one
     * child that is also a template.  The group is created, then all members
     * are added by checking if they are a template name (String) or a concrete
     * Project.  If the former, the template is added and subsequent members
     * will nest under that template.  If the latter, the Project is simply
     * added to the latest seen template.
     *
     * @param label   label or name of the group
     * @param members list of template names and projects ordered from root to
     *                leaf of the desired hierarchy
     * @return the hierarchical group model
     */
    private ProjectsModel createHierarchicalGroup(String label, Object... members)
    {
        ProjectsModel group = createGroup(label);
        TemplateProjectModel node = group.getRoot();
        for (Object member: members)
        {
            if (member instanceof String)
            {
                // Name of template.  Add it and then ensure future members
                // nest under it.
                TemplateProjectModel child = new TemplateProjectModel(group, (String) member);
                node.addChild(child);
                node = child;
            }
            else
            {
                node.addChild(createConcrete(group, (Project) member));
            }
        }

        return group;
    }

    private ProjectsModel createFlatGroup(String label, Project... projects)
    {
        ProjectsModel group = createGroup(label);
        for (Project p: projects)
        {
            group.getRoot().addChild(createConcrete(group, p));
        }
        return group;
    }

    private ProjectsModel createGroup(String label)
    {
        return new ProjectsModel(label, label != null);
    }

    private ConcreteProjectModel createConcrete(ProjectsModel group, Project project)
    {
        return new ConcreteProjectModel(group, project, Collections.<BuildResult>emptyList(), 0);
    }
}
