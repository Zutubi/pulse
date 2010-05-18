package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.admin.*;
import com.zutubi.pulse.acceptance.pages.browse.BrowsePage;
import com.zutubi.pulse.acceptance.utils.CleanupTestUtils;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.ResourceRequirementConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.BuildCompletedTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfigurationActions;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Tests for deletion of various things: an area that is notorious for bugs!
 */
public class DeleteAcceptanceTest extends SeleniumTestBase
{
    private static final String ACTION_DELETE_RECORD = "delete record";
    private static final String ACTION_DELETE_BUILDS = "delete all build results";
    private static final String ACTION_HIDE_RECORD = "hide record";

    private static final String ACTION_RESTORE = "restore";

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

    public void testDeleteListItem() throws Exception
    {
        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);
        String labelPath = insertLabel(projectPath);

        browser.loginAsAdmin();
        ListPage labelList = browser.openAndWaitFor(ListPage.class, PathUtils.getParentPath(labelPath));
        String baseName = PathUtils.getBaseName(labelPath);
        assertItemPresent(labelList, baseName, null, "view", "delete");
        DeleteConfirmPage confirmPage = labelList.clickDelete(baseName);
        confirmPage.waitFor();
        assertTasks(confirmPage, labelPath, ACTION_DELETE_RECORD);
        confirmPage.clickDelete();

        labelList.waitFor();
        assertFalse(labelList.isItemPresent(baseName));
    }

    public void testCancelDelete() throws Exception
    {
        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);
        String labelPath = insertLabel(projectPath);

        browser.loginAsAdmin();
        ListPage labelList = browser.openAndWaitFor(ListPage.class, PathUtils.getParentPath(labelPath));
        String baseName = PathUtils.getBaseName(labelPath);
        assertItemPresent(labelList, baseName, null, "view", "delete");
        DeleteConfirmPage confirmPage = labelList.clickDelete(baseName);
        confirmPage.waitFor();
        confirmPage.clickCancel();

        labelList.waitFor();
        assertItemPresent(labelList, baseName, null);
    }

    public void testCancelDeleteProject() throws Exception
    {
        xmlRpcHelper.insertTrivialProject(random, false);
        browser.loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, random, false);
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        confirmPage.clickCancel();
        hierarchyPage.waitFor();
    }

    public void testDeleteProject() throws Exception
    {
        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);

        browser.loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, random, false);
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        assertTasks(confirmPage, projectPath, ACTION_DELETE_RECORD, projectPath, ACTION_DELETE_BUILDS);
        confirmPage.clickDelete();
        ProjectHierarchyPage global = browser.createPage(ProjectHierarchyPage.class, ProjectManager.GLOBAL_PROJECT_NAME, true);
        global.waitFor();
        assertElementNotPresent("link=" + random);

        BrowsePage browsePage = browser.openAndWaitFor(BrowsePage.class);
        assertFalse(browsePage.isProjectPresent(null, random));
    }

    public void testDeleteProjectWithReference() throws Exception
    {
        String refererName = random + "-er";
        String refereeName = random + "-ee";
        String refererPath = xmlRpcHelper.insertTrivialProject(refererName, false);
        String refereePath = xmlRpcHelper.insertTrivialProject(refereeName, false);

        String triggerPath = insertBuildCompletedTrigger(refereePath, refererPath);

        browser.loginAsAdmin();
        ListPage triggersPage = browser.openAndWaitFor(ListPage.class, PathUtils.getParentPath(triggerPath));
        assertItemPresent(triggersPage, "test", null);

        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, refereeName, false);
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        assertTasks(confirmPage, refereePath, ACTION_DELETE_RECORD, refereePath, ACTION_DELETE_BUILDS, triggerPath, ACTION_DELETE_RECORD);
        confirmPage.clickDelete();

        browser.waitFor(ProjectHierarchyPage.class, ProjectManager.GLOBAL_PROJECT_NAME, true);

        triggersPage.openAndWaitFor();
        assertFalse(triggersPage.isItemPresent("test"));
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

        browser.login(authLogin, "");
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, random, false);
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        assertTasks(confirmPage, projectPath, ACTION_DELETE_RECORD, projectPath, ACTION_DELETE_BUILDS, PathUtils.getPath(dashboardPath, "shownProjects", "0"), "remove reference");
        assertTextNotPresent("A further");
        confirmPage.clickCancel();
        hierarchyPage.waitFor();
        browser.logout();

        browser.login(noAuthLogin, "");
        hierarchyPage.openAndWaitFor();
        confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        assertTasks(confirmPage, projectPath, ACTION_DELETE_RECORD, projectPath, ACTION_DELETE_BUILDS);
        assertTextPresent("A further task is required that is not visible to you with your current permissions");
        confirmPage.clickDelete();

        ProjectHierarchyPage globalPage = browser.createPage(ProjectHierarchyPage.class, ProjectManager.GLOBAL_PROJECT_NAME, true);
        globalPage.waitFor();

        dashboard = xmlRpcHelper.getConfig(dashboardPath);
        assertEquals(0, ((Vector) dashboard.get("shownProjects")).size());

        // Make sure that it is not missing just because the reference is now
        // broken
        assertTrue(xmlRpcHelper.isConfigValid(dashboardPath));
    }

    public void testDeleteAgent() throws Exception
    {
        deleteAgentHelper(random);
    }

    public void testDeleteAgentWithSpecialCharacters() throws Exception
    {
        deleteAgentHelper(random + " ~!#%^&*()_+<>?.,\":';][}{'|=-`");
    }

    private void deleteAgentHelper(String name) throws Exception
    {
        String path = xmlRpcHelper.insertSimpleAgent(name, random);
        browser.loginAsAdmin();
        AgentHierarchyPage hierarchyPage = browser.openAndWaitFor(AgentHierarchyPage.class, name, false);
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        assertTasks(confirmPage, path, ACTION_DELETE_RECORD, path, "delete agent state");
        confirmPage.clickDelete();

        browser.waitFor(AgentHierarchyPage.class, AgentManager.GLOBAL_AGENT_NAME, true);

        assertFalse(xmlRpcHelper.configPathExists(path));
    }

    public void testDeleteAgentWithBuildStageReference() throws Exception
    {
        String agentName = "agent-" + random;
        String agentPath = xmlRpcHelper.insertSimpleAgent(agentName);
        String projectName = "project-" + random;
        String projectPath = xmlRpcHelper.insertSimpleProject(projectName, false);

        Hashtable<String, Object> stage = xmlRpcHelper.createEmptyConfig(BuildStageConfiguration.class);
        stage.put("name", "agent");
        stage.put("agent", agentPath);
        stage.put("recipe", "");
        String stagePath = xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, "stages"), stage);

        browser.loginAsAdmin();
        AgentHierarchyPage hierarchyPage = browser.openAndWaitFor(AgentHierarchyPage.class, agentName, false);
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        assertTasks(confirmPage, agentPath, ACTION_DELETE_RECORD, agentPath, "delete agent state", PathUtils.getPath(stagePath, "agent"), "null out reference");
        confirmPage.clickDelete();

        AgentHierarchyPage globalPage = browser.createPage(AgentHierarchyPage.class, AgentManager.GLOBAL_AGENT_NAME, true);
        globalPage.waitFor();
        assertFalse(xmlRpcHelper.configPathExists(agentPath));

        assertTrue(xmlRpcHelper.isConfigValid(stagePath));
        stage = xmlRpcHelper.getConfig(stagePath);
        assertNull(stage.get("agent"));
    }

    public void testDeleteUser() throws Exception
    {
        String path = xmlRpcHelper.insertTrivialUser(random);
        browser.loginAsAdmin();

        ListPage usersPage = browser.openAndWaitFor(ListPage.class, MasterConfigurationRegistry.USERS_SCOPE);
        assertItemPresent(usersPage, random, null, AccessManager.ACTION_VIEW, AccessManager.ACTION_DELETE, UserConfigurationActions.ACTION_SET_PASSWORD);
        DeleteConfirmPage confirmPage = usersPage.clickDelete(random);
        confirmPage.waitFor();
        assertTasks(confirmPage, path, ACTION_DELETE_RECORD, path, "delete user state");
        confirmPage.clickDelete();

        usersPage.waitFor();
        assertFalse(usersPage.isItemPresent(random));
    }

    public void testHideMapItem() throws Exception
    {
        String templateProjectName = random + "-parent";
        setupTemplateProjectWithDefaultCleanup(templateProjectName);

        String projectPath = xmlRpcHelper.insertSimpleProject(random, templateProjectName, false);

        browser.loginAsAdmin();
        String cleanupsPath = PathUtils.getPath(projectPath, "cleanup");
        String cleanupPath = PathUtils.getPath(cleanupsPath, "default");

        ListPage cleanupsPage = browser.openAndWaitFor(ListPage.class, cleanupsPath);
        cleanupsPage.expandTreeNode(cleanupsPath);
        assertItemPresent(cleanupsPage, "default", ListPage.ANNOTATION_INHERITED, AccessManager.ACTION_VIEW, ConfigurationRefactoringManager.ACTION_CLONE, AccessManager.ACTION_DELETE);
        assertTrue(cleanupsPage.isTreeLinkPresent("default"));

        DeleteConfirmPage confirmPage = cleanupsPage.clickDelete("default");
        confirmPage.waitFor();
        assertTextPresent("Are you sure you wish to hide this record?");
        assertTasks(confirmPage, cleanupPath, ACTION_HIDE_RECORD);
        confirmPage.clickDelete();

        cleanupsPage.waitFor();
        assertItemPresent(cleanupsPage, "default", ListPage.ANNOTATION_HIDDEN, ACTION_RESTORE);
        assertFalse(cleanupsPage.isTreeLinkPresent("default"));
    }

    public void testCancelHide() throws Exception
    {
        String templateProjectName = random + "-parent";
        setupTemplateProjectWithDefaultCleanup(templateProjectName);

        String projectPath = xmlRpcHelper.insertSimpleProject(random, templateProjectName, false);

        browser.loginAsAdmin();
        String cleanupssPath = PathUtils.getPath(projectPath, "cleanup");

        ListPage cleanupsPage = browser.openAndWaitFor(ListPage.class, cleanupssPath);
        assertItemPresent(cleanupsPage, "default", ListPage.ANNOTATION_INHERITED, AccessManager.ACTION_VIEW, ConfigurationRefactoringManager.ACTION_CLONE, AccessManager.ACTION_DELETE);

        DeleteConfirmPage confirmPage = cleanupsPage.clickDelete("default");
        confirmPage.waitFor();
        confirmPage.clickCancel();

        cleanupsPage.waitFor();
        assertItemPresent(cleanupsPage, "default", ListPage.ANNOTATION_INHERITED, AccessManager.ACTION_VIEW, ConfigurationRefactoringManager.ACTION_CLONE, AccessManager.ACTION_DELETE);
    }

    private void setupTemplateProjectWithDefaultCleanup(String name) throws Exception
    {
        xmlRpcHelper.insertSimpleProject(name, true);

        CleanupTestUtils cleanupUtils = new CleanupTestUtils(xmlRpcHelper);
        cleanupUtils.addCleanupRule(name, "default");
    }

    public void testHideMapItemWithSkeletonDescendant() throws Exception
    {
        String grandParentName = random + "-grandparent";
        String parentName = random + "-parent";
        String childName = random + "-child";

        setupTemplateProjectWithDefaultCleanup(grandParentName);

        String parentPath = xmlRpcHelper.insertSimpleProject(parentName, grandParentName, true);
        String parentCleanupsPath = PathUtils.getPath(parentPath, "cleanup");
        String parentCleanupPath = PathUtils.getPath(parentCleanupsPath, "default");
        String childPath = xmlRpcHelper.insertSimpleProject(childName, parentName, false);
        String childCleanupPath = PathUtils.getPath(childPath, "cleanup", "default");

        browser.loginAsAdmin();

        ListPage cleanupsPage = browser.openAndWaitFor(ListPage.class, parentCleanupsPath);
        assertItemPresent(cleanupsPage, "default", ListPage.ANNOTATION_INHERITED, AccessManager.ACTION_VIEW, ConfigurationRefactoringManager.ACTION_CLONE, AccessManager.ACTION_DELETE);

        DeleteConfirmPage confirmPage = cleanupsPage.clickDelete("default");
        confirmPage.waitFor();
        assertTextPresent("Are you sure you wish to hide this record?");
        assertTasks(confirmPage, parentCleanupPath, ACTION_HIDE_RECORD);
        confirmPage.clickDelete();

        cleanupsPage.waitFor();
        assertItemPresent(cleanupsPage, "default", ListPage.ANNOTATION_HIDDEN, ACTION_RESTORE);
        assertFalse(xmlRpcHelper.configPathExists(childCleanupPath));
    }

    public void testHideMapItemWithDescendantOverride() throws Exception
    {
        String grandParentName = random + "-grandparent";
        String parentName = random + "-parent";
        String childName = random + "-child";

        setupTemplateProjectWithDefaultCleanup(grandParentName);

        String parentPath = xmlRpcHelper.insertSimpleProject(parentName, grandParentName, true);
        String parentCleanupsPath = PathUtils.getPath(parentPath, "cleanup");
        String parentCleanupPath = PathUtils.getPath(parentCleanupsPath, "default");
        String childPath = xmlRpcHelper.insertSimpleProject(childName, parentName, false);
        String childCleanupPath = PathUtils.getPath(childPath, "cleanup", "default");
        Hashtable<String, Object> childCleanup = xmlRpcHelper.getConfig(childCleanupPath);
        childCleanup.put("retain", 928);
        xmlRpcHelper.saveConfig(childCleanupPath, childCleanup, false);

        browser.loginAsAdmin();

        ListPage cleanupsPage = browser.openAndWaitFor(ListPage.class, parentCleanupsPath);
        assertItemPresent(cleanupsPage, "default", ListPage.ANNOTATION_INHERITED, AccessManager.ACTION_VIEW, ConfigurationRefactoringManager.ACTION_CLONE, AccessManager.ACTION_DELETE);

        DeleteConfirmPage confirmPage = cleanupsPage.clickDelete("default");
        confirmPage.waitFor();
        assertTextPresent("Are you sure you wish to hide this record?");
        assertTasks(confirmPage, parentCleanupPath, ACTION_HIDE_RECORD, childCleanupPath, ACTION_DELETE_RECORD);
        confirmPage.clickDelete();

        cleanupsPage.waitFor();
        assertItemPresent(cleanupsPage, "default", ListPage.ANNOTATION_HIDDEN, ACTION_RESTORE);
        assertFalse(xmlRpcHelper.configPathExists(childCleanupPath));
    }

    public void testRestoreMapItem() throws Exception
    {
        String parentName = random + "-parent";

        setupTemplateProjectWithDefaultCleanup(parentName);

        String projectPath = xmlRpcHelper.insertSimpleProject(random, parentName, false);
        String cleanupsPath = PathUtils.getPath(projectPath, "cleanup");
        String cleanupPath = PathUtils.getPath(cleanupsPath, "default");
        xmlRpcHelper.deleteConfig(cleanupPath);

        browser.loginAsAdmin();

        ListPage cleanupsPage = browser.openAndWaitFor(ListPage.class, cleanupsPath);
        assertItemPresent(cleanupsPage, "default", ListPage.ANNOTATION_HIDDEN, ACTION_RESTORE);
        cleanupsPage.expandTreeNode(cleanupsPath);
        assertFalse(cleanupsPage.isTreeLinkPresent("default"));

        cleanupsPage.clickRestore("default");
        cleanupsPage.waitFor();
        assertItemPresent(cleanupsPage, "default", ListPage.ANNOTATION_INHERITED, AccessManager.ACTION_VIEW, ConfigurationRefactoringManager.ACTION_CLONE, AccessManager.ACTION_DELETE);
        cleanupsPage.expandTreeNode(cleanupsPath);
        assertTrue(cleanupsPage.isTreeLinkPresent("default"));
    }

    public void testHideListItem() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";

        String parentPath = xmlRpcHelper.insertSimpleProject(parentName, true);
        String childPath = xmlRpcHelper.insertSimpleProject(childName, parentName, false);

        String parentReqsPath = PathUtils.getPath(parentPath, "requirements");
        String childReqsPath = PathUtils.getPath(childPath, "requirements");
        Hashtable<String, Object> req = xmlRpcHelper.createEmptyConfig(ResourceRequirementConfiguration.class);
        req.put("resource", "foo");
        String parentReqPath = xmlRpcHelper.insertConfig(parentReqsPath, req);
        String baseName = PathUtils.getBaseName(parentReqPath);
        String childReqPath = PathUtils.getPath(childReqsPath, baseName);

        browser.loginAsAdmin();

        ListPage reqsPage = browser.openAndWaitFor(ListPage.class, childReqsPath);
        assertItemPresent(reqsPage, baseName, ListPage.ANNOTATION_INHERITED, AccessManager.ACTION_VIEW, AccessManager.ACTION_DELETE);
        DeleteConfirmPage confirmPage = reqsPage.clickDelete(baseName);
        confirmPage.waitFor();
        assertTasks(confirmPage, childReqPath, ACTION_HIDE_RECORD);
        confirmPage.clickDelete();

        reqsPage.waitFor();
        assertItemPresent(reqsPage, baseName, ListPage.ANNOTATION_HIDDEN, ACTION_RESTORE);
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
        Hashtable<String, Object> req = xmlRpcHelper.createEmptyConfig(ResourceRequirementConfiguration.class);
        req.put("resource", "foo");
        String parentReqPath = xmlRpcHelper.insertConfig(parentReqsPath, req);
        String baseName = PathUtils.getBaseName(parentReqPath);
        String childReqPath = PathUtils.getPath(childReqsPath, baseName);
        xmlRpcHelper.deleteConfig(childReqPath);

        browser.loginAsAdmin();

        ListPage reqsPage = browser.openAndWaitFor(ListPage.class, childReqsPath);
        assertItemPresent(reqsPage, baseName, ListPage.ANNOTATION_HIDDEN, ACTION_RESTORE);
        reqsPage.clickRestore(baseName);

        reqsPage.waitFor();
        assertItemPresent(reqsPage, baseName, ListPage.ANNOTATION_INHERITED, AccessManager.ACTION_VIEW, AccessManager.ACTION_DELETE);
        assertTrue(xmlRpcHelper.configPathExists(childReqPath));
    }

    public void testDeleteSingleton() throws Exception
    {
        String projectPath = xmlRpcHelper.insertSimpleProject(random, false);

        browser.loginAsAdmin();
        String path = PathUtils.getPath(projectPath, "scm");
        CompositePage subversionPage = browser.openAndWaitFor(CompositePage.class, path);
        assertTrue(subversionPage.isActionPresent(AccessManager.ACTION_DELETE));
        subversionPage.clickAction(AccessManager.ACTION_DELETE);

        DeleteConfirmPage confirmPage = browser.createPage(DeleteConfirmPage.class, path, false);
        confirmPage.waitFor();
        CompositePage projectPage = confirmPage.confirmDeleteSingleton();
        assertTrue(projectPage.isTreeLinkPresent("scm"));
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

    public void assertTasks(DeleteConfirmPage page, String... pathActionPairs)
    {
        if (pathActionPairs.length % 2 != 0)
        {
            fail("Tasks must be made up of (path, action) pairs");
        }

        int i;
        for (i = 0; i < pathActionPairs.length / 2; i++)
        {
            assertEquals(pathActionPairs[i * 2], browser.getCellContents(page.getId(), i + 1, 0));
            assertEquals(pathActionPairs[i * 2 + 1], browser.getCellContents(page.getId(), i + 1, 1));
        }

        String actionsCell = browser.getCellContents(page.getId(), i + 1, 0);
        actionsCell = actionsCell.replaceAll(" +", " ");
        assertEquals((page.isHide() ? "hide" : "delete") + " cancel", actionsCell);
    }

}
