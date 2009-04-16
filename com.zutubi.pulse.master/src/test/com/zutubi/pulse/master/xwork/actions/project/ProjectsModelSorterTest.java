package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.Sort;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ProjectsModelSorterTest extends PulseTestCase
{
    private ProjectsModelSorter sorter;

    protected void setUp() throws Exception
    {
        super.setUp();

        sorter = new ProjectsModelSorter();
    }

    public void testLexicalSorting()
    {
        List<ProjectsModel> flatGroups = new LinkedList<ProjectsModel>(Arrays.asList(
                createFlatGroup("z", "cp2", "cp1"),
                createFlatGroup("a", "p1"),
                createFlatGroup("b", "p3", "p1", "cp1"),
                createFlatGroup(null, "p2", "cp3")
        ));
        sorter.sort(flatGroups);

        List<ProjectsModel> expectedSorting = new LinkedList<ProjectsModel>(Arrays.asList(
                createFlatGroup("a", "p1"),
                createFlatGroup("b", "cp1", "p1", "p3"),
                createFlatGroup("z", "cp1", "cp2"),
                createFlatGroup(null, "cp3", "p2")
        ));

        assertProjectsModelLists(expectedSorting, flatGroups);
    }

    public void testNestedSorting()
    {
        List<ProjectsModel> groups = new LinkedList<ProjectsModel>(Arrays.asList(
                createHierarchicalGroup("z", "cp2", createTemplates("a", "y", "c"), "b")
        ));
        sorter.sort(groups);

        List<ProjectsModel> expectedSorting = new LinkedList<ProjectsModel>(Arrays.asList(
                createHierarchicalGroup("z", "b", "cp2", createTemplates("a", "c", "y"))
        ));

        assertProjectsModelLists(expectedSorting, groups);
    }

    public void testGroupSorting()
    {
        List<ProjectsModel> groups = new LinkedList<ProjectsModel>(Arrays.asList(
                createFlatGroup("z", "z"),
                createFlatGroup("a", "a"),
                createFlatGroup("b", "b")
        ));
        sorter.sort(groups);

        List<ProjectsModel> expectedSorting = new LinkedList<ProjectsModel>(Arrays.asList(
                createFlatGroup("a", "a"),
                createFlatGroup("b", "b"),
                createFlatGroup("z", "z")
        ));

        assertProjectsModelLists(expectedSorting, groups);
    }

    public void testCustomSorting()
    {
        sorter.setProjectNameComparator(new InverseStringComparator());
        sorter.setGroupNameComparator(new InverseStringComparator());
        
        List<ProjectsModel> flatGroups = new LinkedList<ProjectsModel>(Arrays.asList(
                createFlatGroup("z", "cp2", "cp1"),
                createFlatGroup("a", "p1"),
                createFlatGroup("b", "p3", "p1", "cp1"),
                createFlatGroup(null, "cp3", "p2")
        ));
        sorter.sort(flatGroups);

        List<ProjectsModel> expectedSorting = new LinkedList<ProjectsModel>(Arrays.asList(
                createFlatGroup("z", "cp2", "cp1"),
                createFlatGroup("b", "p3", "p1", "cp1"),
                createFlatGroup("a", "p1"),
                createFlatGroup(null,  "p2", "cp3")
        ));

        assertProjectsModelLists(expectedSorting, flatGroups);
    }

    private ProjectsModel createFlatGroup(String groupName, String... projectNames)
    {
        ProjectsModel group = createGroup(groupName);
        for (String projectName : projectNames)
        {
            group.getRoot().addChild(createConcrete(projectName));
        }
        return group;
    }

    private ProjectsModel createHierarchicalGroup(String label, Object... members)
    {
        ProjectsModel group = createGroup(label);
        TemplateProjectModel node = group.getRoot();
        for (Object member: members)
        {
            if (member instanceof String)
            {
                node.addChild(createConcrete((String) member));
            }
            else
            {
                node.addChild((ProjectModel) member);
            }
        }

        return group;
    }
    private ProjectsModel createGroup(String label)
    {
        return new ProjectsModel(label, label != null);
    }

    private ConcreteProjectModel createConcrete(String projectName)
    {
        ConcreteProjectModel model = mock(ConcreteProjectModel.class);
        stub(model.getName()).toReturn(projectName);
        return model;
    }

    private TemplateProjectModel createTemplates(String projectName, String... children)
    {
        TemplateProjectModel root = new TemplateProjectModel(null, projectName);
        for (String child : children)
        {
            root.addChild(createConcrete(child));
        }
        return root;
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
        assertSame(expected.getName(), got.getName());
    }

    private static class InverseStringComparator implements Comparator<String>
    {
        private Comparator<String> delegate = new Sort.StringComparator();

        public int compare(String o1, String o2)
        {
            int result = delegate.compare(o1, o2);
            if (result < 0)
            {
                return 1;
            }
            if (result > 0)
            {
                return -1;
            }
            return 0;
        }
    }
}
