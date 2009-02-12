package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.ProjectAclForm;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.ConfigurationRegistry;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Vector;

/**
 * Acceptance tests for project ACLs
 */
public class ProjectPermissionsAcceptanceTest extends SeleniumTestBase
{
    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testVisibilityOfGroups() throws Exception
    {
        xmlRpcHelper.insertTrivialUser(random);

        String permissionsPath = PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, ProjectManager.GLOBAL_PROJECT_NAME, "permissions");
        Vector<String> permissions = xmlRpcHelper.getConfigListing(permissionsPath);

        login(random, "");
        ListPage permissionsPage = new ListPage(selenium, urls, permissionsPath);
        permissionsPage.goTo();
        permissionsPage.clickAction(permissions.get(0), ListPage.ACTION_VIEW);

        ProjectAclForm aclForm = new ProjectAclForm(selenium);
        aclForm.waitFor();
        String[] groups = aclForm.getComboBoxOptions("group");
        // Two options could just be the default plus the current value.  As
        // there are at least 3 groups, we can safely assert more than 2.
        assertTrue(groups.length > 2);
    }
}
