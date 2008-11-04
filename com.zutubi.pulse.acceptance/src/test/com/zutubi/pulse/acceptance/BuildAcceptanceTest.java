package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.BuildStageForm;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildChangesPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildDetailedViewPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.acceptance.pages.browse.ViewChangelistPage;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.tove.config.ConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ResourceConfiguration;
import com.zutubi.pulse.master.tove.config.project.ResourceRequirementConfiguration;
import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.TextUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * An acceptance test that adds a very simple project and runs a build as a
 * sanity test.
 */
@Test(dependsOnGroups = {"init.*"})
public class BuildAcceptanceTest extends SeleniumTestBase
{
    private static final String CHANGE_AUTHOR = "pulse";
    private static final String CHANGE_COMMENT = "Edit build file.";
    private static final String CHANGE_FILENAME = "build.xml";

    private static final String LOCATOR_ENV_ARTIFACT = "link=env.txt";

    @BeforeMethod
    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();

        Vector<String> agents = xmlRpcHelper.getConfigListing(ConfigurationRegistry.AGENTS_SCOPE);
        for(String agent: agents)
        {
            if(!agent.equals(AgentManager.GLOBAL_AGENT_NAME) && !agent.equals(AgentManager.MASTER_AGENT_NAME))
            {
                xmlRpcHelper.deleteConfig(PathUtils.getPath(ConfigurationRegistry.AGENTS_SCOPE, agent));
            }
        }
    }

    @AfterMethod
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testSimpleBuild() throws Exception
    {
        loginAsAdmin();
        addProject(random, true);

        triggerSuccessfulBuild(random, AgentManager.MASTER_AGENT_NAME);
    }

    public void testChangesBetweenBuilds() throws Exception
    {
        // Run an initial build
        addProject(random, true);
        xmlRpcHelper.runBuild(random, 30000);

        // Commit a change to the repository.  Note monitoring the SCM is
        // disabled for these projects, so no chance of a build being started
        // by this change.
        String revisionString = editAndCommitBuildFile();
        int buildNumber = xmlRpcHelper.runBuild(random, 30000);

        // Check the changes tab.
        loginAsAdmin();
        BuildChangesPage changesPage = new BuildChangesPage(selenium, urls, random, buildNumber);
        changesPage.goTo();
        assertEquals(BuildChangesPage.formatChangesHeader(buildNumber), changesPage.getChangesHeader());

        List<Changelist> changelists = changesPage.getChangelists();
        assertEquals(1, changelists.size());
        assertBuildFileChangelist(changelists.get(0), revisionString);
        
        // Check the changelist view too.
        List<Long> changeIds = changesPage.getChangeIds();
        assertEquals(1, changeIds.size());
        ViewChangelistPage changelistPage = new ViewChangelistPage(selenium, urls, random, buildNumber, changeIds.get(0), revisionString);
        changelistPage.goTo();
        assertBuildFileChangelist(changelistPage.getChangelist(), revisionString);
    }

    private String editAndCommitBuildFile() throws IOException, SVNException
    {
        SVNRepositoryFactoryImpl.setup();
        File wcDir = FileSystemUtils.createTempDir(getName(), ".tmp");
        try
        {
            DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
            BasicAuthenticationManager authenticationManager = new BasicAuthenticationManager(CHANGE_AUTHOR, CHANGE_AUTHOR);
            SVNUpdateClient updateClient = new SVNUpdateClient(authenticationManager, options);
            updateClient.doCheckout(SVNURL.parseURIDecoded(Constants.TRIVIAL_PROJECT_REPOSITORY), wcDir, SVNRevision.UNDEFINED, SVNRevision.HEAD, SVNDepth.INFINITY, false);

            File buildFile = new File(wcDir, CHANGE_FILENAME);
            assertTrue(buildFile.exists());
            FileSystemUtils.createFile(buildFile, "<?xml version=\"1.0\"?>\n" +
                    "<project default=\"default\">\n" +
                    "    <target name=\"default\">\n" +
                    "        <echo message=\"" + random + "\"/>\n" +
                    "    </target>\n" +
                    "</project>");

            SVNClientManager clientManager = SVNClientManager.newInstance(options, authenticationManager);
            SVNCommitInfo commitInfo = clientManager.getCommitClient().doCommit(new File[]{buildFile}, true, CHANGE_COMMENT, null, null, false, false, SVNDepth.EMPTY);
            return Long.toString(commitInfo.getNewRevision());
        }
        finally
        {
            FileSystemUtils.rmdir(wcDir);
        }
    }

    private void assertBuildFileChangelist(Changelist changelist, String revisionString)
    {
        assertEquals(revisionString, changelist.getRevision().getRevisionString());
        assertEquals(CHANGE_AUTHOR, changelist.getAuthor());
        assertEquals(CHANGE_COMMENT, changelist.getComment());

        List<FileChange> fileChanges = changelist.getChanges();
        assertEquals(1, fileChanges.size());
        FileChange fileChange = fileChanges.get(0);
        assertTrue(fileChange.getPath().endsWith(CHANGE_FILENAME));
        assertEquals(revisionString, fileChange.getRevisionString());
        assertEquals(FileChange.Action.EDIT, fileChange.getAction());
    }

    public void testAgentBuild() throws Exception
    {
        addProject(random, true);
        loginAsAdmin();

        String agentHandle;
        ensureAgent(AGENT_NAME);
        agentHandle = xmlRpcHelper.getConfigHandle("agents/" + AGENT_NAME);

        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, random, false);
        hierarchyPage.goTo();
        ProjectConfigPage configPage = hierarchyPage.clickConfigure();
        configPage.waitFor();
        ListPage stagesPage = configPage.clickCollection(ProjectConfigPage.BUILD_STAGES_BASE, ProjectConfigPage.BUILD_STAGES_DISPLAY);
        stagesPage.waitFor();
        stagesPage.clickView("default");

        BuildStageForm stageForm = new BuildStageForm(selenium, true);
        stageForm.waitFor();

        stageForm.applyFormElements("", agentHandle);

        triggerSuccessfulBuild(random, AGENT_NAME);
    }

    public void testDetailedView() throws Exception
    {
        addProject(random, true);

        loginAsAdmin();
        triggerSuccessfulBuild(random, AgentManager.MASTER_AGENT_NAME);

        BuildDetailedViewPage detailedViewPage = new BuildDetailedViewPage(selenium, urls, random, 1);
        detailedViewPage.goTo();
        SeleniumUtils.waitForLocator(selenium, LOCATOR_ENV_ARTIFACT);
        SeleniumUtils.assertNotVisible(selenium, LOCATOR_ENV_ARTIFACT);
        detailedViewPage.clickCommand("default", "build");
        SeleniumUtils.assertVisible(selenium, LOCATOR_ENV_ARTIFACT);
        selenium.click(LOCATOR_ENV_ARTIFACT);
        selenium.waitForPageToLoad("10000");
        assertTextPresent("Process Environment");

        detailedViewPage.goTo();
        detailedViewPage.clickCommand("default", "build");
        selenium.click("link=decorated");
        selenium.waitForPageToLoad("10000");
        assertElementPresent("decorated");
    }

    public void testPulseEnvironmentVariables() throws Exception
    {
        loginAsAdmin();
        ensureProject(random);

        xmlRpcHelper.insertProjectProperty(random, "pname", "pvalue", false, true, false);

        triggerSuccessfulBuild(random, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(random, 1, "pname=pvalue", "PULSE_PNAME=pvalue", "PULSE_BUILD_NUMBER=1");
    }

    public void testImportedResources() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(AgentManager.MASTER_AGENT_NAME, resourceName);
        xmlRpcHelper.insertConfig(getPath(resourcePath, "properties"), xmlRpcHelper.createProperty("test-property", "test-value", false, false, false));

        String projectName = random + "-project";
        ensureProject(projectName);
        xmlRpcHelper.insertConfig(getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "PULSE_TEST-PROPERTY=test-value");
    }

    public void testProjectPropertyReferencesResourceProperty() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(AgentManager.MASTER_AGENT_NAME, resourceName);
        xmlRpcHelper.insertConfig(getPath(resourcePath, "properties"), xmlRpcHelper.createProperty("rp", "rv", false, false, false));

        String projectName = random + "-project";
        String projectPath = getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName);
        ensureProject(projectName);
        xmlRpcHelper.insertConfig(getPath(projectPath, "requirements"), createRequiredResource(resourceName, null));
        xmlRpcHelper.insertConfig(getPath(projectPath, "properties"), xmlRpcHelper.createProperty("pp", "ref ${rp}", true, true, false));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "pp=ref rv");
    }

    public void testResourcePropertyReferencesEarlierProperty() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(AgentManager.MASTER_AGENT_NAME, resourceName);
        String propertiesPath = getPath(resourcePath, "properties");
        xmlRpcHelper.insertConfig(propertiesPath, xmlRpcHelper.createProperty("referee", "ee", false, false, false));
        xmlRpcHelper.insertConfig(propertiesPath, xmlRpcHelper.createProperty("referer", "ref ${referee}", true, true, false));

        String projectName = random + "-project";
        ensureProject(projectName);
        xmlRpcHelper.insertConfig(getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "referer=ref ee");
    }

    public void testProjectPropertyReferencesAgentName() throws Exception
    {
        String projectName = random + "-project";
        ensureProject(projectName);
        String stagePath = getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "stages", "default");
        Hashtable<String, Object> defaultStage = xmlRpcHelper.getConfig(stagePath);
        defaultStage.put("agent", getPath(ConfigurationRegistry.AGENTS_SCOPE, AgentManager.MASTER_AGENT_NAME));
        xmlRpcHelper.saveConfig(stagePath, defaultStage, false);
        xmlRpcHelper.insertProjectProperty(projectName, "pp", "ref ${agent}", true, true, false);

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "pp=ref " + AgentManager.MASTER_AGENT_NAME);
    }

    public void testResourcePropertyReferencesAgentName() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(AgentManager.MASTER_AGENT_NAME, resourceName);
        xmlRpcHelper.insertConfig(getPath(resourcePath, "properties"), xmlRpcHelper.createProperty("rp", "ref ${agent}", true, true, false));

        String projectName = random + "-project";
        ensureProject(projectName);
        xmlRpcHelper.insertConfig(getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "rp=ref " + AgentManager.MASTER_AGENT_NAME);
    }

    public void testSuppressedProperty() throws Exception
    {
        String projectName = random + "-project";
        ensureProject(projectName);
        String stagePath = getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "stages", "default");
        Hashtable<String, Object> defaultStage = xmlRpcHelper.getConfig(stagePath);
        defaultStage.put("agent", getPath(ConfigurationRegistry.AGENTS_SCOPE, AgentManager.MASTER_AGENT_NAME));
        xmlRpcHelper.saveConfig(stagePath, defaultStage, false);
        String suppressedName = "PULSE_TEST_SUPPRESSED";
        String suppressedValue = random + "-suppress";
        xmlRpcHelper.insertProjectProperty(projectName, suppressedName, suppressedValue, false, true, false);

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, AgentManager.MASTER_AGENT_NAME);
        goToEnv(projectName, 1);
        assertTextPresent(suppressedName);
        assertTextNotPresent(suppressedValue);
    }

    public void testBuildLogAvailable() throws Exception
    {
        addProject(random, true);

        loginAsAdmin();
        triggerSuccessfulBuild(random, AgentManager.MASTER_AGENT_NAME);

        BuildDetailedViewPage detailedViewPage = new BuildDetailedViewPage(selenium, urls, random, 1);
        detailedViewPage.goTo();

        String logLinkId = "log-" + random + "-1";

        SeleniumUtils.assertLinkPresent(selenium, logLinkId);

        selenium.getAllLinks();

        selenium.click("id=" + logLinkId);
        selenium.waitForPageToLoad("10000");

        assertTextPresent("tail of build log");        
    }

    private void assertEnvironment(String projectName, int buildId, String... envs)
    {
        goToEnv(projectName, buildId);
        for(String env: envs)
        {
            assertTextPresent(env);
        }
    }

    private void goToEnv(String projectName, int buildId)
    {
        BuildDetailedViewPage detailedViewPage = new BuildDetailedViewPage(selenium, urls, projectName, buildId);
        detailedViewPage.goTo();
        detailedViewPage.clickCommand("default", "build");
        selenium.click(LOCATOR_ENV_ARTIFACT);
        selenium.waitForPageToLoad("10000");
    }

    private String addResource(String agent, String name) throws Exception
    {
        Hashtable<String, Object> resource = xmlRpcHelper.createDefaultConfig(ResourceConfiguration.class);
        resource.put("name", name);
        return xmlRpcHelper.insertConfig(getPath(ConfigurationRegistry.AGENTS_SCOPE, agent, "resources"), resource);
    }

    private Hashtable<String, Object> createRequiredResource(String resource, String version) throws Exception
    {
        Hashtable<String, Object> requirement = xmlRpcHelper.createDefaultConfig(ResourceRequirementConfiguration.class);
        requirement.put("resource", resource);
        if (TextUtils.stringSet(version))
        {
            requirement.put("version", version);
            requirement.put("defaultVersion", false);
        }

        return requirement;
    }

    private void triggerSuccessfulBuild(String projectName, String agent)
    {
        ProjectHomePage home = new ProjectHomePage(selenium, urls, projectName);
        home.goTo();
        home.triggerBuild();
        home.waitFor();
        String statusId = IDs.buildStatusCell(projectName, 1);
        SeleniumUtils.refreshUntilElement(selenium, statusId, 30000);
        SeleniumUtils.refreshUntilText(selenium, statusId, "success", 30000);
        SeleniumUtils.assertText(selenium, IDs.stageAgentCell(projectName, 1, "default"), agent);
    }
}
