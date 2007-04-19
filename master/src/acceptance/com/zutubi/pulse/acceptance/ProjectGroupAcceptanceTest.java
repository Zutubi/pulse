package com.zutubi.pulse.acceptance;

import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebTable;
import com.zutubi.pulse.acceptance.forms.ProjectGroupForm;
import com.zutubi.util.RandomUtils;

/**
 */
public class ProjectGroupAcceptanceTest extends BaseAcceptanceTestCase
{
    private static final String P1 = "projectgroups1";
    private static final String P2 = "projectgroups2";
    private static final String P3 = "projectgroups3";

    private String groupName;

    protected void setUp() throws Exception
    {
        super.setUp();
        loginAsAdmin();
        ensureProject(P1);
        ensureProject(P2);
        ensureProject(P3);
        clickLink(Navigation.TAB_PROJECTS);
        groupName = "Project_Group_" + RandomUtils.randomString(5);
    }

    public void testAddGroup()
    {
        ProjectGroupForm form = addPrologue();
        String id1 = form.getOptionValue("projects", P1);
        String id3 = form.getOptionValue("projects", P3);
        form.saveFormElements(groupName, id1 + "," + id3);
        assertGroup(groupName, P1, P3);
    }

    public void testAddGroupValidation()
    {
        ProjectGroupForm form = addPrologue();
        form.saveFormElements("", "");
        form.assertFormPresent();
        assertTextPresent("name is required");
    }

    public void testAddGroupDuplicate()
    {
        testAddGroup();
        ProjectGroupForm form = addPrologue();
        form.saveFormElements(groupName, "");
        form.assertFormPresent();
        assertTextPresent("a group with name " + groupName + " already exists");
    }

    public void testEditGroup()
    {
        testAddGroup();
        ProjectGroupForm form = editPrologue();
        String id1 = form.getOptionValue("projects", P1);
        String id3 = form.getOptionValue("projects", P3);
        form.assertFormElements(groupName, id1 + "," + id3);
        form.saveFormElements(groupName + "_edited", id3);
        assertGroup(groupName + "_edited", P3);
    }

    public void testEditGroupValidation()
    {
        testAddGroup();
        ProjectGroupForm form = editPrologue();
        form.saveFormElements("", "");
        form.assertFormPresent();
        assertTextPresent("name is required");
    }

    public void testEditGroupDuplicate()
    {
        String original = groupName;
        testAddGroup();
        groupName = "Project_Group_" + RandomUtils.randomString(5);
        testAddGroup();

        ProjectGroupForm form = editPrologue();
        form.saveFormElements(original, "");
        form.assertFormPresent();
        assertTextPresent("a group with name " + original + " already exists");
    }

    public void testDeleteGroup()
    {
        testAddGroup();
        clickLink("delete_" + groupName);
        assertTableNotPresent("group_" + groupName);
    }

    private ProjectGroupForm addPrologue()
    {
        clickLink("project.group.add");
        ProjectGroupForm form = new ProjectGroupForm(tester, true);
        form.assertFormPresent();
        return form;
    }

    private ProjectGroupForm editPrologue()
    {
        clickLink("edit_" + groupName);
        ProjectGroupForm form = new ProjectGroupForm(tester, false);
        form.assertFormPresent();
        return form;
    }

    private void assertGroup(String name, String... projects)
    {
        WebTable table = getTester().getDialog().getWebTableBySummaryOrId("group_" + name);
        assertNotNull(table);
        assertEquals(table.getRowCount() - 4, projects.length);
        for(int i = 3; i < table.getRowCount() - 1; i++)
        {
            TableCell cell = table.getTableCell(i, 0);
            assertEquals(projects[i - 3], cell.getText());
        }
    }

}
