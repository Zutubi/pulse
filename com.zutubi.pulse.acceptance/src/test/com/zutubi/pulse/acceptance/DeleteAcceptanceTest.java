package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.admin.*;
import com.zutubi.pulse.acceptance.pages.browse.BrowsePage;
import com.zutubi.pulse.acceptance.utils.CleanupTestUtils;
import com.zutubi.pulse.core.scm.config.api.CheckoutScheme;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;
import com.zutubi.pulse.master.tove.config.project.ResourceRequirementConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.BuildCompletedTriggerConfiguration;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.Condition;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.util.*;

import static com.zutubi.pulse.acceptance.pages.admin.ListPage.*;
import static com.zutubi.pulse.master.tove.config.user.UserConfigurationActions.ACTION_SET_PASSWORD;
import static com.zutubi.tove.config.ConfigurationRefactoringManager.ACTION_CLONE;
import static com.zutubi.tove.security.AccessManager.ACTION_DELETE;
import static com.zutubi.tove.security.AccessManager.ACTION_VIEW;
import static com.zutubi.tove.type.record.PathUtils.getPath;

/**
 * Tests for deletion of various things: an area that is notorious for bugs!
 */
public class DeleteAcceptanceTest extends AcceptanceTestBase
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

        getBrowser().loginAsAdmin();
        ListPage labelList = getBrowser().openAndWaitFor(ListPage.class, PathUtils.getParentPath(labelPath));
        String baseName = PathUtils.getBaseName(labelPath);
        assertTrue(labelList.isItemPresent(baseName, ANNOTATION_NONE, ACTION_VIEW, ACTION_DELETE));
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

        getBrowser().loginAsAdmin();
        ListPage labelList = getBrowser().openAndWaitFor(ListPage.class, PathUtils.getParentPath(labelPath));
        String baseName = PathUtils.getBaseName(labelPath);
        assertTrue(labelList.isItemPresent(baseName, ANNOTATION_NONE, ACTION_VIEW, ACTION_DELETE));
        DeleteConfirmPage confirmPage = labelList.clickDelete(baseName);
        confirmPage.waitFor();
        confirmPage.clickCancel();

        labelList.waitFor();
        assertTrue(labelList.isItemPresent(baseName, ANNOTATION_NONE));
    }

    public void testCancelDeleteProject() throws Exception
    {
        xmlRpcHelper.insertTrivialProject(random, false);
        getBrowser().loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, random, false);
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        confirmPage.clickCancel();
        hierarchyPage.waitFor();
    }

    public void testDeleteProject() throws Exception
    {
        // Set up the project to build on the master agent, using a custom
        // persistent work directory.
        String projectPath = xmlRpcHelper.insertSimpleProject(random, false);
        File buildDirectory = runBuildInPersistentWorkDirectory();
        
        getBrowser().loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, random, false);
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        assertTasks(confirmPage, projectPath, ACTION_DELETE_RECORD, projectPath, ACTION_DELETE_BUILDS);
        confirmPage.clickDelete();
        ProjectHierarchyPage global = getBrowser().createPage(ProjectHierarchyPage.class, ProjectManager.GLOBAL_PROJECT_NAME, true);
        global.waitFor();
        assertFalse(getBrowser().isElementIdPresent("link=" + random));

        BrowsePage browsePage = getBrowser().openAndWaitFor(BrowsePage.class);
        assertFalse(browsePage.isProjectPresent(null, random));

        waitForDirectoryToBeCleaned(buildDirectory);
    }

    public void testDeleteStage() throws Exception
    {
        String projectPath = xmlRpcHelper.insertSimpleProject(random, false);
        File buildDirectory = runBuildInPersistentWorkDirectory();

        xmlRpcHelper.deleteConfig(getPath(projectPath, Constants.Project.STAGES, ProjectConfigurationWizard.DEFAULT_STAGE));

        waitForDirectoryToBeCleaned(buildDirectory);
    }

    public void testEditPersistentWorkDirPattern() throws Exception
    {
        xmlRpcHelper.insertSimpleProject(random, false);
        File buildDirectory = runBuildInPersistentWorkDirectory();

        setCustomPersistentDir("$(agent.data.dir)/work/$(project.handle)/$(stage)");

        waitForDirectoryToBeCleaned(buildDirectory);
    }
    
    private File runBuildInPersistentWorkDirectory() throws Exception
    {
        // Set up the project to build on the master agent, using a custom
        // persistent work directory.
        setIncrementalUpdate();
        setMasterAgent();
        setCustomPersistentDir("$(agent.data.dir)/work/$(project)/$(stage)");

        // Run a build, make sure the directory we expect appeared.
        xmlRpcHelper.runBuild(random, XmlRpcHelper.BUILD_TIMEOUT);

        File agentsDir = new File(AcceptanceTestUtils.getDataDirectory(), "agents");
        final File buildDirectory = new File(agentsDir, FileSystemUtils.composeFilename(getMasterAgentId(agentsDir), "work", random, ProjectConfigurationWizard.DEFAULT_STAGE));
        assertTrue(buildDirectory.isDirectory());
        return buildDirectory;
    }

    private void setIncrementalUpdate() throws Exception
    {
        String scmPath = getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, random, Constants.Project.SCM);
        Hashtable<String, Object> scm = xmlRpcHelper.getConfig(scmPath);
        scm.put(Constants.Project.Scm.CHECKOUT_SCHEME, CheckoutScheme.INCREMENTAL_UPDATE.name());
        xmlRpcHelper.saveConfig(scmPath, scm, false);
        xmlRpcHelper.waitForProjectToInitialise(random);
    }

    private void setMasterAgent() throws Exception
    {
        String stagePath = getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, random, Constants.Project.STAGES, ProjectConfigurationWizard.DEFAULT_STAGE);
        Hashtable<String, Object> stage = xmlRpcHelper.getConfig(stagePath);
        stage.put(Constants.Project.Stage.AGENT, getPath(MasterConfigurationRegistry.AGENTS_SCOPE, AgentManager.MASTER_AGENT_NAME));
        xmlRpcHelper.saveConfig(stagePath, stage, false);
    }

    private void setCustomPersistentDir(String pattern) throws Exception
    {
        String optionsPath = getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, random, Constants.Project.OPTIONS);
        Hashtable<String, Object> options = xmlRpcHelper.getConfig(optionsPath);
        options.put("persistentWorkDir", pattern);
        xmlRpcHelper.saveConfig(optionsPath, options, false);
    }

    private String getMasterAgentId(File agentsDir)
    {
        List<String> agentDirs = new LinkedList<String>(Arrays.asList(agentsDir.list()));
        Collections.sort(agentDirs, new Comparator<String>()
        {
            public int compare(String o1, String o2)
            {
                return (int) (Long.parseLong(o1) - Long.parseLong(o2));
            }
        });

        return agentDirs.get(0);
    }

    private void waitForDirectoryToBeCleaned(final File buildDirectory)
    {
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return !buildDirectory.isDirectory();
            }
        }, 60000, "agent persistent directory for project to be cleaned up");
    }

    public void testDeleteProjectWithReference() throws Exception
    {
        String refererName = random + "-er";
        String refereeName = random + "-ee";
        String refererPath = xmlRpcHelper.insertTrivialProject(refererName, false);
        String refereePath = xmlRpcHelper.insertTrivialProject(refereeName, false);

        String triggerPath = insertBuildCompletedTrigger(refereePath, refererPath);

        getBrowser().loginAsAdmin();
        ListPage triggersPage = getBrowser().openAndWaitFor(ListPage.class, PathUtils.getParentPath(triggerPath));
        assertTrue(triggersPage.isItemPresent("test", ANNOTATION_NONE));

        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, refereeName, false);
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        assertTasks(confirmPage, refereePath, ACTION_DELETE_RECORD, refereePath, ACTION_DELETE_BUILDS, triggerPath, ACTION_DELETE_RECORD);
        confirmPage.clickDelete();

        getBrowser().waitFor(ProjectHierarchyPage.class, ProjectManager.GLOBAL_PROJECT_NAME, true);

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

        String dashboardPath = getPath(authPath, "preferences", "dashboard");
        Hashtable<String, Object> dashboard = xmlRpcHelper.getConfig(dashboardPath);
        dashboard.put("showAllProjects", false);
        dashboard.put("shownProjects", new Vector<String>(Arrays.asList(projectPath)));
        xmlRpcHelper.saveConfig(dashboardPath, dashboard, false);

        assertTrue(getBrowser().login(authLogin, ""));
        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, random, false);
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        assertTasks(confirmPage, projectPath, ACTION_DELETE_RECORD, projectPath, ACTION_DELETE_BUILDS, getPath(dashboardPath, "shownProjects", "0"), "remove reference");
        assertFalse(getBrowser().isTextPresent("A further"));
        confirmPage.clickCancel();
        hierarchyPage.waitFor();
        getBrowser().logout();

        assertTrue(getBrowser().login(noAuthLogin, ""));
        hierarchyPage.openAndWaitFor();
        confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        assertTasks(confirmPage, projectPath, ACTION_DELETE_RECORD, projectPath, ACTION_DELETE_BUILDS);
        assertTrue(getBrowser().isTextPresent("A further task is required that is not visible to you with your current permissions"));
        confirmPage.clickDelete();

        ProjectHierarchyPage globalPage = getBrowser().createPage(ProjectHierarchyPage.class, ProjectManager.GLOBAL_PROJECT_NAME, true);
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
        getBrowser().loginAsAdmin();
        AgentHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(AgentHierarchyPage.class, name, false);
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        assertTasks(confirmPage, path, ACTION_DELETE_RECORD, path, "delete agent state");
        confirmPage.clickDelete();

        getBrowser().waitFor(AgentHierarchyPage.class, AgentManager.GLOBAL_AGENT_NAME, true);

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
        String stagePath = xmlRpcHelper.insertConfig(getPath(projectPath, "stages"), stage);

        getBrowser().loginAsAdmin();
        AgentHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(AgentHierarchyPage.class, agentName, false);
        DeleteConfirmPage confirmPage = hierarchyPage.clickDelete();
        confirmPage.waitFor();
        assertTasks(confirmPage, agentPath, ACTION_DELETE_RECORD, agentPath, "delete agent state", getPath(stagePath, "agent"), "null out reference");
        confirmPage.clickDelete();

        AgentHierarchyPage globalPage = getBrowser().createPage(AgentHierarchyPage.class, AgentManager.GLOBAL_AGENT_NAME, true);
        globalPage.waitFor();
        assertFalse(xmlRpcHelper.configPathExists(agentPath));

        assertTrue(xmlRpcHelper.isConfigValid(stagePath));
        stage = xmlRpcHelper.getConfig(stagePath);
        assertNull(stage.get("agent"));
    }

    public void testDeleteUser() throws Exception
    {
        String path = xmlRpcHelper.insertTrivialUser(random);
        getBrowser().loginAsAdmin();

        ListPage usersPage = getBrowser().openAndWaitFor(ListPage.class, MasterConfigurationRegistry.USERS_SCOPE);
        assertTrue(usersPage.isItemPresent(random, ANNOTATION_NONE, ACTION_VIEW, ACTION_DELETE, ACTION_SET_PASSWORD));
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

        getBrowser().loginAsAdmin();
        String cleanupsPath = getPath(projectPath, "cleanup");
        String cleanupPath = getPath(cleanupsPath, "default");

        ListPage cleanupsPage = getBrowser().openAndWaitFor(ListPage.class, cleanupsPath);
        cleanupsPage.expandTreeNode(cleanupsPath);
        assertTrue(cleanupsPage.isItemPresent("default", ANNOTATION_INHERITED, ACTION_VIEW, ACTION_CLONE, ACTION_DELETE));
        assertTrue(cleanupsPage.isTreeLinkPresent("default"));

        DeleteConfirmPage confirmPage = cleanupsPage.clickDelete("default");
        confirmPage.waitFor();
        assertTrue(getBrowser().isTextPresent("Are you sure you wish to hide this record?"));
        assertTasks(confirmPage, cleanupPath, ACTION_HIDE_RECORD);
        confirmPage.clickDelete();

        cleanupsPage.waitFor();
        assertTrue(cleanupsPage.isItemPresent("default", ANNOTATION_HIDDEN, ACTION_RESTORE));
        assertFalse(cleanupsPage.isTreeLinkPresent("default"));
    }

    public void testCancelHide() throws Exception
    {
        String templateProjectName = random + "-parent";
        setupTemplateProjectWithDefaultCleanup(templateProjectName);

        String projectPath = xmlRpcHelper.insertSimpleProject(random, templateProjectName, false);

        getBrowser().loginAsAdmin();
        String cleanupsPath = getPath(projectPath, "cleanup");

        ListPage cleanupsPage = getBrowser().openAndWaitFor(ListPage.class, cleanupsPath);
        assertTrue(cleanupsPage.isItemPresent("default", ANNOTATION_INHERITED, ACTION_VIEW, ACTION_CLONE, ACTION_DELETE));

        DeleteConfirmPage confirmPage = cleanupsPage.clickDelete("default");
        confirmPage.waitFor();
        confirmPage.clickCancel();

        cleanupsPage.waitFor();
        assertTrue(cleanupsPage.isItemPresent("default", ANNOTATION_INHERITED, ACTION_VIEW, ACTION_CLONE, ACTION_DELETE));
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
        String parentCleanupsPath = getPath(parentPath, "cleanup");
        String parentCleanupPath = getPath(parentCleanupsPath, "default");
        String childPath = xmlRpcHelper.insertSimpleProject(childName, parentName, false);
        String childCleanupPath = getPath(childPath, "cleanup", "default");

        getBrowser().loginAsAdmin();

        ListPage cleanupsPage = getBrowser().openAndWaitFor(ListPage.class, parentCleanupsPath);
        assertTrue(cleanupsPage.isItemPresent("default", ANNOTATION_INHERITED, ACTION_VIEW, ACTION_CLONE, ACTION_DELETE));

        DeleteConfirmPage confirmPage = cleanupsPage.clickDelete("default");
        confirmPage.waitFor();
        assertTrue(getBrowser().isTextPresent("Are you sure you wish to hide this record?"));
        assertTasks(confirmPage, parentCleanupPath, ACTION_HIDE_RECORD);
        confirmPage.clickDelete();

        cleanupsPage.waitFor();
        assertTrue(cleanupsPage.isItemPresent("default", ANNOTATION_HIDDEN, ACTION_RESTORE));
        assertFalse(xmlRpcHelper.configPathExists(childCleanupPath));
    }

    public void testHideMapItemWithDescendantOverride() throws Exception
    {
        String grandParentName = random + "-grandparent";
        String parentName = random + "-parent";
        String childName = random + "-child";

        setupTemplateProjectWithDefaultCleanup(grandParentName);

        String parentPath = xmlRpcHelper.insertSimpleProject(parentName, grandParentName, true);
        String parentCleanupsPath = getPath(parentPath, "cleanup");
        String parentCleanupPath = getPath(parentCleanupsPath, "default");
        String childPath = xmlRpcHelper.insertSimpleProject(childName, parentName, false);
        String childCleanupPath = getPath(childPath, "cleanup", "default");
        Hashtable<String, Object> childCleanup = xmlRpcHelper.getConfig(childCleanupPath);
        childCleanup.put("retain", 928);
        xmlRpcHelper.saveConfig(childCleanupPath, childCleanup, false);

        getBrowser().loginAsAdmin();

        ListPage cleanupsPage = getBrowser().openAndWaitFor(ListPage.class, parentCleanupsPath);
        assertTrue(cleanupsPage.isItemPresent("default", ANNOTATION_INHERITED, ACTION_VIEW, ACTION_CLONE, ACTION_DELETE));

        DeleteConfirmPage confirmPage = cleanupsPage.clickDelete("default");
        confirmPage.waitFor();
        assertTrue(getBrowser().isTextPresent("Are you sure you wish to hide this record?"));
        assertTasks(confirmPage, parentCleanupPath, ACTION_HIDE_RECORD, childCleanupPath, ACTION_DELETE_RECORD);
        confirmPage.clickDelete();

        cleanupsPage.waitFor();
        assertTrue(cleanupsPage.isItemPresent("default", ANNOTATION_HIDDEN, ACTION_RESTORE));
        assertFalse(xmlRpcHelper.configPathExists(childCleanupPath));
    }

    public void testRestoreMapItem() throws Exception
    {
        String parentName = random + "-parent";

        setupTemplateProjectWithDefaultCleanup(parentName);

        String projectPath = xmlRpcHelper.insertSimpleProject(random, parentName, false);
        String cleanupsPath = getPath(projectPath, "cleanup");
        String cleanupPath = getPath(cleanupsPath, "default");
        xmlRpcHelper.deleteConfig(cleanupPath);

        getBrowser().loginAsAdmin();

        ListPage cleanupsPage = getBrowser().openAndWaitFor(ListPage.class, cleanupsPath);
        assertTrue(cleanupsPage.isItemPresent("default", ANNOTATION_HIDDEN, ACTION_RESTORE));
        cleanupsPage.expandTreeNode(cleanupsPath);
        assertFalse(cleanupsPage.isTreeLinkPresent("default"));

        cleanupsPage.clickRestore("default");
        cleanupsPage.waitFor();
        assertTrue(cleanupsPage.isItemPresent("default", ANNOTATION_INHERITED, ACTION_VIEW, ACTION_CLONE, ACTION_DELETE));
        cleanupsPage.expandTreeNode(cleanupsPath);
        assertTrue(cleanupsPage.isTreeLinkPresent("default"));
    }

    public void testHideListItem() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";

        String parentPath = xmlRpcHelper.insertSimpleProject(parentName, true);
        String childPath = xmlRpcHelper.insertSimpleProject(childName, parentName, false);

        String parentReqsPath = getPath(parentPath, "requirements");
        String childReqsPath = getPath(childPath, "requirements");
        Hashtable<String, Object> req = xmlRpcHelper.createEmptyConfig(ResourceRequirementConfiguration.class);
        req.put("resource", "foo");
        String parentReqPath = xmlRpcHelper.insertConfig(parentReqsPath, req);
        String baseName = PathUtils.getBaseName(parentReqPath);
        String childReqPath = getPath(childReqsPath, baseName);

        getBrowser().loginAsAdmin();

        ListPage reqsPage = getBrowser().openAndWaitFor(ListPage.class, childReqsPath);
        assertTrue(reqsPage.isItemPresent(baseName, ANNOTATION_INHERITED, ACTION_VIEW, ACTION_DELETE));
        DeleteConfirmPage confirmPage = reqsPage.clickDelete(baseName);
        confirmPage.waitFor();
        assertTasks(confirmPage, childReqPath, ACTION_HIDE_RECORD);
        confirmPage.clickDelete();

        reqsPage.waitFor();
        assertTrue(reqsPage.isItemPresent(baseName, ANNOTATION_HIDDEN, ACTION_RESTORE));
        assertFalse(xmlRpcHelper.configPathExists(childReqPath));
    }

    public void testRestoreListItem() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";

        String parentPath = xmlRpcHelper.insertSimpleProject(parentName, true);
        String childPath = xmlRpcHelper.insertSimpleProject(childName, parentName, false);

        String parentReqsPath = getPath(parentPath, "requirements");
        String childReqsPath = getPath(childPath, "requirements");
        Hashtable<String, Object> req = xmlRpcHelper.createEmptyConfig(ResourceRequirementConfiguration.class);
        req.put("resource", "foo");
        String parentReqPath = xmlRpcHelper.insertConfig(parentReqsPath, req);
        String baseName = PathUtils.getBaseName(parentReqPath);
        String childReqPath = getPath(childReqsPath, baseName);
        xmlRpcHelper.deleteConfig(childReqPath);

        getBrowser().loginAsAdmin();

        ListPage reqsPage = getBrowser().openAndWaitFor(ListPage.class, childReqsPath);
        assertTrue(reqsPage.isItemPresent(baseName, ANNOTATION_HIDDEN, ACTION_RESTORE));
        reqsPage.clickRestore(baseName);

        reqsPage.waitFor();
        assertTrue(reqsPage.isItemPresent(baseName, ANNOTATION_INHERITED, ACTION_VIEW, ACTION_DELETE));
        assertTrue(xmlRpcHelper.configPathExists(childReqPath));
    }

    public void testDeleteSingleton() throws Exception
    {
        String projectPath = xmlRpcHelper.insertSimpleProject(random, false);

        getBrowser().loginAsAdmin();
        String path = getPath(projectPath, "scm");
        CompositePage subversionPage = getBrowser().openAndWaitFor(CompositePage.class, path);
        assertTrue(subversionPage.isActionPresent(ACTION_DELETE));
        subversionPage.clickAction(ACTION_DELETE);

        DeleteConfirmPage confirmPage = getBrowser().createPage(DeleteConfirmPage.class, path, false);
        confirmPage.waitFor();
        CompositePage projectPage = confirmPage.confirmDeleteSingleton();
        assertTrue(projectPage.isTreeLinkPresent("scm"));
    }

    private String insertBuildCompletedTrigger(String refereePath, String refererPath) throws Exception
    {
        Hashtable<String, Object> trigger = xmlRpcHelper.createEmptyConfig(BuildCompletedTriggerConfiguration.class);
        trigger.put("name", "test");
        trigger.put("project", refereePath);
        return xmlRpcHelper.insertConfig(getPath(refererPath, "triggers"), trigger);
    }

    private String insertLabel(String projectPath) throws Exception
    {
        Hashtable<String, Object> label = xmlRpcHelper.createEmptyConfig(LabelConfiguration.class);
        label.put("label", "test");
        return xmlRpcHelper.insertConfig(getPath(projectPath, "labels"), label);
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
            assertEquals(pathActionPairs[i * 2], getBrowser().getCellContents(page.getId(), i + 1, 0));
            assertEquals(pathActionPairs[i * 2 + 1], getBrowser().getCellContents(page.getId(), i + 1, 1));
        }

        String actionsCell = getBrowser().getCellContents(page.getId(), i + 1, 0);
        actionsCell = actionsCell.replaceAll(" +", " ");
        assertEquals((page.isHide() ? "hide" : "delete") + " cancel", actionsCell);
    }
}
