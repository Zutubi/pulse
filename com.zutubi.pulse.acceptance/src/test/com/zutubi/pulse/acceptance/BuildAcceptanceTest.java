package com.zutubi.pulse.acceptance;

import static com.zutubi.pulse.acceptance.Constants.Project.TYPE;
import static com.zutubi.pulse.acceptance.Constants.TEST_ANT_REPOSITORY;
import static com.zutubi.pulse.acceptance.Constants.VERSIONED_REPOSITORY;
import com.zutubi.pulse.acceptance.forms.admin.BuildStageForm;
import com.zutubi.pulse.acceptance.forms.admin.SpecifyBuildPropertiesForm;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.browse.*;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.model.TestResultSummary;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.agent.AgentManager;
import static com.zutubi.pulse.master.agent.AgentManager.MASTER_AGENT_NAME;
import static com.zutubi.pulse.master.model.ProjectManager.GLOBAL_PROJECT_NAME;
import com.zutubi.pulse.master.tove.config.ConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ResourceConfiguration;
import com.zutubi.pulse.master.tove.config.project.ResourceRequirementConfiguration;
import com.zutubi.pulse.master.tove.config.project.changeviewer.FisheyeConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.BuildCompletedTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.DirectoryArtifactConfiguration;
import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.TextUtils;
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
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * An acceptance test that adds a very simple project and runs a build as a
 * sanity test.
 */
public class BuildAcceptanceTest extends SeleniumTestBase
{
    private static final int BUILD_TIMEOUT = 90000;

    private static final String CHANGE_AUTHOR = "pulse";
    private static final String CHANGE_COMMENT = "Edit build file.";
    private static final String CHANGE_FILENAME = "build.xml";

    private static final String LOCATOR_ENV_ARTIFACT = "link=env.txt";
    private static final String LOCATOR_OUTPUT_ARTIFACT = "link=output.txt";

    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();

        Vector<String> agents = xmlRpcHelper.getConfigListing(ConfigurationRegistry.AGENTS_SCOPE);
        for (String agent : agents)
        {
            if (!agent.equals(AgentManager.GLOBAL_AGENT_NAME) && !agent.equals(MASTER_AGENT_NAME))
            {
                xmlRpcHelper.deleteConfig(PathUtils.getPath(ConfigurationRegistry.AGENTS_SCOPE, agent));
            }
        }
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testSimpleBuild() throws Exception
    {
        loginAsAdmin();
        addProject(random, true);

        triggerSuccessfulBuild(random, MASTER_AGENT_NAME);

        // Check some properties
        EnvironmentArtifactPage envPage = new EnvironmentArtifactPage(selenium, urls, random, 1, "default", "build");
        envPage.goTo();
        assertTrue(envPage.isPropertyPresentWithValue(BuildProperties.PROPERTY_LOCAL_BUILD, Boolean.toString(false)));
        assertTrue(envPage.isPropertyPresentWithValue(BuildProperties.PROPERTY_PERSONAL_BUILD, Boolean.toString(false)));
    }

    public void testChangesBetweenBuilds() throws Exception
    {
        // Run an initial build
        addProject(random, true);
        xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

        // Commit a change to the repository.  Note monitoring the SCM is
        // disabled for these projects, so no chance of a build being started
        // by this change.
        String revisionString = editAndCommitBuildFile();
        int buildNumber = xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

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

    public void testChangeViewerLinks() throws Exception
    {
        final String FISHEYE_BASE = "http://fisheye";
        final String FISHEYE_PROJECT = "project";

        String projectPath = addProject(random, true);
        Hashtable<String, Object> changeViewer = xmlRpcHelper.createDefaultConfig(FisheyeConfiguration.class);
        changeViewer.put("baseURL", FISHEYE_BASE);
        changeViewer.put("projectPath", FISHEYE_PROJECT);
        xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, "changeViewer"), changeViewer);

        xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);
        String revisionString = editAndCommitBuildFile();
        int buildNumber = xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

        String changelistLink = FISHEYE_BASE + "/changelog/" + FISHEYE_PROJECT + "/?cs=" + revisionString;

        loginAsAdmin();
        BuildChangesPage changesPage = new BuildChangesPage(selenium, urls, random, buildNumber);
        changesPage.goTo();
        SeleniumUtils.assertLinkToPresent(selenium, changelistLink);

        ViewChangelistPage changelistPage = new ViewChangelistPage(selenium, urls, random, buildNumber, changesPage.getChangeIds().get(0), revisionString);
        changelistPage.goTo();

        String prefixPart = FISHEYE_BASE + "/browse/";
        String filePart = FISHEYE_PROJECT + "/accept/trunk/triviant/" + CHANGE_FILENAME;
        SeleniumUtils.assertLinkToPresent(selenium, changelistLink);
        SeleniumUtils.assertLinkToPresent(selenium, prefixPart + filePart + "?r=" + revisionString);
        SeleniumUtils.assertLinkToPresent(selenium, prefixPart + "~raw,r=" + revisionString + "/" + filePart);
        SeleniumUtils.assertLinkToPresent(selenium, prefixPart + filePart + "?r1=" + new Revision(revisionString).calculatePreviousNumericalRevision() + "&r2=" + revisionString);
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
            updateClient.doCheckout(SVNURL.parseURIDecoded(Constants.TRIVIAL_ANT_REPOSITORY), wcDir, SVNRevision.UNDEFINED, SVNRevision.HEAD, SVNDepth.INFINITY, false);

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
        assertEquals(revisionString, fileChange.getRevision().getRevisionString());
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
        triggerSuccessfulBuild(random, MASTER_AGENT_NAME);

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

        triggerSuccessfulBuild(random, MASTER_AGENT_NAME);
        assertEnvironment(random, 1, "pname=pvalue", "PULSE_PNAME=pvalue", "PULSE_BUILD_NUMBER=1");
    }

    public void testImportedResources() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(MASTER_AGENT_NAME, resourceName);
        xmlRpcHelper.insertConfig(getPath(resourcePath, "properties"), xmlRpcHelper.createProperty("test-property", "test-value", false, false, false));

        String projectName = random + "-project";
        ensureProject(projectName);
        xmlRpcHelper.insertConfig(getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "PULSE_TEST-PROPERTY=test-value");
    }

    public void testProjectPropertyReferencesResourceProperty() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(MASTER_AGENT_NAME, resourceName);
        xmlRpcHelper.insertConfig(getPath(resourcePath, "properties"), xmlRpcHelper.createProperty("rp", "rv", false, false, false));

        String projectName = random + "-project";
        String projectPath = getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName);
        ensureProject(projectName);
        xmlRpcHelper.insertConfig(getPath(projectPath, "requirements"), createRequiredResource(resourceName, null));
        xmlRpcHelper.insertConfig(getPath(projectPath, "properties"), xmlRpcHelper.createProperty("pp", "ref ${rp}", true, true, false));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "pp=ref rv");
    }

    public void testResourcePropertyReferencesEarlierProperty() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(MASTER_AGENT_NAME, resourceName);
        String propertiesPath = getPath(resourcePath, "properties");
        xmlRpcHelper.insertConfig(propertiesPath, xmlRpcHelper.createProperty("referee", "ee", false, false, false));
        xmlRpcHelper.insertConfig(propertiesPath, xmlRpcHelper.createProperty("referer", "ref ${referee}", true, true, false));

        String projectName = random + "-project";
        ensureProject(projectName);
        xmlRpcHelper.insertConfig(getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, MASTER_AGENT_NAME);
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
        triggerSuccessfulBuild(projectName, MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "pp=ref " + MASTER_AGENT_NAME);
    }

    public void testResourcePropertyReferencesAgentName() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(MASTER_AGENT_NAME, resourceName);
        xmlRpcHelper.insertConfig(getPath(resourcePath, "properties"), xmlRpcHelper.createProperty("rp", "ref ${agent}", true, true, false));

        String projectName = random + "-project";
        ensureProject(projectName);
        xmlRpcHelper.insertConfig(getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "rp=ref " + MASTER_AGENT_NAME);
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
        triggerSuccessfulBuild(projectName, MASTER_AGENT_NAME);
        goToEnv(projectName, 1);
        assertTextPresent(suppressedName);
        assertTextNotPresent(suppressedValue);
    }

    public void testScmPropertiesAvailableInPulseFile() throws Exception
    {
        Hashtable<String, Object> type = xmlRpcHelper.createEmptyConfig("zutubi.customTypeConfig");
        type.put("pulseFileString", "<?xml version=\"1.0\"?>\n" +
                "<project default-recipe=\"default\"><recipe name=\"default\"><print name=\"mess\" message=\"${svn.url}\"/></recipe></project>");
        xmlRpcHelper.insertProject(random, GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.TRIVIAL_ANT_REPOSITORY), type);
        xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

        loginAsAdmin();
        goToArtifact(random, 1, "mess", LOCATOR_OUTPUT_ARTIFACT);
        assertTextPresent(Constants.TRIVIAL_ANT_REPOSITORY);
    }

    public void testBuildLogAvailable() throws Exception
    {
        addProject(random, true);

        loginAsAdmin();
        triggerSuccessfulBuild(random, MASTER_AGENT_NAME);

        BuildDetailedViewPage detailedViewPage = new BuildDetailedViewPage(selenium, urls, random, 1);
        detailedViewPage.goTo();

        String logLinkId = "log-" + random + "-1";

        SeleniumUtils.assertLinkPresent(selenium, logLinkId);

        selenium.click("id=" + logLinkId);
        selenium.waitForPageToLoad("10000");

        assertTextPresent("tail of build log");
    }

    public void testDownloadArtifactLink() throws Exception
    {
        // CIB-1724: download raw artifacts via 2.0 url scheme.
        addProject(random, true);
        int buildNumber = xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);
        Vector<Hashtable<String, Object>> artifacts = xmlRpcHelper.getArtifactsInBuild(random, buildNumber);
        Hashtable<String, Object> outputArtifact = CollectionUtils.find(artifacts, new Predicate<Hashtable<String, Object>>()
        {
            public boolean satisfied(Hashtable<String, Object> artifact)
            {
                return "command output".equals(artifact.get("name"));
            }
        });

        assertNotNull(outputArtifact);
        String permalink = (String) outputArtifact.get("permalink");
        // This is to check for the new 2.0-ised URL.
        assertTrue(permalink.contains("/downloads/"));

        loginAsAdmin();
        goTo(permalink + "/output.txt");
        selenium.waitForPageToLoad("30000");

        assertTrue(selenium.getBodyText().contains("BUILD SUCCESSFUL"));
    }

    public void testManualTriggerBuildWithPrompt() throws Exception
    {
        loginAsAdmin();
        ensureProject(random);

        // add the pname=pvalue property to the build.
        xmlRpcHelper.insertProjectProperty(random, "pname", "pvalue", false, true, false);

        // edit the build options, setting prompt to true.
        enableBuildPrompting(random);

        // trigger a build
        ProjectHomePage home = new ProjectHomePage(selenium, urls, random);
        home.goTo();
        home.triggerBuild();

        // we should be prompted for a revision and a pname value.
        SpecifyBuildPropertiesForm sbpf = new SpecifyBuildPropertiesForm(selenium);
        sbpf.waitFor();
        assertTrue(sbpf.isFormPresent());
        
        // leave the revision blank
        sbpf.triggerFormElements("");

        // next page is the project homepage.
        waitForBuildOnProjectHomePage(random, MASTER_AGENT_NAME);
    }

    /**
     * Check that the prompted property values that in a manual build are added to the build,
     * but do not change the project configuration.
     *  
     * @throws Exception on error.
     */
    public void testManualTriggerBuildWithPromptAllowsPropertyValueOverride() throws Exception
    {
        loginAsAdmin();
        ensureProject(random);

        // add the pname=pvalue property to the build.
        xmlRpcHelper.insertProjectProperty(random, "pname", "pvalue", false, true, false);

        // edit the build options, setting prompt to true.
        enableBuildPrompting(random);

        // trigger a build
        ProjectHomePage home = new ProjectHomePage(selenium, urls, random);
        home.goTo();
        home.triggerBuild();

        // we should be prompted for a revision and a pname value.
        SpecifyBuildPropertiesForm sbpf = new SpecifyBuildPropertiesForm(selenium, "pname");
        sbpf.waitFor();
        // leave the revision blank, update pname to qvalue.
        sbpf.triggerFormElements("", "qvalue");

        // next page is the project homepage.
        waitForBuildOnProjectHomePage(random, MASTER_AGENT_NAME);

        // verify that the correct property value was used in the build.
        assertEnvironment(random, 1, "pname=qvalue", "PULSE_PNAME=qvalue");

        // go back to the properties page and ensure that the value is pvalue.
        ListPage propertiesPage = new ListPage(selenium, urls, getPropertiesPath(random));
        propertiesPage.goTo();

        assertEquals("pname", propertiesPage.getCellContent(0, 0));
        assertEquals("pvalue", propertiesPage.getCellContent(0, 1));
    }

    public void testTriggerProperties() throws Exception
    {
        String manualProject = random + "-manual";
        String buildCompletedProject = random + "-completed";

        ensureProject(manualProject);
        ensureProject(buildCompletedProject);

        Hashtable<String, Object> buildCompletedTrigger = xmlRpcHelper.createEmptyConfig(BuildCompletedTriggerConfiguration.class);
        buildCompletedTrigger.put("name", "cascade");
        buildCompletedTrigger.put("project", PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, manualProject));

        Hashtable<String, Object> property = xmlRpcHelper.createProperty("tp", "tpv");
        Hashtable<String, Hashtable<String, Object>> properties = new Hashtable<String, Hashtable<String, Object>>();
        properties.put("trigger property", property);
        buildCompletedTrigger.put("properties", properties);

        xmlRpcHelper.insertConfig(PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, buildCompletedProject, "triggers"), buildCompletedTrigger);
        xmlRpcHelper.runBuild(manualProject, BUILD_TIMEOUT);
        xmlRpcHelper.waitForBuildToComplete(buildCompletedProject, 1, BUILD_TIMEOUT);

        loginAsAdmin();
        assertEnvironment(buildCompletedProject, 1, "PULSE_TP=tpv");
    }

    public void testVersionedBuildWithImports() throws Exception
    {
        loginAsAdmin();
        xmlRpcHelper.insertProject(random, GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(VERSIONED_REPOSITORY), xmlRpcHelper.createVersionedConfig("pulse/pulse.xml"));

        triggerSuccessfulBuild(random, MASTER_AGENT_NAME);
    }

    public void testTestResults() throws Exception
    {
        Hashtable<String, Object> antConfig = xmlRpcHelper.getAntConfig();
        antConfig.put(Constants.Project.AntType.TARGETS, "test");
        String projectPath = xmlRpcHelper.insertProject(random, GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(TEST_ANT_REPOSITORY), antConfig);

        Hashtable<String, Object> dirArtifactConfig = xmlRpcHelper.createDefaultConfig(DirectoryArtifactConfiguration.class);
        dirArtifactConfig.put("name", "xml reports");
        dirArtifactConfig.put("base", "build/reports/xml");
        dirArtifactConfig.put("postprocessors", new Vector<String>(Arrays.asList("junit")));
        xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, TYPE, "artifacts"), dirArtifactConfig);

        int buildId = xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

        loginAsAdmin();

        BuildTestsPage testsPage = new BuildTestsPage(selenium, urls, random, buildId);
        testsPage.goTo();

        TestResultSummary expectedSummary = new TestResultSummary(0, 1, 0, 2);

        assertTrue(testsPage.hasTests());
        assertEquals(expectedSummary, testsPage.getTestSummary());

        StageTestsPage stageTestsPage = testsPage.clickStageAndWait("default");
        assertEquals(expectedSummary, stageTestsPage.getTestSummary());

        TestSuitePage suitePage = stageTestsPage.clickSuiteAndWait("com.zutubi.testant.UnitTest");
        assertEquals(expectedSummary, suitePage.getTestSummary());
        
        suitePage.clickStageCrumb();
        stageTestsPage.waitFor();
        stageTestsPage.clickAllCrumb();
        testsPage.waitFor();
    }

    public void testIdLeader() throws Exception
    {
        String template = random + "-template";
        String leader = random + "-leader";
        String follower1 = random + "-follower1";
        String follower2 = random + "-follower2";

        String templatePath = xmlRpcHelper.insertSimpleProject(template, true);
        String leaderPath = xmlRpcHelper.insertSimpleProject(leader, template, false);
        xmlRpcHelper.insertSimpleProject(follower1, template, false);
        xmlRpcHelper.insertSimpleProject(follower2, template, false);

        // Build follower1 before setting leader, to test the id not being less
        // than an existing build.
        assertEquals(1, xmlRpcHelper.runBuild(follower1, BUILD_TIMEOUT));

        // Set the leader in the template.
        String optionsPath = PathUtils.getPath(templatePath, Constants.Project.OPTIONS);
        Hashtable<String, Object> templateOptions = xmlRpcHelper.getConfig(optionsPath);
        templateOptions.put(Constants.Project.Options.ID_LEADER, leaderPath);
        xmlRpcHelper.saveConfig(optionsPath, templateOptions, false);

        // Make sure projects are sharing sequence.
        assertEquals(2, xmlRpcHelper.runBuild(follower1, BUILD_TIMEOUT));
        assertEquals(3, xmlRpcHelper.runBuild(follower2, BUILD_TIMEOUT));
        assertEquals(4, xmlRpcHelper.runBuild(follower1, BUILD_TIMEOUT));

        // Clear the leader
        templateOptions.put(Constants.Project.Options.ID_LEADER, "");
        xmlRpcHelper.saveConfig(optionsPath, templateOptions, false);

        // Make sure follower2 is back on its own sequence.
        assertEquals(4, xmlRpcHelper.runBuild(follower2, BUILD_TIMEOUT));
    }

    private void enableBuildPrompting(String projectName) throws Exception
    {
        Hashtable<String, Object> config = xmlRpcHelper.getConfig(getOptionsPath(projectName));
        config.put("prompt", Boolean.TRUE);
        xmlRpcHelper.saveConfig(getOptionsPath(projectName), config, false);
    }

    private String getPropertiesPath(String projectName)
    {
        return PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "properties");
    }

    private String getOptionsPath(String projectName)
    {
        return PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "options");
    }

    private void assertEnvironment(String projectName, int buildId, String... envs)
    {
        goToEnv(projectName, buildId);
        for (String env : envs)
        {
            assertTextPresent(env);
        }
    }

    private void goToEnv(String projectName, int buildId)
    {
        goToArtifact(projectName, buildId, "build", LOCATOR_ENV_ARTIFACT);
    }

    private void goToArtifact(String projectName, int buildId, String command, String artifact)
    {
        BuildDetailedViewPage detailedViewPage = new BuildDetailedViewPage(selenium, urls, projectName, buildId);
        detailedViewPage.goTo();
        detailedViewPage.clickCommand("default", command);
        selenium.click(artifact);
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

        waitForBuildOnProjectHomePage(projectName, agent);
    }

    private void waitForBuildOnProjectHomePage(String projectName, String agent)
    {
        ProjectHomePage home = new ProjectHomePage(selenium, urls, projectName);
        home.waitFor();

        String statusId = IDs.buildStatusCell(projectName, 1);
        SeleniumUtils.refreshUntilElement(selenium, statusId, BUILD_TIMEOUT);
        SeleniumUtils.refreshUntilText(selenium, statusId, "success", BUILD_TIMEOUT);
        SeleniumUtils.assertText(selenium, IDs.stageAgentCell(projectName, 1, "default"), agent);
    }
}
