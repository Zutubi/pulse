package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.BuiltinGroupForm;
import com.zutubi.pulse.acceptance.forms.admin.GroupForm;
import com.zutubi.pulse.acceptance.pages.admin.GroupsPage;
import com.zutubi.pulse.acceptance.pages.admin.HierarchyPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.master.model.ProjectManager;
import static com.zutubi.pulse.master.model.UserManager.ALL_USERS_GROUP_NAME;
import static com.zutubi.pulse.master.model.UserManager.ANONYMOUS_USERS_GROUP_NAME;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.tove.type.record.PathUtils;

/**
 * Test for user groups.
 */
public class GroupAcceptanceTest extends SeleniumTestBase
{
    private static final String ANONYMOUS_USERS_GROUP_ID = "anonymous.users";

    public void testDefaultGroupsTable()
    {
        loginAsAdmin();

        GroupsPage groupsPage = new GroupsPage(selenium, urls);
        groupsPage.goTo();

        assertTrue(groupsPage.isGroupPresent("administrators"));
        assertTrue(groupsPage.isGroupPresent("all users"));
        assertTrue(groupsPage.isGroupPresent("anonymous users"));
        assertTrue(groupsPage.isGroupPresent("developers"));
        assertTrue(groupsPage.isGroupPresent("project administrators"));
    }

    public void testCreateEmptyGroup()
    {
        loginAsAdmin();

        GroupsPage groupsPage = new GroupsPage(selenium, urls);
        groupsPage.goTo();

        GroupForm form = groupsPage.clickAddGroupAndWait();
        form.finishFormElements(random, null, null);

        waitForElement(getGroupPath(random));
        assertTrue(form.isFormPresent());
        assertFormElements(form, random, "", "");
    }

    public void testAddUserToGroup() throws Exception
    {
        String login = "u:" + random;
        String userHandle;

        xmlRpcHelper.loginAsAdmin();
        try
        {
            String userPath = xmlRpcHelper.insertTrivialUser(login);
            userHandle = xmlRpcHelper.getConfigHandle(userPath);
        }
        finally
        {
            xmlRpcHelper.logout();
        }

        login(login, "");
        ProjectHierarchyPage globalPage = new ProjectHierarchyPage(selenium, urls, ProjectManager.GLOBAL_PROJECT_NAME, true);
        globalPage.goTo();
        assertFalse(globalPage.isAddPresent());
        logout();

        loginAsAdmin();
        GroupsPage groupsPage = new GroupsPage(selenium, urls);
        groupsPage.goTo();

        GroupForm form = groupsPage.clickAddGroupAndWait();
        form.finishFormElements(random, null, ServerPermission.CREATE_PROJECT.toString());
        waitForElement(getGroupPath(random));
        form.waitFor();
        form.applyFormElements(null, userHandle, null);
        form.waitFor();
        logout();

        login(login, "");
        globalPage.goTo();
        SeleniumUtils.waitForElementId(selenium, HierarchyPage.LINK_ADD);
        logout();
    }

    public void testAddPermissionToGroup() throws Exception
    {
        String login = "u:" + random;
        String userHandle;

        xmlRpcHelper.loginAsAdmin();
        try
        {
            String userPath = xmlRpcHelper.insertTrivialUser(login);
            userHandle = xmlRpcHelper.getConfigHandle(userPath);
        }
        finally
        {
            xmlRpcHelper.logout();
        }

        loginAsAdmin();
        GroupsPage groupsPage = new GroupsPage(selenium, urls);
        groupsPage.goTo();

        GroupForm form = groupsPage.clickAddGroupAndWait();
        form.finishFormElements(random, userHandle, null);
        waitForElement(getGroupPath(random));
        logout();

        login(login, "");
        ProjectHierarchyPage globalPage = new ProjectHierarchyPage(selenium, urls, ProjectManager.GLOBAL_PROJECT_NAME, true);
        globalPage.goTo();
        assertFalse(globalPage.isAddPresent());
        logout();

        loginAsAdmin();
        goTo(urls.adminGroup(random));
        form.waitFor();
        form.applyFormElements(null, null, ServerPermission.CREATE_PROJECT.toString());
        form.waitFor();
        logout();

        login(login, "");
        globalPage.goTo();
        assertTrue(globalPage.isAddPresent());
        logout();
    }

    public void testEditBuiltInGroup()
    {
        loginAsAdmin();

        GroupsPage groupsPage = new GroupsPage(selenium, urls);
        groupsPage.goTo();

        BuiltinGroupForm form = groupsPage.clickViewBuiltinGroupAndWait(ANONYMOUS_USERS_GROUP_ID);

        form.applyFormElements(null, ServerPermission.PERSONAL_BUILD.toString());
        form.waitFor();
        assertTextNotPresent("name requires a value");
    }

    public void testAllUsersGroupNameIsReadOnly()
    {
        assertBuiltinGroupNameIsReadOnly(ALL_USERS_GROUP_NAME, "all.users");
    }

    public void testAnonymousUserGroupNameIsReadOnly()
    {
        assertBuiltinGroupNameIsReadOnly(ANONYMOUS_USERS_GROUP_NAME, ANONYMOUS_USERS_GROUP_ID);
    }

    private void assertBuiltinGroupNameIsReadOnly(String groupName, String groupId)
    {
        loginAsAdmin();

        GroupsPage groupsPage = new GroupsPage(selenium, urls);
        groupsPage.goTo();

        // a) ensure only view link is available for all users group.
        assertFalse(groupsPage.isActionLinkPresent(groupName, "clone"));
        assertFalse(groupsPage.isActionLinkPresent(groupName, "delete"));

        // b) go to form, ensure name is not editable.
        // click view or groups/name
        BuiltinGroupForm form = groupsPage.clickViewBuiltinGroupAndWait(groupId);
        assertTrue(form.isFormPresent());
        assertFalse(form.isEditable("name"));

        logout();
    }

    private String getGroupPath(String group)
    {
        return PathUtils.getPath(MasterConfigurationRegistry.GROUPS_SCOPE, group);
    }
}
