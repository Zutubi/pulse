package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.ProjectAclForm;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.acceptance.rpc.RpcClient;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.tove.security.AccessManager;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import static com.zutubi.tove.type.record.PathUtils.getPath;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;

/**
 * Acceptance tests for project ACLs
 */
public class ProjectPermissionsAcceptanceTest extends AcceptanceTestBase
{
    protected void setUp() throws Exception
    {
        super.setUp();
        rpcClient.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        if (rpcClient.isLoggedIn())
        {
            rpcClient.logout();
        }
        super.tearDown();
    }

    public void testVisibilityOfGroups() throws Exception
    {
        rpcClient.RemoteApi.insertTrivialUser(random);

        String permissionsPath = getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, ProjectManager.GLOBAL_PROJECT_NAME, "permissions");
        Vector<String> permissions = rpcClient.RemoteApi.getConfigListing(permissionsPath);

        getBrowser().loginAndWait(random, "");
        ListPage permissionsPage = getBrowser().openAndWaitFor(ListPage.class, permissionsPath);
        permissionsPage.clickAction(permissions.get(0), ListPage.ACTION_VIEW);

        ProjectAclForm aclForm = getBrowser().createForm(ProjectAclForm.class);
        aclForm.waitFor();
        List<String> groups = aclForm.getComboBoxOptions("group");
        // Two options could just be the default plus the current value.  As
        // there are at least 3 groups, we can safely assert more than 2.
        assertTrue(groups.size() > 2);
    }

    public void testPermissionLabels() throws Exception
    {
        String permissionsPath = getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, ProjectManager.GLOBAL_PROJECT_NAME, "permissions");
        Vector<String> permissions = rpcClient.RemoteApi.getConfigListing(permissionsPath);

        getBrowser().loginAsAdmin();
        ListPage permissionsPage = getBrowser().openAndWaitFor(ListPage.class, permissionsPath);
        permissionsPage.clickAction(permissions.get(0), ListPage.ACTION_VIEW);

        ProjectAclForm aclForm = getBrowser().createForm(ProjectAclForm.class);
        aclForm.waitFor();
        List<String> actionDisplays = aclForm.getComboBoxDisplays("allowedActions.choice");
        assertThat(actionDisplays, hasItem("administer"));
        assertThat(actionDisplays, hasItem("cancel build"));
        assertThat(actionDisplays, hasItem("view source"));

        List<String> actionValues = aclForm.getComboBoxOptions("allowedActions.choice");
        assertThat(actionValues, hasItem("administer"));
        assertThat(actionValues, hasItem("cancelBuild"));
        assertThat(actionValues, hasItem("viewSource"));
    }

    public void testCleanPermission() throws Exception
    {
        String project = random + "-project";
        String canCleanUser = random + "-clean";
        String cannotCleanUser = random + "-noclean";

        String projectPath = rpcClient.RemoteApi.insertSimpleProject(project);

        addUserWithProjectPermissions(canCleanUser, project, ProjectConfigurationActions.ACTION_MARK_CLEAN);
        rpcClient.RemoteApi.insertTrivialUser(cannotCleanUser);

        getBrowser().loginAndWait(canCleanUser, "");
        ProjectHomePage homePage = getBrowser().openAndWaitFor(ProjectHomePage.class, project);
        assertTrue(homePage.isActionPresent(ProjectConfigurationActions.ACTION_MARK_CLEAN));
        getBrowser().logout();

        getBrowser().loginAndWait(cannotCleanUser, "");
        homePage.openAndWaitFor();
        assertFalse(homePage.isActionPresent(ProjectConfigurationActions.ACTION_MARK_CLEAN));
        getBrowser().logout();

        RpcClient client = new RpcClient();
        client.login(canCleanUser, "");
        client.RemoteApi.doConfigAction(projectPath, ProjectConfigurationActions.ACTION_MARK_CLEAN);
        client.logout();

        client.login(cannotCleanUser, "");
        try
        {
            client.RemoteApi.doConfigAction(projectPath, ProjectConfigurationActions.ACTION_MARK_CLEAN);
            fail("User should not have permission to clean");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("AccessDeniedException"));
        }
        finally
        {
            client.logout();
        }
    }
    
    public void testAclUpdatesRequireAdminPermission() throws Exception
    {
        String project = random + "-project";
        String projectAdminUser = random + "-admin";
        String projectWriteUser = random + "-write";

        String projectPath = rpcClient.RemoteApi.insertSimpleProject(project);
        addUserWithProjectPermissions(projectAdminUser, project, AccessManager.ACTION_ADMINISTER);
        addUserWithProjectPermissions(projectWriteUser, project, AccessManager.ACTION_WRITE);

        RpcClient adminClient = new RpcClient();
        adminClient.login(projectAdminUser, "");
        RpcClient writeClient = new RpcClient();
        writeClient.login(projectWriteUser, "");
        
        String permissionsPath = getPath(projectPath, "permissions");
        String projectAdminsPath = getPath(MasterConfigurationRegistry.GROUPS_SCOPE, "project administrators");
        try
        {
            // The writer can't add ACLs, the admin can.
            Hashtable<String, Object> aclConfig = rpcClient.RemoteApi.createProjectAcl(projectAdminsPath, ProjectConfigurationActions.ACTION_CANCEL_BUILD);
            String aclPath;
            try
            {
                writeClient.RemoteApi.insertConfig(permissionsPath, aclConfig);
                fail("User without admin permission should not be able to insert new ACL");
            }
            catch (Exception e)
            {
                assertThat(e.getMessage(), containsString("AccessDeniedException"));
            }
            
            aclPath = adminClient.RemoteApi.insertConfig(permissionsPath, aclConfig);
            
            // The writer can happily read back the permission.
            aclConfig = writeClient.RemoteApi.getConfig(aclPath);
            aclConfig.put("allowedActions", new Vector());
            
            // The writer can't update the ACL, the admin can.
            try
            {
                writeClient.RemoteApi.saveConfig(aclPath, aclConfig, false);
                fail("User without admin permission should not be able to change ACL");
            }
            catch (Exception e)
            {
                assertThat(e.getMessage(), containsString("AccessDeniedException"));
            }
            
            adminClient.RemoteApi.saveConfig(aclPath, aclConfig, false);
            
            // The writer can't delete the ACL, the admin can.
            try
            {
                writeClient.RemoteApi.deleteConfig(aclPath);
                fail("User without admin permission should not be able to delete ACL");
            }
            catch (Exception e)
            {
                assertThat(e.getMessage(), containsString("AccessDeniedException"));
            }

            adminClient.RemoteApi.deleteConfig(aclPath);
        }
        finally
        {
            adminClient.logout();
            writeClient.logout();
        }
    }
    
    private void addUserWithProjectPermissions(String user, String project, String... permissions) throws Exception
    {
        String userPath = rpcClient.RemoteApi.insertTrivialUser(user);
        String groupPath = rpcClient.RemoteApi.insertGroup(user + "-group", asList(userPath));
        rpcClient.RemoteApi.addProjectPermissions(getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, project), groupPath, permissions);
    }
}
