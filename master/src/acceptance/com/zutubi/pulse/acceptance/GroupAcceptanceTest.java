package com.zutubi.pulse.acceptance;

import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebTable;
import com.zutubi.pulse.acceptance.forms.AddGroupMembersForm;
import com.zutubi.pulse.acceptance.forms.GroupForm;
import com.zutubi.pulse.util.RandomUtils;
import net.sourceforge.jwebunit.ExpectedRow;
import net.sourceforge.jwebunit.ExpectedTable;

/**
 */
public class GroupAcceptanceTest extends BaseAcceptanceTestCase
{
    private String groupName;

    protected void setUp() throws Exception
    {
        super.setUp();
        loginAsAdmin();
        ensureProject("groups1");
        ensureProject("groups2");
        ensureProject("groups3");
        ensureUser("gu1");
        ensureUser("gu2");
        ensureUser("gu3");
        clickLink(Navigation.TAB_ADMINISTRATION);
        clickLinkWithText("groups");
        groupName = "group " + RandomUtils.randomString(5);
    }

    public void testAddGroup()
    {
        GroupForm form = addPrologue();
        form.saveFormElements(groupName, "false", "false", "false", "");
        assertGroup(groupName, 0, false, false, 0);
    }

    public void testAddGroupServerAdmin()
    {
        GroupForm form = addPrologue();
        form.saveFormElements(groupName, "true", "false", "false", "");
        assertGroup(groupName, 0, true, false, 0);
    }

    public void testAddGroupPersonal()
    {
        GroupForm form = addPrologue();
        form.saveFormElements(groupName, "false", "true", "false", "");
        assertGroup(groupName, 0, false, true, 0);
    }

    public void testAddGroupAllProjectAdmin()
    {
        GroupForm form = addPrologue();
        form.saveFormElements(groupName, "false", "false", "true", "");
        assertGroup(groupName, 0, false, false, -1);
    }

    public void testAddGroupSomeProjectsAdmin()
    {
        GroupForm form = addPrologue();
        String id1 = form.getOptionValue("projects", "groups1");
        String id2 = form.getOptionValue("projects", "groups2");
        form.saveFormElements(groupName, "false", "false", "false", id1 + "," + id2);
        assertGroup(groupName, 0, false, false, 2);
    }

    public void testAddGroupValidation()
    {
        GroupForm form = addPrologue();
        form.saveFormElements("", "false", "false", "false", "");
        form.assertFormPresent();
        assertTextPresent("name is required");
    }

    public void testAddGroupDuplicate()
    {
        testAddGroup();
        GroupForm form = addPrologue();
        form.saveFormElements(groupName, "false", "false", "false", "");
        form.assertFormPresent();
        assertTextPresent("a group with name " + groupName + " already exists");
    }

    public void testEditGroup()
    {
        testAddGroup();
        GroupForm form = editPrologue();
        form.assertFormElements(groupName, "false", "false", "false", "");
        form.saveFormElements(groupName + " edited", "true", "true", "true", "");
        assertGroup(groupName + " edited", 0, true, true, -1);
    }

    public void testEditGroupValidation()
    {
        testAddGroup();
        GroupForm form = editPrologue();
        form.saveFormElements("", "false", "false", "false", "");
        form.assertFormPresent();
        assertTextPresent("name is required");
    }

    public void testEditGroupDuplicate()
    {
        String original = groupName;
        testAddGroup();
        groupName = "group " + RandomUtils.randomString(5);
        testAddGroup();

        GroupForm form = editPrologue();
        form.saveFormElements(original, "false", "false", "false", "");
        form.assertFormPresent();
        assertTextPresent("a group with name " + original + " already exists");
    }

    public void testEditGroupRemovePrivileges()
    {
        testAddGroupSomeProjectsAdmin();
        GroupForm form = editPrologue();
        form.saveFormElements(groupName, "false", "false", "false", "");
        assertGroup(groupName, 0, false, false, 0);
    }

    public void testAddGroupMembers()
    {
        testAddGroup();
        addMember("gu1");
        assertGroupMembers("gu1");
        clickLinkWithText("return to groups view");
        assertGroup(groupName, 1, false, false, 0);
    }

    public void testAddGroupMembersCurrentMembersNotShown()
    {
        testAddGroup();
        addMember("gu1");
        clickLink("group.members.add");
        AddGroupMembersForm form = new AddGroupMembersForm(tester);
        form.assertOptionNotPresent("members", "gu1 (gu1)");
    }

    public void testRemoveGroupMember()
    {
        testAddGroup();
        addMember("gu1");
        clickLink("remove_gu1");
        assertGroupMembers();
    }

    public void testAdminUsersGetPrivileges()
    {
        // Check that they can see both the administration link, and the
        // project editing/triggering links.
        String user = "gu " + RandomUtils.randomString(5);
        navigateToUserAdministration();
        submitCreateUserForm(user, user, user, user);
        logout();

        assertUserHasNoPrivileges(user);

        loginAsAdmin();
        navigateToGroupsAdministration();
        testAddGroupServerAdmin();
        addMember(user);
        logout();

        login(user, user);
        clickLink(Navigation.TAB_ADMINISTRATION);
        assertProjectPrivileges("groups1", true);
        assertProjectPrivileges("groups2", true);
    }

    public void testRemoveAdminPrivileges()
    {
        String user = "gu " + RandomUtils.randomString(5);
        navigateToUserAdministration();
        submitCreateUserForm(user, user, user, user);

        navigateToGroupsAdministration();
        testAddGroupServerAdmin();
        addMember(user);
        clickLink("remove_" + user);
        logout();

        login(user, user);
        assertLinkNotPresent(Navigation.TAB_ADMINISTRATION);
    }

    public void testProjectAdminsGetPrivileges()
    {
        // Should have write access to all projects
        String user = "gu " + RandomUtils.randomString(5);
        navigateToUserAdministration();
        submitCreateUserForm(user, user, user, user);

        navigateToGroupsAdministration();
        
        testAddGroupAllProjectAdmin();
        addMember(user);
        logout();

        login(user, user);
        assertLinkNotPresent(Navigation.TAB_ADMINISTRATION);
        assertProjectPrivileges("groups1", true);
        assertProjectPrivileges("groups2", true);
        assertProjectPrivileges("groups3", true);
    }

    public void testRemoveProjectAdminPrivileges()
    {
        String user = "gu " + RandomUtils.randomString(5);
        navigateToUserAdministration();
        submitCreateUserForm(user, user, user, user);

        navigateToGroupsAdministration();

        testAddGroupAllProjectAdmin();
        addMember(user);
        clickLink("remove_" + user);
        logout();

        login(user, user);
        assertProjectPrivileges("groups1", false);
    }

    public void testSpecificProjectAdminsGetPrivileges()
    {
        // Should have write access to groups1 and groups2 but not groups3
        String user = "gu " + RandomUtils.randomString(5);
        navigateToUserAdministration();
        submitCreateUserForm(user, user, user, user);

        navigateToGroupsAdministration();
        
        testAddGroupSomeProjectsAdmin();
        addMember(user);
        logout();

        login(user, user);
        assertLinkNotPresent(Navigation.TAB_ADMINISTRATION);
        assertProjectPrivileges("groups1", true);
        assertProjectPrivileges("groups2", true);
        assertProjectPrivileges("groups3", false);
    }

    public void testRemoveSpecificProjectAdminPrivileges()
    {
        String user = "gu " + RandomUtils.randomString(5);
        navigateToUserAdministration();
        submitCreateUserForm(user, user, user, user);
        navigateToGroupsAdministration();
        testAddGroupSomeProjectsAdmin();
        addMember(user);
        clickLink("remove_" + user);
        logout();

        login(user, user);
        assertProjectPrivileges("groups1", false);
    }

    private void addMember(String login)
    {
        clickLink("members_" + groupName);
        assertGroupMembers();
        clickLink("group.members.add");
        AddGroupMembersForm form = new AddGroupMembersForm(tester);
        form.assertFormPresent();
        form.saveFormElements(form.getOptionValue("members", login + " (" + login + ")"));
    }

    private GroupForm addPrologue()
    {
        clickLink("group.add");
        GroupForm form = new GroupForm(tester, true);
        form.assertFormPresent();
        return form;
    }

    private GroupForm editPrologue()
    {
        editGroup(groupName);
        GroupForm form = new GroupForm(tester, false);
        form.assertFormPresent();
        return form;
    }

    private void editGroup(String name)
    {
        clickLinkWithText("edit", getGroupRow(name) - 2);
    }

    private void assertGroup(String name, int members, boolean serverAdmin, boolean personal, int projects)
    {
        int row = getGroupRow(name);
        if(row >= 0)
        {
            assertGroup(name, row, members, serverAdmin, personal, projects);
        }
        else
        {
            fail("Row for group '" + name + "' not found");
        }
    }

    private int getGroupRow(String name)
    {
        WebTable table = getTester().getDialog().getWebTableBySummaryOrId("groups");
        for(int i = 0; i < table.getRowCount(); i++)
        {
            TableCell cell = table.getTableCell(i, 0);
            if(cell.getText().equals(name))
            {
                return i;
            }
        }

        return -1;
    }

    private void assertGroup(String name, int row, int members, boolean serverAdmin, boolean personal, int projects)
    {
        String membersText = Integer.toString(members) + " users";
        String adminText = serverAdmin ? "server admin" : "none";
        String personalText = personal ? "personal builds allowed" : "none";
        String projectText = "admin for all projects";

        if(projects == 0)
        {
            projectText = "none";
        }
        else if(projects > 0)
        {
            projectText = "admin for " + projects + " projects";
        }

        assertTableRowEqual("groups", row, new String[] { name, membersText, adminText, personalText, projectText, "edit", "delete" });
    }

    private void assertGroupMembers(String... members)
    {
        ExpectedTable expected = new ExpectedTable();
        String title = "members of group " + groupName;
        expected.appendRow(new ExpectedRow(new String[] { title, title, title }));
        expected.appendRow(new ExpectedRow(new String[] { "login", "name", "actions" }));
        for(String member: members)
        {
            expected.appendRow(new ExpectedRow(new String[] { member, member, "remove" }));
        }
        expected.appendRow(new ExpectedRow(new String[] { "add group members", "add group members", "add group members" }));
        assertTableEquals("members", expected);
    }

    private void assertUserHasNoPrivileges(String user)
    {
        login(user, user);
        assertLinkNotPresent(Navigation.TAB_ADMINISTRATION);
        clickLink(Navigation.TAB_PROJECTS);
        clickLink("groups1");
        assertLinkNotPresentWithText("trigger");
        logout();
    }

    private void assertProjectPrivileges(String project, boolean writeable)
    {
        clickLink(Navigation.TAB_PROJECTS);
        clickLink(project);
        if(writeable)
        {
            assertLinkPresentWithText("trigger");
        }
        else
        {

            assertLinkNotPresentWithText("trigger");
        }
    }

}
