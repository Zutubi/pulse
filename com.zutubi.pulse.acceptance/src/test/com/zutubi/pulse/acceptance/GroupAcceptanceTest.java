package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.BuiltinGroupForm;
import com.zutubi.pulse.acceptance.forms.admin.GroupForm;
import com.zutubi.pulse.acceptance.pages.admin.GroupsPage;
import com.zutubi.pulse.acceptance.pages.admin.HierarchyPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.tove.type.record.PathUtils;

import static com.zutubi.pulse.master.model.UserManager.*;

/**
 * Test for user groups.
 */
public class GroupAcceptanceTest extends AcceptanceTestBase
{
    private static final String ANONYMOUS_USERS_GROUP_ID = "anonymous.users";

    public void testDefaultGroupsTable()
    {
        getBrowser().loginAsAdmin();

        GroupsPage groupsPage = getBrowser().openAndWaitFor(GroupsPage.class);

        assertTrue(groupsPage.isGroupPresent(ADMINS_GROUP_NAME));
        assertTrue(groupsPage.isGroupPresent(ALL_USERS_GROUP_NAME));
        assertTrue(groupsPage.isGroupPresent(ANONYMOUS_USERS_GROUP_NAME));
        assertTrue(groupsPage.isGroupPresent(DEVELOPERS_GROUP_NAME));
        assertTrue(groupsPage.isGroupPresent(PROJECT_ADMINS_GROUP_NAME));
    }

    public void testCreateEmptyGroup()
    {
        getBrowser().loginAsAdmin();

        GroupsPage groupsPage = getBrowser().openAndWaitFor(GroupsPage.class);

        GroupForm form = groupsPage.clickAddGroupAndWait();
        form.finishFormElements(random, null, null);

        getBrowser().waitForElement(getGroupPath(random));
        assertTrue(form.isFormPresent());
        assertTrue(form.checkFormValues(random, "", ""));
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

        assertTrue(getBrowser().login(login, ""));
        ProjectHierarchyPage globalPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, ProjectManager.GLOBAL_PROJECT_NAME, true);
        assertFalse(globalPage.isAddPresent());
        getBrowser().logout();

        getBrowser().loginAsAdmin();
        GroupsPage groupsPage = getBrowser().openAndWaitFor(GroupsPage.class);

        GroupForm form = groupsPage.clickAddGroupAndWait();
        form.finishFormElements(random, null, ServerPermission.CREATE_PROJECT.toString());
        getBrowser().waitForElement(getGroupPath(random));
        form.waitFor();
        form.applyFormElements(null, userHandle, null);
        form.waitFor();
        getBrowser().logout();

        assertTrue(getBrowser().login(login, ""));
        globalPage.openAndWaitFor();
        getBrowser().waitForElement(HierarchyPage.LINK_ADD);
        getBrowser().logout();
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

        getBrowser().loginAsAdmin();
        GroupsPage groupsPage = getBrowser().openAndWaitFor(GroupsPage.class);

        GroupForm form = groupsPage.clickAddGroupAndWait();
        form.finishFormElements(random, userHandle, null);
        getBrowser().waitForElement(getGroupPath(random));
        getBrowser().logout();

        assertTrue(getBrowser().login(login, ""));
        ProjectHierarchyPage globalPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, ProjectManager.GLOBAL_PROJECT_NAME, true);
        assertFalse(globalPage.isAddPresent());
        getBrowser().logout();

        getBrowser().loginAsAdmin();
        getBrowser().open(urls.adminGroup(random));
        form.waitFor();
        form.applyFormElements(null, null, ServerPermission.CREATE_PROJECT.toString());
        form.waitFor();
        getBrowser().logout();

        assertTrue(getBrowser().login(login, ""));
        globalPage.openAndWaitFor();
        assertTrue(globalPage.isAddPresent());
        getBrowser().logout();
    }

    public void testEditBuiltInGroup()
    {
        getBrowser().loginAsAdmin();

        GroupsPage groupsPage = getBrowser().openAndWaitFor(GroupsPage.class);

        BuiltinGroupForm form = groupsPage.clickViewBuiltinGroupAndWait(ANONYMOUS_USERS_GROUP_ID);

        form.applyFormElements(null, ServerPermission.PERSONAL_BUILD.toString());
        form.waitFor();
        assertFalse(getBrowser().isTextPresent("name requires a value"));
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
        getBrowser().loginAsAdmin();

        GroupsPage groupsPage = getBrowser().openAndWaitFor(GroupsPage.class);

        // a) ensure only view link is available for all users group.
        assertFalse(groupsPage.isActionLinkPresent(groupName, "clone"));
        assertFalse(groupsPage.isActionLinkPresent(groupName, "delete"));

        // b) go to form, ensure name is not editable.
        // click view or groups/name
        BuiltinGroupForm form = groupsPage.clickViewBuiltinGroupAndWait(groupId);
        assertTrue(form.isFormPresent());
        assertFalse(form.isEditable("name"));

        getBrowser().logout();
    }

    private String getGroupPath(String group)
    {
        return PathUtils.getPath(MasterConfigurationRegistry.GROUPS_SCOPE, group);
    }
}
