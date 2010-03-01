package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.BuildStageForm;
import com.zutubi.pulse.acceptance.forms.admin.TriggerBuildForm;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.browse.*;
import com.zutubi.pulse.acceptance.pages.dashboard.DashboardPage;
import com.zutubi.pulse.acceptance.utils.Repository;
import com.zutubi.pulse.acceptance.utils.SubversionWorkspace;
import com.zutubi.pulse.core.commands.api.DirectoryArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.FileArtifactConfiguration;
import com.zutubi.pulse.core.commands.core.JUnitReportPostProcessorConfiguration;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.TestResultSummary;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.config.api.CheckoutScheme;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;
import com.zutubi.pulse.master.tove.config.project.ResourceRequirementConfiguration;
import com.zutubi.pulse.master.tove.config.project.changeviewer.FisheyeConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.BuildCompletedTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.CustomTypeConfiguration;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.*;
import com.zutubi.util.io.IOUtils;
import org.apache.commons.httpclient.Header;
import org.tmatesoft.svn.core.SVNException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import static com.zutubi.pulse.acceptance.Constants.*;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.ARTIFACTS;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.Artifact.POSTPROCESSORS;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.DirectoryArtifact.BASE;
import static com.zutubi.pulse.acceptance.Constants.Project.FileArtifact.FILE;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.DEFAULT_RECIPE_NAME;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.RECIPES;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.COMMANDS;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.DEFAULT_COMMAND;
import static com.zutubi.pulse.acceptance.Constants.Project.NAME;
import static com.zutubi.pulse.acceptance.Constants.Project.TYPE;
import static com.zutubi.pulse.core.dependency.ivy.IvyStatus.STATUS_INTEGRATION;
import static com.zutubi.pulse.core.dependency.ivy.IvyStatus.STATUS_RELEASE;
import static com.zutubi.pulse.master.agent.AgentManager.GLOBAL_AGENT_NAME;
import static com.zutubi.pulse.master.agent.AgentManager.MASTER_AGENT_NAME;
import static com.zutubi.pulse.master.model.ProjectManager.GLOBAL_PROJECT_NAME;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.AGENTS_SCOPE;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.PROJECTS_SCOPE;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import static com.zutubi.util.CollectionUtils.asPair;
import static com.zutubi.util.Constants.SECOND;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

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

    private static final String ANT_PROCESSOR = "ant output processor";
    private static final String JUNIT_PROCESSOR = "junit xml report processor";

    private Repository repository;

    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();

        Vector<String> agents = xmlRpcHelper.getConfigListing(AGENTS_SCOPE);
        for (String agent : agents)
        {
            if (!agent.equals(GLOBAL_AGENT_NAME) && !agent.equals(MASTER_AGENT_NAME))
            {
                xmlRpcHelper.deleteConfig(PathUtils.getPath(AGENTS_SCOPE, agent));
            }
        }

        repository = new Repository();
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
        EnvironmentArtifactPage envPage = browser.openAndWaitFor(EnvironmentArtifactPage.class, random, 1L, "default", "build");
        assertTrue(envPage.isPropertyPresentWithValue(BuildProperties.PROPERTY_LOCAL_BUILD, Boolean.toString(false)));
        assertTrue(envPage.isPropertyPresentWithValue(BuildProperties.PROPERTY_PERSONAL_BUILD, Boolean.toString(false)));
        assertTrue(envPage.isPropertyPresentWithValue(BuildProperties.PROPERTY_OWNER, random));
    }

    public void testChangesBetweenBuilds() throws Exception
    {
        // Run an initial build
        addProject(random, true);
        xmlRpcHelper.runBuild(random);

        // Commit a change to the repository.  Note monitoring the SCM is
        // disabled for these projects, so no chance of a build being started
        // by this change.
        String revisionString = editAndCommitBuildFile();
        long buildNumber = xmlRpcHelper.runBuild(random);

        // Check the changes tab.
        loginAsAdmin();
        BuildChangesPage changesPage = browser.openAndWaitFor(BuildChangesPage.class, random, buildNumber);
        assertEquals(BuildChangesPage.formatChangesHeader(buildNumber), changesPage.getChangesHeader());

        List<Changelist> changelists = changesPage.getChangelists();
        assertEquals(1, changelists.size());
        assertBuildFileChangelist(changelists.get(0), revisionString);

        // Check the changelist view too.
        List<Long> changeIds = changesPage.getChangeIds();
        assertEquals(1, changeIds.size());
        ViewChangelistPage changelistPage = browser.openAndWaitFor(ViewChangelistPage.class, random, buildNumber, changeIds.get(0), revisionString);
        assertBuildFileChangelist(changelistPage.getChangelist(), revisionString);
    }

    public void testChangeAffectsMultipleProjects() throws Exception
    {
        String visibleProject = random + "-visible";
        String invisibleProject = random + "-invisible";
        String regularUser = random + "-user";
        
        addProject(visibleProject, true);
        String invisibleProjectPath = addProject(invisibleProject, true);
        xmlRpcHelper.insertTrivialUser(regularUser);
        
        // Remove permissions that allow normal users to view the invisible
        // project (so only admin can see it).
        xmlRpcHelper.deleteAllConfigs(PathUtils.getPath(invisibleProjectPath, Constants.Project.PERMISSIONS, PathUtils.WILDCARD_ANY_ELEMENT));
        
        xmlRpcHelper.runBuild(visibleProject);
        xmlRpcHelper.runBuild(invisibleProject);

        // Commit a change to the repository.  Note monitoring the SCM is
        // disabled for these projects, so no chance of a build being started
        // by this change.
        String revisionString = editAndCommitBuildFile();
        
        long visibleBuildNumber = xmlRpcHelper.runBuild(visibleProject);
        long invisibleBuildNumber = xmlRpcHelper.runBuild(invisibleProject);

        // Check the changes tab.
        loginAsAdmin();
        BuildChangesPage changesPage = browser.openAndWaitFor(BuildChangesPage.class, visibleProject, visibleBuildNumber);
        List<Long> changeIds = changesPage.getChangeIds();
        assertEquals(1, changeIds.size());
        ViewChangelistPage changelistPage = browser.openAndWaitFor(ViewChangelistPage.class, visibleProject, visibleBuildNumber, changeIds.get(0), revisionString);
        List<Pair<String, Long>> builds = changelistPage.getBuilds();
        assertEquals(2, builds.size());
        assertThat(builds, hasItem(new Pair<String, Long>(visibleProject, visibleBuildNumber)));
        assertThat(builds, hasItem(new Pair<String, Long>(invisibleProject, invisibleBuildNumber)));
        
        logout();
        login(regularUser, "");
        
        // Regular user should only see the visible project.
        changelistPage.openAndWaitFor();
        builds = changelistPage.getBuilds();
        assertEquals(1, builds.size());
        assertThat(builds, hasItem(new Pair<String, Long>(visibleProject, visibleBuildNumber)));
        
        // Check other pages that we can view where the changelist appears.
        browser.openAndWaitFor(ProjectHomePage.class, visibleProject);
        browser.openAndWaitFor(DashboardPage.class);
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

        xmlRpcHelper.runBuild(random);
        String revisionString = editAndCommitBuildFile();
        long buildNumber = xmlRpcHelper.runBuild(random);

        String changelistLink = FISHEYE_BASE + "/changelog/" + FISHEYE_PROJECT + "/?cs=" + revisionString;

        loginAsAdmin();
        BuildChangesPage changesPage = browser.openAndWaitFor(BuildChangesPage.class, random, buildNumber);
        assertTrue(browser.isLinkToPresent(changelistLink));

        browser.openAndWaitFor(ViewChangelistPage.class, random, buildNumber, changesPage.getChangeIds().get(0), revisionString);

        String prefixPart = FISHEYE_BASE + "/browse/";
        String filePart = FISHEYE_PROJECT + "/accept/trunk/triviant/" + CHANGE_FILENAME;
        assertTrue(browser.isLinkToPresent(changelistLink));
        assertTrue(browser.isLinkToPresent(prefixPart + filePart + "?r=" + revisionString));
        assertTrue(browser.isLinkToPresent(prefixPart + "~raw,r=" + revisionString + "/" + filePart));
        assertTrue(browser.isLinkToPresent(prefixPart + filePart + "?r1=" + new Revision(revisionString).calculatePreviousNumericalRevision() + "&r2=" + revisionString));
    }

    private String editAndCommitBuildFile() throws IOException, SVNException
    {
        File wcDir = createTempDirectory();
        SubversionWorkspace workspace = new SubversionWorkspace(wcDir, CHANGE_AUTHOR, CHANGE_AUTHOR);
        
        try
        {
            workspace.doCheckout(TRIVIAL_ANT_REPOSITORY);

            File buildFile = new File(wcDir, CHANGE_FILENAME);
            assertTrue(buildFile.exists());
            FileSystemUtils.createFile(buildFile, "<?xml version=\"1.0\"?>\n" +
                    "<project default=\"default\">\n" +
                    "    <target name=\"default\">\n" +
                    "        <echo message=\"" + random + "\"/>\n" +
                    "    </target>\n" +
                    "</project>");

            return workspace.doCommit(CHANGE_COMMENT, buildFile);
        }
        finally
        {
            IOUtils.close(workspace);
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

        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, random, false);
        ProjectConfigPage configPage = hierarchyPage.clickConfigure();
        configPage.waitFor();
        ListPage stagesPage = configPage.clickCollection(ProjectConfigPage.BUILD_STAGES_BASE, ProjectConfigPage.BUILD_STAGES_DISPLAY);
        stagesPage.waitFor();
        stagesPage.clickView("default");

        BuildStageForm stageForm = browser.createForm(BuildStageForm.class, true);
        stageForm.waitFor();

        stageForm.applyFormElements("", agentHandle);

        triggerSuccessfulBuild(random, AGENT_NAME);
    }

    public void testDetailedView() throws Exception
    {
        addProject(random, true);

        loginAsAdmin();
        triggerSuccessfulBuild(random, MASTER_AGENT_NAME);

        BuildDetailedViewPage detailedViewPage = browser.openAndWaitFor(BuildDetailedViewPage.class, random, 1L);
        browser.waitForLocator(LOCATOR_ENV_ARTIFACT);
        assertFalse(browser.isVisible(LOCATOR_ENV_ARTIFACT));
        detailedViewPage.clickCommand("default", "build");
        assertTrue(browser.isVisible(LOCATOR_ENV_ARTIFACT));
        browser.click(LOCATOR_ENV_ARTIFACT);
        browser.waitForPageToLoad(10 * SECOND);
        assertTextPresent("Process Environment");

        detailedViewPage.openAndWaitFor();
        detailedViewPage.clickCommand("default", "build");
        browser.click("link=decorated");
        browser.waitForPageToLoad(10 * SECOND);
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
        xmlRpcHelper.insertConfig(getPath(PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

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
        String projectPath = getPath(PROJECTS_SCOPE, projectName);
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
        xmlRpcHelper.insertConfig(getPath(PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "referer=ref ee");
    }

    public void testProjectPropertyReferencesAgentName() throws Exception
    {
        String projectName = random + "-project";
        ensureProject(projectName);
        String stagePath = getPath(PROJECTS_SCOPE, projectName, "stages", "default");
        Hashtable<String, Object> defaultStage = xmlRpcHelper.getConfig(stagePath);
        defaultStage.put("agent", getPath(AGENTS_SCOPE, MASTER_AGENT_NAME));
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
        xmlRpcHelper.insertConfig(getPath(PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "rp=ref " + MASTER_AGENT_NAME);
    }

    public void testSuppressedProperty() throws Exception
    {
        String projectName = random + "-project";
        ensureProject(projectName);
        String stagePath = getPath(PROJECTS_SCOPE, projectName, "stages", "default");
        Hashtable<String, Object> defaultStage = xmlRpcHelper.getConfig(stagePath);
        defaultStage.put("agent", getPath(AGENTS_SCOPE, MASTER_AGENT_NAME));
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
        Hashtable<String, Object> type = xmlRpcHelper.createEmptyConfig(CustomTypeConfiguration.class);
        type.put("pulseFileString", "<?xml version=\"1.0\"?>\n" +
                "<project default-recipe=\"default\"><recipe name=\"default\"><print name=\"mess\" message=\"${svn.url}\"/></recipe></project>");
        xmlRpcHelper.insertProject(random, GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(TRIVIAL_ANT_REPOSITORY), type);
        xmlRpcHelper.runBuild(random);

        loginAsAdmin();
        goToArtifact(random, 1, "mess", LOCATOR_OUTPUT_ARTIFACT);
        assertTextPresent(TRIVIAL_ANT_REPOSITORY);
    }

    public void testBuildLogAvailable() throws Exception
    {
        addProject(random, true);

        loginAsAdmin();
        triggerSuccessfulBuild(random, MASTER_AGENT_NAME);

        BuildDetailedViewPage page = browser.openAndWaitFor(BuildDetailedViewPage.class, random, 1L);

        assertTrue(page.isBuildLogLinkPresent());
        BuildLogPage log = page.clickBuildLogLink();
        assertTrue(log.isLogAvailable());
    }

    public void testDownloadArtifactLink() throws Exception
    {
        // CIB-1724: download raw artifacts via 2.0 url scheme.
        addProject(random, true);
        long buildNumber = xmlRpcHelper.runBuild(random);
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
        browser.open(permalink + "/output.txt");
        browser.waitForPageToLoad(30 * SECOND);

        assertTrue(browser.getBodyText().contains("BUILD SUCCESSFUL"));
    }

    public void testSetArtifactContentType() throws Exception
    {
        final String ARTIFACT_NAME = "ant build file";
        final String ARTIFACT_FILENAME = "build.xml";
        final String CONTENT_TYPE = "application/test";

        String projectPath = addProject(random, true);

        Hashtable<String, Object> artifactConfig = xmlRpcHelper.createDefaultConfig(FileArtifactConfiguration.class);
        artifactConfig.put(NAME, ARTIFACT_NAME);
        artifactConfig.put(FILE, ARTIFACT_FILENAME);
        artifactConfig.put(TYPE, CONTENT_TYPE);
        xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE_NAME, COMMANDS, DEFAULT_COMMAND, ARTIFACTS), artifactConfig);

        int buildNumber = xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

        Vector<Hashtable<String, Object>> artifacts = xmlRpcHelper.getArtifactsInBuild(random, buildNumber);
        Hashtable<String, Object> artifact = CollectionUtils.find(artifacts, new Predicate<Hashtable<String, Object>>()
        {
            public boolean satisfied(Hashtable<String, Object> artifact)
            {
                return ARTIFACT_NAME.equals(artifact.get("name"));
            }
        });

        assertNotNull(artifact);

        String base = browser.getBaseUrl();
        if (base.endsWith("/"))
        {
            base = base.substring(0, base.length() - 1);
        }
        
        String url = base + artifact.get("permalink") + ARTIFACT_FILENAME;
        Header contentTypeHeader = AcceptanceTestUtils.readHttpHeader(url, "Content-Type");
        assertNotNull(contentTypeHeader);
        assertEquals(CONTENT_TYPE, contentTypeHeader.getValue().trim());
    }

    public void testManualTriggerBuildWithPrompt() throws Exception
    {
        loginAsAdmin();
        ensureProject(random);

        // add the pname=pvalue property to the build.
        xmlRpcHelper.insertProjectProperty(random, "pname", "pvalue", false, true, false);

        // edit the build options, setting prompt to true.
        xmlRpcHelper.enableBuildPrompting(random);

        // trigger a build
        ProjectHomePage home = browser.openAndWaitFor(ProjectHomePage.class, random);
        home.triggerBuild();

        // we should be prompted for a revision and a pname value.
        TriggerBuildForm form = browser.createForm(TriggerBuildForm.class);
        form.waitFor();
        assertTrue(form.isFormPresent());

        // leave the revision blank
        form.triggerFormElements(asPair("status", STATUS_INTEGRATION));

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
        xmlRpcHelper.enableBuildPrompting(random);

        // trigger a build
        ProjectHomePage home = browser.openAndWaitFor(ProjectHomePage.class, random);
        home.triggerBuild();

        // we should be prompted for a revision and a pname value.
        TriggerBuildForm form = browser.createForm(TriggerBuildForm.class);
        form.addProperty("pname");
        form.waitFor();
        // leave the revision blank, update pname to qvalue.
        form.triggerFormElements(asPair("status",STATUS_INTEGRATION), asPair("property.pname", "qvalue"));

        // next page is the project homepage.
        waitForBuildOnProjectHomePage(random, MASTER_AGENT_NAME);

        // verify that the correct property value was used in the build.
        assertEnvironment(random, 1, "pname=qvalue", "PULSE_PNAME=qvalue");

        // go back to the properties page and ensure that the value is pvalue.
        ListPage propertiesPage = browser.openAndWaitFor(ListPage.class, getPropertiesPath(random));

        assertEquals("pname", propertiesPage.getCellContent(0, 0));
        assertEquals("pvalue", propertiesPage.getCellContent(0, 1));
    }

    public void testManualTriggerBuildWithPromptAllowsStatusSelection() throws Exception
    {
        loginAsAdmin();
        ensureProject(random);
        xmlRpcHelper.enableBuildPrompting(random);

        // trigger a build
        ProjectHomePage home = browser.openAndWaitFor(ProjectHomePage.class, random);
        home.triggerBuild();

        TriggerBuildForm triggerBuildForm = browser.createForm(TriggerBuildForm.class);
        triggerBuildForm.waitFor();
        triggerBuildForm.triggerFormElements(asPair("status", STATUS_RELEASE));

        // next page is the project homepage.
        waitForBuildOnProjectHomePage(random, MASTER_AGENT_NAME);

        assertEquals(STATUS_RELEASE, repository.getIvyModuleDescriptor(random, 1).getStatus());
    }

    public void testTriggerProperties() throws Exception
    {
        String manualProject = random + "-manual";
        String buildCompletedProject = random + "-completed";

        ensureProject(manualProject);
        ensureProject(buildCompletedProject);

        Hashtable<String, Object> buildCompletedTrigger = xmlRpcHelper.createEmptyConfig(BuildCompletedTriggerConfiguration.class);
        buildCompletedTrigger.put("name", "cascade");
        buildCompletedTrigger.put("project", PathUtils.getPath(PROJECTS_SCOPE, manualProject));

        Hashtable<String, Object> property = xmlRpcHelper.createProperty("tp", "tpv");
        Hashtable<String, Hashtable<String, Object>> properties = new Hashtable<String, Hashtable<String, Object>>();
        properties.put("trigger property", property);
        buildCompletedTrigger.put("properties", properties);

        xmlRpcHelper.insertConfig(PathUtils.getPath(PROJECTS_SCOPE, buildCompletedProject, "triggers"), buildCompletedTrigger);
        xmlRpcHelper.runBuild(manualProject);
        xmlRpcHelper.waitForBuildToComplete(buildCompletedProject, 1);

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
        antConfig.put(Constants.Project.AntCommand.TARGETS, "test");
        String projectPath = xmlRpcHelper.insertSingleCommandProject(random, GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(TEST_ANT_REPOSITORY), antConfig);
        insertTestCapture(projectPath, JUNIT_PROCESSOR);

        buildAndCheckTestSummary(false, new TestResultSummary(0, 0, 1, 0, 2));
    }

    public void testTestResultsExpectedFailure() throws Exception
    {
        Hashtable<String, Object> config = xmlRpcHelper.getAntConfig();
        config.put(Constants.Project.AntCommand.TARGETS, "test");
        config.put(Constants.Project.AntCommand.ARGUMENTS, "-Dignore.test.result=true");
        String projectPath = xmlRpcHelper.insertSingleCommandProject(random, GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(TEST_ANT_REPOSITORY), config);

        // Capture the results, change our processor to load the expected
        // failures.
        insertTestCapture(projectPath, JUNIT_PROCESSOR);
        String ppPath = PathUtils.getPath(projectPath, Project.POST_PROCESSORS, JUNIT_PROCESSOR);
        config = xmlRpcHelper.getConfig(ppPath);
        config.put("expectedFailureFile", "expected-failures.txt");
        xmlRpcHelper.saveConfig(ppPath, config, false);

        // Make sure the ant processor doesn't fail the build.
        ppPath = PathUtils.getPath(projectPath, Project.POST_PROCESSORS, ANT_PROCESSOR);
        config = xmlRpcHelper.getConfig(ppPath);
        config.put("failOnError", false);
        xmlRpcHelper.saveConfig(ppPath, config, false);

        buildAndCheckTestSummary(true, new TestResultSummary(1, 0, 0, 0, 2));
    }

    private void buildAndCheckTestSummary(boolean expectedSuccess, TestResultSummary expectedSummary) throws Exception
    {
        long buildId = xmlRpcHelper.runBuild(random);
        Hashtable<String, Object> build = xmlRpcHelper.getBuild(random, (int) buildId);
        boolean success = (Boolean) build.get("succeeded");
        assertEquals(expectedSuccess, success);

        loginAsAdmin();
        BuildTestsPage testsPage = browser.openAndWaitFor(BuildTestsPage.class, random, buildId);

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
        
        checkApiTestSummary(expectedSummary, build);
        @SuppressWarnings({"unchecked"})
        Vector<Hashtable<String, Object>> stages = (Vector<Hashtable<String, Object>>) build.get("stages");
        checkApiTestSummary(expectedSummary, stages.get(0));
    }

    private void checkApiTestSummary(TestResultSummary expectedSummary, Hashtable<String, Object> result)
    {
        @SuppressWarnings({"unchecked"})
        Hashtable<String, Object> tests = (Hashtable<String, Object>) result.get("tests");
        assertNotNull(tests);
        assertEquals(expectedSummary.getTotal(), tests.get("total"));
        assertEquals(expectedSummary.getPassed(), tests.get("passed"));
        assertEquals(expectedSummary.getSkipped(), tests.get("skipped"));
        assertEquals(expectedSummary.getExpectedFailures(), tests.get("expectedFailures"));
        assertEquals(expectedSummary.getFailures(), tests.get("failures"));
        assertEquals(expectedSummary.getErrors(), tests.get("errors"));
    }

    public void testTestResultsNestedSuites() throws Exception
    {
        final String PROCESSOR_NAME = "nesty";
        final String PROCESSOR_SUITE = "sweety has spaces & special % ch@r@cter$!";

        Hashtable<String, Object> antConfig = xmlRpcHelper.getAntConfig();
        antConfig.put(Constants.Project.AntCommand.TARGETS, "test");
        String projectPath = xmlRpcHelper.insertSingleCommandProject(random, GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(TEST_ANT_REPOSITORY), antConfig);

        Hashtable<String, Object> junitProcessor = xmlRpcHelper.createDefaultConfig(JUnitReportPostProcessorConfiguration.class);
        junitProcessor.put(NAME, PROCESSOR_NAME);
        junitProcessor.put("suite", PROCESSOR_SUITE);
        xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, POSTPROCESSORS), junitProcessor);

        insertTestCapture(projectPath, PROCESSOR_NAME);

        long buildId = xmlRpcHelper.runBuild(random);

        loginAsAdmin();

        // Test we can drill all the way down then back up again.
        BuildTestsPage testsPage = browser.openAndWaitFor(BuildTestsPage.class, random, buildId);

        assertTrue(testsPage.hasTests());
        StageTestsPage stageTestsPage = testsPage.clickStageAndWait("default");
        TestSuitePage topSuitePage = stageTestsPage.clickSuiteAndWait(PROCESSOR_SUITE);
        TestSuitePage nestedSuitePath = topSuitePage.clickSuiteAndWait("com.zutubi.testant.UnitTest");

        nestedSuitePath.clickSuiteCrumb(PROCESSOR_SUITE);
        topSuitePage.waitFor();
        topSuitePage.clickStageCrumb();
        stageTestsPage.waitFor();
        stageTestsPage.clickAllCrumb();
        testsPage.waitFor();
    }

    public void testOCUnitTestResults() throws Exception
    {
        String projectPath = xmlRpcHelper.insertSingleCommandProject(random, GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(OCUNIT_REPOSITORY), xmlRpcHelper.getAntConfig());

        Hashtable<String, Object> artifactConfig = xmlRpcHelper.createDefaultConfig(FileArtifactConfiguration.class);
        artifactConfig.put("name", "test report");
        artifactConfig.put("file", "results.txt");
        artifactConfig.put(POSTPROCESSORS, new Vector<String>(Arrays.asList(PathUtils.getPath(projectPath, POSTPROCESSORS, "ocunit output processor"))));
        xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE_NAME, COMMANDS, DEFAULT_COMMAND, ARTIFACTS), artifactConfig);

        long buildId = xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

        loginAsAdmin();

        BuildTestsPage testsPage = browser.openAndWaitFor(BuildTestsPage.class, random, buildId);
        TestResultSummary expectedSummary = new TestResultSummary(0, 0, 3, 0, 583);

        assertTrue(testsPage.hasTests());
        assertEquals(expectedSummary, testsPage.getTestSummary());

        StageTestsPage stageTestsPage = testsPage.clickStageAndWait("default");
        assertEquals(expectedSummary, stageTestsPage.getTestSummary());

        TestSuitePage allSuitePage = stageTestsPage.clickSuiteAndWait("All tests");
        assertEquals(expectedSummary, allSuitePage.getTestSummary());

        TestSuitePage suitePage = allSuitePage.clickSuiteAndWait("/Users/jhiggs/Documents/Projects/Connoisseur/build/Debug/UnitTests.octest(Tests)");
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

    public void testPersistentWorkDir() throws Exception
    {
        String projectPath = xmlRpcHelper.insertSimpleProject(random, false);

        String optionsPath = PathUtils.getPath(projectPath, Constants.Project.OPTIONS);
        Hashtable<String, Object> options = xmlRpcHelper.getConfig(optionsPath);
        options.put(Constants.Project.Options.PERSISTENT_WORK_DIR, "${data.dir}/customwork/${project}");
        xmlRpcHelper.saveConfig(optionsPath, options, false);

        String svnPath = PathUtils.getPath(projectPath, Constants.Project.SCM);
        Hashtable<String, Object> svn = xmlRpcHelper.getConfig(svnPath);
        svn.put(Constants.Project.Scm.CHECKOUT_SCHEME, CheckoutScheme.INCREMENTAL_UPDATE.toString());
        xmlRpcHelper.saveConfig(svnPath, svn, false);
        xmlRpcHelper.waitForProjectToInitialise(random);

        String stagePath = PathUtils.getPath(projectPath, Constants.Project.STAGES, "default");
        Hashtable<String, Object> stage = xmlRpcHelper.getConfig(stagePath);
        stage.put(Constants.Project.Stage.AGENT, PathUtils.getPath(AGENTS_SCOPE, MASTER_AGENT_NAME));
        xmlRpcHelper.saveConfig(stagePath, stage, false);

        xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

        String dataDir = xmlRpcHelper.getServerInfo().get(ConfigurationManager.CORE_PROPERTY_PULSE_DATA_DIR);
        File workDir = new File(FileSystemUtils.composeFilename(dataDir, "customwork", random));
        assertTrue(workDir.isDirectory());
        File buildFile = new File(workDir, "build.xml");
        assertTrue(buildFile.isFile());
    }

    public void testTerminateOnStageFailure() throws Exception
    {
        String projectPath = createProjectWithTwoAntStages("nosuchbuildfile.xml");
        setTerminateStageOnFailure(projectPath);

        long buildId = xmlRpcHelper.runBuild(random, ResultState.ERROR.getPrettyString(), BUILD_TIMEOUT);

        loginAsAdmin();
        browser.openAndWaitFor(BuildSummaryPage.class, random, buildId);
        assertTextPresent(String.format("Build terminated due to failure of stage '%s'", ProjectConfigurationWizard.DEFAULT_STAGE));
    }

    public void testTerminateOnStageFailureStageSucceeds() throws Exception
    {
        String projectPath = createProjectWithTwoAntStages("build.xml");
        setTerminateStageOnFailure(projectPath);

        long buildId = xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

        loginAsAdmin();
        browser.openAndWaitFor(BuildSummaryPage.class, random, buildId);
        assertTextNotPresent("terminated");
    }

    public void testTerminateOnStageFailureLimit() throws Exception
    {
        String projectPath = createProjectWithTwoAntStages("nosuchbuildfile.xml");
        String optionsPath = PathUtils.getPath(projectPath, Constants.Project.OPTIONS);
        Hashtable<String, Object> options = xmlRpcHelper.getConfig(optionsPath);
        options.put("stageFailureLimit", 1);
        xmlRpcHelper.saveConfig(optionsPath, options, false);

        long buildId = xmlRpcHelper.runBuild(random, ResultState.ERROR.getPrettyString(), BUILD_TIMEOUT);

        loginAsAdmin();
        browser.openAndWaitFor(BuildSummaryPage.class, random, buildId);
        assertTextPresent("Build terminated due to the stage failure limit (1) being reached");
    }

    private String createProjectWithTwoAntStages(String buildFile) throws Exception
    {
        String projectPath = xmlRpcHelper.insertSingleCommandProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(TRIVIAL_ANT_REPOSITORY), xmlRpcHelper.getAntConfig(buildFile));
        Hashtable<String, String> keys = new Hashtable<String, String>();
        keys.put(ProjectConfigurationWizard.DEFAULT_STAGE, "another-stage");
        xmlRpcHelper.cloneConfig(PathUtils.getPath(projectPath, Project.STAGES), keys);
        return projectPath;
    }

    private void setTerminateStageOnFailure(String projectPath) throws Exception
    {
        String defaultStagePath = PathUtils.getPath(projectPath, Project.STAGES, ProjectConfigurationWizard.DEFAULT_STAGE);
        Hashtable<String, Object> stage = xmlRpcHelper.getConfig(defaultStagePath);
        stage.put("terminateBuildOnFailure", true);
        xmlRpcHelper.saveConfig(defaultStagePath, stage, false);
    }

    private void insertTestCapture(String projectPath, String processorName) throws Exception
    {
        Hashtable<String, Object> dirArtifactConfig = xmlRpcHelper.createDefaultConfig(DirectoryArtifactConfiguration.class);
        dirArtifactConfig.put(NAME, "xml reports");
        dirArtifactConfig.put(BASE, "build/reports/xml");
        dirArtifactConfig.put(POSTPROCESSORS, new Vector<String>(Arrays.asList(PathUtils.getPath(projectPath, POSTPROCESSORS, processorName))));
        xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE_NAME, COMMANDS, DEFAULT_COMMAND, ARTIFACTS), dirArtifactConfig);
    }

    private String getPropertiesPath(String projectName)
    {
        return PathUtils.getPath(PROJECTS_SCOPE, projectName, "properties");
    }

    private void assertEnvironment(String projectName, long buildId, String... envs)
    {
        goToEnv(projectName, buildId);
        for (String env : envs)
        {
            assertTextPresent(env);
        }
    }

    private void goToEnv(String projectName, long buildId)
    {
        goToArtifact(projectName, buildId, "build", LOCATOR_ENV_ARTIFACT);
    }

    private void goToArtifact(String projectName, long buildId, String command, String artifact)
    {
        BuildDetailedViewPage detailedViewPage = browser.openAndWaitFor(BuildDetailedViewPage.class, projectName, buildId);
        detailedViewPage.clickCommand("default", command);
        browser.click(artifact);
        browser.waitForPageToLoad(10 * SECOND);
    }

    private String addResource(String agent, String name) throws Exception
    {
        Hashtable<String, Object> resource = xmlRpcHelper.createDefaultConfig(ResourceConfiguration.class);
        resource.put("name", name);
        return xmlRpcHelper.insertConfig(getPath(AGENTS_SCOPE, agent, "resources"), resource);
    }

    private Hashtable<String, Object> createRequiredResource(String resource, String version) throws Exception
    {
        Hashtable<String, Object> requirement = xmlRpcHelper.createDefaultConfig(ResourceRequirementConfiguration.class);
        requirement.put("resource", resource);
        if (StringUtils.stringSet(version))
        {
            requirement.put("version", version);
            requirement.put("defaultVersion", false);
        }

        return requirement;
    }

    private void triggerSuccessfulBuild(String projectName, String agent)
    {
        ProjectHomePage home = browser.openAndWaitFor(ProjectHomePage.class, projectName);
        home.triggerBuild();

        waitForBuildOnProjectHomePage(projectName, agent);
    }

    private void waitForBuildOnProjectHomePage(String projectName, String agent)
    {
        browser.openAndWaitFor(ProjectHomePage.class, projectName);

        String statusId = IDs.buildStatusCell(projectName, 1);
        browser.refreshUntilElement(statusId, BUILD_TIMEOUT);
        browser.refreshUntilText(statusId, BUILD_TIMEOUT, "success");

        assertEquals(agent, browser.getText(WebUtils.toValidHtmlName(IDs.stageAgentCell(projectName, 1, "default"))));
    }
}
