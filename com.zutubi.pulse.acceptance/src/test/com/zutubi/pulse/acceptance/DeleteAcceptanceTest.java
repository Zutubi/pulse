package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.admin.*;
import com.zutubi.pulse.acceptance.pages.browse.BrowsePage;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.BuildCompletedTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfigurationActions;
import com.zutubi.tove.config.ConfigurationRegistry;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Tests for deletion of various things: an area that is notorious for bugs!
 */
@Test(dependsOnGroups = {"init.*"})
public class DeleteAcceptanceTest extends SeleniumTestBase
{
    private static final String ACTION_DELETE_RECORD = "delete record";
    private static final String ACTION_DELETE_BUILDS = "delete all build results";
    private static final String ACTION_HIDE_RECORD   = "hide record";

    private static final String ACTION_RESTORE       = "restore";

    @BeforeMethod
    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();
    }

    @AfterMethod
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testDeleteListItem() throws Exception
    {
        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);
        String labelPath = insertLabel(projectPath);

        loginAsAdmin();
        ListPage labelList = new ListPage(selenium, urls, PathUtils.getParentPath(labelPath));
        labelList.goTo();
        String baseName = PathUtils.getBaseName(labelPath);
        labelList.assertItemPresent(baseName, null, "view", "delete");
        DeleteConfirmPage confirmPage = labelList.clickDelete(baseName);
        confirmPage.waitFor();
        confirmPage.assertTasks(labelPath, ACTION_DELETE_RECORD);
        confirmPage.clickDelete();

        labelList.waitFor();
        labelList.assertItemNotPresent(baseName);
    }

    public void testCancelDelete() throws Exception
    {
        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);
        String labelPath = insertLabel(projectPath);

        loginAsAdmin();
        ListPage labelList = new ListPage(selenium, urls, PathUtils.getParentPath(labelPath));
        labelList.goTo();
        String baseName = PathUtils.getBaseName(labelPath);
        labelList.assertItemPresent(baseName, null, "view", "delete");
        DeleteConfirmPage confirmPage = labelList.clickDelete(baseName);
        confirmPage.waitFor();
        confirmPage.clickCancel();

        labelList.waitFor();
        labelList.assertItemPresent(baseName, null);
    }

    public void testCancelDeleteProject() throws Exception
    {
        xmlRpcHelper.insertTrivialProject(random, false);
        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, random, false);
        hierarchyPage.goTo();
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        confirmPage.clickCancel();
        hierarchyPage.waitFor();
    }

    public void testDeleteProject() throws Exception
    {
        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);

        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, random, false);
        hierarchyPage.goTo();
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        confirmPage.assertTasks(projectPath, ACTION_DELETE_RECORD, projectPath, ACTION_DELETE_BUILDS);
        confirmPage.clickDelete();
        ProjectHierarchyPage global = new ProjectHierarchyPage(selenium, urls, ProjectManager.GLOBAL_PROJECT_NAME, true);
        global.waitFor();
        assertElementNotPresent("link=" + random);

        BrowsePage browsePage = new BrowsePage(selenium, urls);
        browsePage.goTo();
        browsePage.assertProjectNotPresent(null, random);
    }

    public void testDeleteProjectWithReference() throws Exception
    {
        String refererName = random + "-er";
        String refereeName = random + "-ee";
        String refererPath = xmlRpcHelper.insertTrivialProject(refererName, false);
        String refereePath = xmlRpcHelper.insertTrivialProject(refereeName, false);

        String triggerPath = insertBuildCompletedTrigger(refereePath, refererPath);

        loginAsAdmin();
        ListPage triggersPage = new ListPage(selenium, urls, PathUtils.getParentPath(triggerPath));
        triggersPage.goTo();
        triggersPage.assertItemPresent("test", null);

        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, refereeName, false);
        hierarchyPage.goTo();
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        confirmPage.assertTasks(refereePath, ACTION_DELETE_RECORD, refereePath, ACTION_DELETE_BUILDS, triggerPath, ACTION_DELETE_RECORD);
        confirmPage.clickDelete();

        ProjectHierarchyPage globalPage = new ProjectHierarchyPage(selenium, urls, ProjectManager.GLOBAL_PROJECT_NAME, true);
        globalPage.waitFor();

        triggersPage.goTo();
        triggersPage.assertItemNotPresent("test");
    }

    public void testDeleteWithInvisibleReference() throws Exception
    {
        // We need a user that is part of a group we can assign privileges to
        String authLogin = "u1:" + random;
        String authPath = xmlRpcHelper.insertTrivialUser(authLogin);

        String noAuthLogin = "u2:" + random;
        String noAuthPath = xmlRpcHelper.insertTrivialUser(noAuthLogin);

        String groupName = "group:" + random;
        xmlRpcHelper.insertGroup(groupName, Arrays.asList(authPath, noAuthPath), ServerPermission.DELETE_PROJECT.toString());

        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);

        String dashboardPath = PathUtils.getPath(authPath, "preferences", "dashboard");
        Hashtable<String, Object> dashboard = xmlRpcHelper.getConfig(dashboardPath);
        dashboard.put("showAllProjects", false);
        dashboard.put("shownProjects", new Vector<String>(Arrays.asList(projectPath)));
        xmlRpcHelper.saveConfig(dashboardPath, dashboard, false);

        login(authLogin, "");
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, random, false);
        hierarchyPage.goTo();
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        confirmPage.assertTasks(projectPath, ACTION_DELETE_RECORD, projectPath, ACTION_DELETE_BUILDS, PathUtils.getPath(dashboardPath, "shownProjects", "0"), "remove reference");
        assertTextNotPresent("A further");
        confirmPage.clickCancel();
        hierarchyPage.waitFor();
        logout();
        
        login(noAuthLogin, "");
        hierarchyPage.goTo();
        confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        confirmPage.assertTasks(projectPath, ACTION_DELETE_RECORD, projectPath, ACTION_DELETE_BUILDS);
        assertTextPresent("A further task is required that is not visible to you with your current permissions");
        confirmPage.clickDelete();

        ProjectHierarchyPage globalPage = new ProjectHierarchyPage(selenium, urls, ProjectManager.GLOBAL_PROJECT_NAME, true);
        globalPage.waitFor();

        dashboard = xmlRpcHelper.getConfig(dashboardPath);
        assertEquals(0, ((Vector) dashboard.get("shownProjects")).size());

        // Make sure that it is not missing just because the reference is now
        // broken
        assertTrue(xmlRpcHelper.isConfigValid(dashboardPath));
    }

    public void testDeleteAgent() throws Exception
    {
        String path = xmlRpcHelper.insertSimpleAgent(random);
        loginAsAdmin();
        AgentHierarchyPage hierarchyPage = new AgentHierarchyPage(selenium, urls, random, false);
        hierarchyPage.goTo();
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        confirmPage.assertTasks(path, ACTION_DELETE_RECORD, path, "delete agent state");
        confirmPage.clickDelete();

        AgentHierarchyPage globalPage = new AgentHierarchyPage(selenium, urls, AgentManager.GLOBAL_AGENT_NAME, true);
        globalPage.waitFor();

        assertFalse(xmlRpcHelper.configPathExists(path));
    }

    public void testDeleteAgentWithBuildStageReference() throws Exception
    {
        String agentName = "agent-" + random;
        String agentPath = xmlRpcHelper.insertSimpleAgent(agentName);
        String projectName = "project-" + random;
        String projectPath = xmlRpcHelper.insertSimpleProject(projectName, false);

        Hashtable <String, Object> stage = xmlRpcHelper.createEmptyConfig(BuildStageConfiguration.class);
        stage.put("name", "agent");
        stage.put("agent", agentPath);
        stage.put("recipe", "");
        String stagePath = xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, "stages"), stage);

        loginAsAdmin();
        AgentHierarchyPage hierarchyPage = new AgentHierarchyPage(selenium, urls, agentName, false);
        hierarchyPage.goTo();
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        confirmPage.assertTasks(agentPath, ACTION_DELETE_RECORD, agentPath, "delete agent state", PathUtils.getPath(stagePath, "agent"), "null out reference");
        confirmPage.clickDelete();

        AgentHierarchyPage globalPage = new AgentHierarchyPage(selenium, urls, AgentManager.GLOBAL_AGENT_NAME, true);
        globalPage.waitFor();
        assertFalse(xmlRpcHelper.configPathExists(agentPath));

        assertTrue(xmlRpcHelper.isConfigValid(stagePath));
        stage = xmlRpcHelper.getConfig(stagePath);
        assertNull(stage.get("agent"));
    }

    public void testDeleteUser() throws Exception
    {
        String path = xmlRpcHelper.insertTrivialUser(random);
        loginAsAdmin();
        
        ListPage usersPage = new ListPage(selenium, urls, ConfigurationRegistry.USERS_SCOPE);
        usersPage.goTo();
        usersPage.assertItemPresent(random, null, AccessManager.ACTION_VIEW, AccessManager.ACTION_DELETE, UserConfigurationActions.ACTION_SET_PASSWORD);
        DeleteConfirmPage confirmPage = usersPage.clickDelete(random);
        confirmPage.waitFor();
        confirmPage.assertTasks(path, ACTION_DELETE_RECORD, path, "delete user state");
        confirmPage.clickDelete();

        usersPage.waitFor();
        usersPage.assertItemNotPresent(random);
    }

    public void testHideMapItem() throws Exception
    {
        String projectPath = xmlRpcHelper.insertSimpleProject(random, false);

        loginAsAdmin();
        String cleanupsPath = PathUtils.getPath(projectPath, "cleanup");
        String cleanupPath = PathUtils.getPath(cleanupsPath, "default");

        ListPage cleanupsPage = new ListPage(selenium, urls, cleanupsPath);
        cleanupsPage.goTo();
        cleanupsPage.expandTreeNode(cleanupsPath);
        cleanupsPage.assertItemPresent("default", ListPage.ANNOTATION_INHERITED, AccessManager.ACTION_VIEW, AccessManager.ACTION_CLONE, AccessManager.ACTION_DELETE);
        cleanupsPage.assertTreeLinkPresent("default");

        DeleteConfirmPage confirmPage = cleanupsPage.clickDelete("default");
        confirmPage.waitFor();
        assertTextPresent("Are you sure you wish to hide this record?");
        confirmPage.assertTasks(cleanupPath, ACTION_HIDE_RECORD);
        confirmPage.clickDelete();

        cleanupsPage.waitFor();
        cleanupsPage.assertItemPresent("default", ListPage.ANNOTATION_HIDDEN, ACTION_RESTORE);
        cleanupsPage.assertTreeLinkNotPresent("default");
    }

    public void testCancelHide() throws Exception
    {
        String projectPath = xmlRpcHelper.insertSimpleProject(random, false);

        loginAsAdmin();
        String cleanupssPath = PathUtils.getPath(projectPath, "cleanup");

        ListPage cleanupsPage = new ListPage(selenium, urls, cleanupssPath);
        cleanupsPage.goTo();
        cleanupsPage.assertItemPresent("default", ListPage.ANNOTATION_INHERITED, AccessManager.ACTION_VIEW, AccessManager.ACTION_CLONE, AccessManager.ACTION_DELETE);

        DeleteConfirmPage confirmPage = cleanupsPage.clickDelete("default");
        confirmPage.waitFor();
        confirmPage.clickCancel();

        cleanupsPage.waitFor();
        cleanupsPage.assertItemPresent("default", ListPage.ANNOTATION_INHERITED, AccessManager.ACTION_VIEW, AccessManager.ACTION_CLONE, AccessManager.ACTION_DELETE);
    }

    public void testHideMapItemWithSkeletonDescendent() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";

        String parentPath = xmlRpcHelper.insertSimpleProject(parentName, true);
        String parentCleanupsPath = PathUtils.getPath(parentPath, "cleanup");
        String parentCleanupPath = PathUtils.getPath(parentCleanupsPath, "default");
        String childPath = xmlRpcHelper.insertSimpleProject(childName, parentName, false);
        String childCleanupPath = PathUtils.getPath(childPath, "cleanup", "default");

        loginAsAdmin();

        ListPage cleanupsPage = new ListPage(selenium, urls, parentCleanupsPath);
        cleanupsPage.goTo();
        cleanupsPage.assertItemPresent("default", ListPage.ANNOTATION_INHERITED, AccessManager.ACTION_VIEW, AccessManager.ACTION_CLONE, AccessManager.ACTION_DELETE);

        DeleteConfirmPage confirmPage = cleanupsPage.clickDelete("default");
        confirmPage.waitFor();
        assertTextPresent("Are you sure you wish to hide this record?");
        confirmPage.assertTasks(parentCleanupPath, ACTION_HIDE_RECORD);
        confirmPage.clickDelete();

        cleanupsPage.waitFor();
        cleanupsPage.assertItemPresent("default", ListPage.ANNOTATION_HIDDEN, ACTION_RESTORE);
        assertFalse(xmlRpcHelper.configPathExists(childCleanupPath));
    }

    public void testHideMapItemWithDescendentOverride() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";

        String parentPath = xmlRpcHelper.insertSimpleProject(parentName, true);
        String parentCleanupsPath = PathUtils.getPath(parentPath, "cleanup");
        String parentCleanupPath = PathUtils.getPath(parentCleanupsPath, "default");
        String childPath = xmlRpcHelper.insertSimpleProject(childName, parentName, false);
        String childCleanupPath = PathUtils.getPath(childPath, "cleanup", "default");
        Hashtable<String, Object> childCleanup = xmlRpcHelper.getConfig(childCleanupPath);
        childCleanup.put("retain", 928);
        xmlRpcHelper.saveConfig(childCleanupPath, childCleanup, false);

        loginAsAdmin();

        ListPage cleanupsPage = new ListPage(selenium, urls, parentCleanupsPath);
        cleanupsPage.goTo();
        cleanupsPage.assertItemPresent("default", ListPage.ANNOTATION_INHERITED, AccessManager.ACTION_VIEW, AccessManager.ACTION_CLONE, AccessManager.ACTION_DELETE);

        DeleteConfirmPage confirmPage = cleanupsPage.clickDelete("default");
        confirmPage.waitFor();
        assertTextPresent("Are you sure you wish to hide this record?");
        confirmPage.assertTasks(parentCleanupPath, ACTION_HIDE_RECORD, childCleanupPath, ACTION_DELETE_RECORD);
        confirmPage.clickDelete();

        cleanupsPage.waitFor();
        cleanupsPage.assertItemPresent("default", ListPage.ANNOTATION_HIDDEN, ACTION_RESTORE);
        assertFalse(xmlRpcHelper.configPathExists(childCleanupPath));
    }

    public void testRestoreMapItem() throws Exception
    {
        String projectPath = xmlRpcHelper.insertSimpleProject(random, false);
        String cleanupsPath = PathUtils.getPath(projectPath, "cleanup");
        String cleanupPath = PathUtils.getPath(cleanupsPath, "default");
        xmlRpcHelper.deleteConfig(cleanupPath);

        loginAsAdmin();

        ListPage cleanupsPage = new ListPage(selenium, urls, cleanupsPath);
        cleanupsPage.goTo();
        cleanupsPage.assertItemPresent("default", ListPage.ANNOTATION_HIDDEN, ACTION_RESTORE);
        cleanupsPage.expandTreeNode(cleanupsPath);
        cleanupsPage.assertTreeLinkNotPresent("default");

        cleanupsPage.clickRestore("default");
        cleanupsPage.waitFor();
        cleanupsPage.assertItemPresent("default", ListPage.ANNOTATION_INHERITED, AccessManager.ACTION_VIEW, AccessManager.ACTION_CLONE, AccessManager.ACTION_DELETE);
        cleanupsPage.expandTreeNode(cleanupsPath);
        cleanupsPage.assertTreeLinkPresent("default");
    }

    public void testHideListItem() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";

        String parentPath = xmlRpcHelper.insertSimpleProject(parentName, true);
        String childPath = xmlRpcHelper.insertSimpleProject(childName, parentName, false);

        String parentReqsPath = PathUtils.getPath(parentPath, "requirements");
        String childReqsPath = PathUtils.getPath(childPath, "requirements");
        Hashtable<String, Object> req = xmlRpcHelper.createEmptyConfig(ResourceRequirement.class);
        req.put("resource", "foo");
        String parentReqPath = xmlRpcHelper.insertConfig(parentReqsPath, req);
        String baseName = PathUtils.getBaseName(parentReqPath);
        String childReqPath = PathUtils.getPath(childReqsPath, baseName);

        loginAsAdmin();

        ListPage reqsPage = new ListPage(selenium, urls, childReqsPath);
        reqsPage.goTo();
        reqsPage.assertItemPresent(baseName, ListPage.ANNOTATION_INHERITED, AccessManager.ACTION_VIEW, AccessManager.ACTION_DELETE);
        DeleteConfirmPage confirmPage = reqsPage.clickDelete(baseName);
        confirmPage.waitFor();
        confirmPage.assertTasks(childReqPath, ACTION_HIDE_RECORD);
        confirmPage.clickDelete();

        reqsPage.waitFor();
        reqsPage.assertItemPresent(baseName, ListPage.ANNOTATION_HIDDEN, ACTION_RESTORE);
        assertFalse(xmlRpcHelper.configPathExists(childReqPath));
    }

    public void testRestoreListItem() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";

        String parentPath = xmlRpcHelper.insertSimpleProject(parentName, true);
        String childPath = xmlRpcHelper.insertSimpleProject(childName, parentName, false);

        String parentReqsPath = PathUtils.getPath(parentPath, "requirements");
        String childReqsPath = PathUtils.getPath(childPath, "requirements");
        Hashtable<String, Object> req = xmlRpcHelper.createEmptyConfig(ResourceRequirement.class);
        req.put("resource", "foo");
        String parentReqPath = xmlRpcHelper.insertConfig(parentReqsPath, req);
        String baseName = PathUtils.getBaseName(parentReqPath);
        String childReqPath = PathUtils.getPath(childReqsPath, baseName);
        xmlRpcHelper.deleteConfig(childReqPath);

        loginAsAdmin();

        ListPage reqsPage = new ListPage(selenium, urls, childReqsPath);
        reqsPage.goTo();
        reqsPage.assertItemPresent(baseName, ListPage.ANNOTATION_HIDDEN, ACTION_RESTORE);
        reqsPage.clickRestore(baseName);

        reqsPage.waitFor();
        reqsPage.assertItemPresent(baseName, ListPage.ANNOTATION_INHERITED, AccessManager.ACTION_VIEW, AccessManager.ACTION_DELETE);
        assertTrue(xmlRpcHelper.configPathExists(childReqPath));
    }

    public void testDeleteSingleton() throws Exception
    {
        String projectPath = xmlRpcHelper.insertSimpleProject(random, false);

        loginAsAdmin();
        String path = PathUtils.getPath(projectPath, "scm");
        CompositePage subversionPage = new CompositePage(selenium, urls, path);
        subversionPage.goTo();
        assertTrue(subversionPage.isActionPresent(AccessManager.ACTION_DELETE));
        subversionPage.clickAction(AccessManager.ACTION_DELETE);

        DeleteConfirmPage confirmPage = new DeleteConfirmPage(selenium, urls, path, false);
        confirmPage.waitFor();
        CompositePage projectPage = confirmPage.confirmDeleteSingleton();
        projectPage.assertTreeLinkPresent("scm");
    }

    private String insertBuildCompletedTrigger(String refereePath, String refererPath) throws Exception
    {
        Hashtable<String, Object> trigger = xmlRpcHelper.createEmptyConfig(BuildCompletedTriggerConfiguration.class);
        trigger.put("name", "test");
        trigger.put("project", refereePath);
        return xmlRpcHelper.insertConfig(PathUtils.getPath(refererPath, "triggers"), trigger);
    }

    private String insertLabel(String projectPath) throws Exception
    {
        Hashtable<String, Object> label = xmlRpcHelper.createEmptyConfig(LabelConfiguration.class);
        label.put("label", "test");
        return xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, "labels"), label);
    }
}
