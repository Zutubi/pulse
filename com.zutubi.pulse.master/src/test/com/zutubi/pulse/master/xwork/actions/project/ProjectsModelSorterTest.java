package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.util.Sort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ProjectsModelSorterTest extends ProjectsModelTestBase
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
                createHierarchicalGroup("label-a", "cp2", createTemplates("label-a", "b", "y", "c"), "a")
        ));
        sorter.sort(groups);

        List<ProjectsModel> expectedSorting = new LinkedList<ProjectsModel>(Arrays.asList(
                createHierarchicalGroup("label-a", "a", createTemplates("label-a", "b", "c", "y"), "cp2")
        ));

        assertProjectsModelLists(expectedSorting, groups);
    }

    public void testNestedSorting_MultipleTemplates()
    {
        List<ProjectsModel> groups = new LinkedList<ProjectsModel>(Arrays.asList(
                createHierarchicalGroup("x", "cp2", createTemplates("x", "p1", "y", "c"), "b",
                        createTemplates("x", "p2", "2", "1", "3"), "m", createTemplates("x", "p0", "j", "k")
                )
        ));
        sorter.sort(groups);

        List<ProjectsModel> expectedSorting = new LinkedList<ProjectsModel>(Arrays.asList(
                createHierarchicalGroup("x", "b", "cp2", "m", createTemplates("x", "p0", "j", "k"),
                        createTemplates("x", "p1", "c", "y"), createTemplates("x", "p2", "1", "2", "3"))
        ));

        assertProjectsModelLists(expectedSorting, groups);
    }

    public void testNestedSorting_MixedDepth()
    {
        List<ProjectsModel> groups = new LinkedList<ProjectsModel>(Arrays.asList(
                createHierarchicalGroup("g1", createTemplates("g1", "p1", "y", "c"),
                        createTemplates("g1", "p2", createTemplates("g1", "p4", "r", "t"), "1", "3"),
                        createTemplates("g1", "p0", "j", createTemplates("g1", "p4", "u", "t"), "k", createTemplates("g1", "p5", "h"))
                )
        ));
        sorter.sort(groups);

        List<ProjectsModel> expectedSorting = new LinkedList<ProjectsModel>(Arrays.asList(
                createHierarchicalGroup("g1", createTemplates("g1", "p0", "j", "k", createTemplates("g1", "p4", "t", "u"), createTemplates("g1", "p5", "h")),
                        createTemplates("g1", "p1", "c", "y"),
                        createTemplates("g1", "p2", "1", "3", createTemplates("g1", "p4", "r", "t"))
                )
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

    public void testSortTemplatesToStart()
    {
        List<ProjectsModel> groups = new LinkedList<ProjectsModel>(Arrays.asList(
                createHierarchicalGroup("x", "cp2", createTemplates("x", "p1", "y", "c"), "b",
                        createTemplates("x", "p2", "2", "1", "3"), "m", createTemplates("x", "p0", "j", "k")
                )
        ));
        sorter.sortTemplatesToStart();
        sorter.sort(groups);

        List<ProjectsModel> expectedSorting = new LinkedList<ProjectsModel>(Arrays.asList(
                createHierarchicalGroup("x", createTemplates("x", "p0", "j", "k"), createTemplates("x", "p1", "c", "y"),
                        createTemplates("x", "p2", "1", "2", "3"), "b", "cp2", "m")
        ));

        assertProjectsModelLists(expectedSorting, groups);
    }

    public void testSortTemplatesToEnd()
    {
        List<ProjectsModel> groups = new LinkedList<ProjectsModel>(Arrays.asList(
                createHierarchicalGroup("x", "cp2", createTemplates("x", "p1", "y", "c"), "b",
                        createTemplates("x", "p2", "2", "1", "3"), "m", createTemplates("x", "p0", "j", "k")
                )
        ));
        sorter.sortTemplatesToEnd();
        sorter.sort(groups);

        List<ProjectsModel> expectedSorting = new LinkedList<ProjectsModel>(Arrays.asList(
                createHierarchicalGroup("x", "b", "cp2", "m", createTemplates("x", "p0", "j", "k"), createTemplates("x", "p1", "c", "y"),
                        createTemplates("x", "p2", "1", "2", "3"))
        ));

        assertProjectsModelLists(expectedSorting, groups);
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
