package com.zutubi.pulse.acceptance;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.acceptance.forms.admin.BuildStageForm;
import com.zutubi.pulse.acceptance.forms.admin.TriggerBuildForm;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.agents.AgentStatusPage;
import com.zutubi.pulse.acceptance.pages.browse.*;
import com.zutubi.pulse.acceptance.pages.dashboard.DashboardPage;
import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.acceptance.utils.workspace.SubversionWorkspace;
import com.zutubi.pulse.core.BootstrapCommand;
import com.zutubi.pulse.core.BootstrapCommandConfiguration;
import com.zutubi.pulse.core.RegexPatternConfiguration;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.api.DirectoryArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.FileArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.OutputProducingCommandSupport;
import com.zutubi.pulse.core.commands.core.*;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.FieldScope;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.TestResultSummary;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.config.api.CheckoutScheme;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;
import com.zutubi.pulse.master.tove.config.project.ResourceRequirementConfiguration;
import com.zutubi.pulse.master.tove.config.project.changeviewer.FisheyeConfiguration;
import com.zutubi.pulse.master.tove.config.project.commit.LinkTransformerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.BuildCompletedTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.CustomTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.MultiRecipeTypeConfiguration;
import com.zutubi.pulse.master.xwork.actions.project.ViewChangesAction;
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
import static com.zutubi.pulse.acceptance.Constants.Project.*;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.ARTIFACTS;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.Artifact.POSTPROCESSORS;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.DirectoryArtifact.BASE;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.FileArtifact.FILE;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.FileArtifact.PUBLISH;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.DEFAULT_RECIPE_NAME;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.RECIPES;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.COMMANDS;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.DEFAULT_COMMAND;
import static com.zutubi.pulse.core.dependency.ivy.IvyStatus.STATUS_INTEGRATION;
import static com.zutubi.pulse.core.dependency.ivy.IvyStatus.STATUS_RELEASE;
import static com.zutubi.pulse.master.agent.AgentManager.GLOBAL_AGENT_NAME;
import static com.zutubi.pulse.master.agent.AgentManager.MASTER_AGENT_NAME;
import static com.zutubi.pulse.master.model.ProjectManager.GLOBAL_PROJECT_NAME;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.AGENTS_SCOPE;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.PROJECTS_SCOPE;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard.DEFAULT_STAGE;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import static com.zutubi.util.CollectionUtils.asPair;
import static com.zutubi.util.Constants.SECOND;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

/**
 * An acceptance test that adds a very simple project and runs a build as a
 * sanity test.
 */
@SuppressWarnings({"unchecked"})
public class BuildAcceptanceTest extends SeleniumTestBase
{
    private static final int BUILD_TIMEOUT = 90000;

    private static final String CHANGE_AUTHOR = "pulse";
    private static final String CHANGE_COMMENT = "Edit build file.";
    private static final String CHANGE_FILENAME = "build.xml";

    private static final String ANT_PROCESSOR = "ant output processor";
    private static final String JUNIT_PROCESSOR = "junit xml report processor";
    private static final String CUSTOM_FIELD_PROCESSOR = "custom field processor";

    private static final String MESSAGE_BUILD_COMPLETED = "Build completed with status success";
    private static final String MESSAGE_CHECKING_REQUIREMENTS = "Checking recipe agent requirements...";
    private static final String MESSAGE_RECIPE_COMPLETED = "Recipe '[default]' completed with status success";

    private Repository repository;
    private ConfigurationHelper configurationHelper;
    private ProjectConfigurations projects;

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

        ConfigurationHelperFactory configurationHelperFactory = new SingletonConfigurationHelperFactory();
        configurationHelper = configurationHelperFactory.create(xmlRpcHelper);
        projects = new ProjectConfigurations(configurationHelper);
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testSimpleBuild() throws Exception
    {
        browser.loginAsAdmin();
        addProject(random, true);

        triggerSuccessfulBuild(random, MASTER_AGENT_NAME);

        // Check changes page handling of no changes.
        BuildChangesPage changesPage = browser.openAndWaitFor(BuildChangesPage.class, random, 1L);
        assertFalse(changesPage.hasChanges());
        assertFalse(changesPage.isCompareToPopDownPresent());
        assertTextPresent(Messages.getInstance(ViewChangesAction.class).format("changes.none"));

        // Check some properties
        EnvironmentArtifactPage envPage = browser.openAndWaitFor(EnvironmentArtifactPage.class, random, 1L, "default", "build");
        assertTrue(envPage.isPulsePropertyPresentWithValue(BuildProperties.PROPERTY_LOCAL_BUILD, Boolean.toString(false)));
        assertTrue(envPage.isPulsePropertyPresentWithValue(BuildProperties.PROPERTY_PERSONAL_BUILD, Boolean.toString(false)));
        assertTrue(envPage.isPulsePropertyPresentWithValue(BuildProperties.PROPERTY_OWNER, random));
        assertTrue(envPage.isPulsePropertyPresentWithValue(BuildProperties.PROPERTY_RECIPE_STATUS, "success"));
    }
    
    public void testNoChangesBetweenBuilds() throws Exception
    {
        addProject(random, true);
        xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);
        xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

        browser.loginAsAdmin();

        BuildChangesPage changesPage = browser.openAndWaitFor(BuildChangesPage.class, random, 2L);
        assertFalse(changesPage.hasChanges());
        // Unlike where there are no previous builds, in this case we expect
        // the compare-to popdown.
        assertTrue(changesPage.isCompareToPopDownPresent());
        assertTextPresent(Messages.getInstance(ViewChangesAction.class).format("changes.none"));
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
        browser.loginAsAdmin();
        BuildChangesPage changesPage = browser.openAndWaitFor(BuildChangesPage.class, random, buildNumber);
        assertTextPresent(BuildChangesPage.formatChangesSince(buildNumber));

        List<Changelist> changelists = changesPage.getChangelists();
        assertEquals(1, changelists.size());
        assertBuildFileChangelist(changelists.get(0), revisionString);

        // Pop down the compare to box to make sure it appears.
        changesPage.clickCompareToPopDown();

        // Check the changelist view too.
        List<Long> changeIds = changesPage.getChangeIds();
        assertEquals(1, changeIds.size());
        ViewChangelistPage changelistPage = browser.openAndWaitFor(ViewChangelistPage.class, random, buildNumber, changeIds.get(0), revisionString);
        assertBuildFileChangelist(changelistPage.getChangelist(), revisionString);
        
        // Check appearance of change on user's dashboard.
        DashboardPage dashboardPage = browser.openAndWaitFor(DashboardPage.class);
        DashboardPage.ProjectChange change = dashboardPage.getProjectChange(0);
        assertEquals(ResultState.SUCCESS, change.status);
        assertEquals(random + " :: build " + buildNumber, change.builds);
    }

    public void testChangelistWithManyFiles() throws Exception
    {
        final int CHANGE_COUNT = 102;

        // Create a new repo area so we don't conflict with other tests.
        String subversionUrl = Constants.SUBVERSION_ACCEPT_REPO + random;
        File wcDir = createTempDirectory();
        SubversionWorkspace workspace = new SubversionWorkspace(wcDir, CHANGE_AUTHOR, CHANGE_AUTHOR);
        workspace.doCopy("copy triviant", Constants.TRIVIAL_ANT_REPOSITORY, subversionUrl);
        
        // Run an initial build
        xmlRpcHelper.insertSingleCommandProject(random, GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(subversionUrl), xmlRpcHelper.getAntConfig());
        xmlRpcHelper.runBuild(random);

        // Commit a large change to the repository.
        String revisionString;
        try
        {
            workspace.doCheckout(subversionUrl);

            File[] files = new File[CHANGE_COUNT];
            for (int i = 0; i < CHANGE_COUNT; i++)
            {
                files[i] = new File(wcDir, String.format("file-%03d.txt", i));
                FileSystemUtils.createFile(files[i], "content");
            }
            
            workspace.doAdd(files);
            revisionString = workspace.doCommit(CHANGE_COMMENT, files);
        }
        finally
        {
            IOUtils.close(workspace);
        }
        
        long buildNumber = xmlRpcHelper.runBuild(random);

        browser.loginAsAdmin();
        BuildChangesPage changesPage = browser.openAndWaitFor(BuildChangesPage.class, random, buildNumber);
        assertTextPresent(String.format("%d more files", CHANGE_COUNT - 5));

        List<Long> changeIds = changesPage.getChangeIds();
        assertEquals(1, changeIds.size());
        ViewChangelistPage changelistPage = browser.openAndWaitFor(ViewChangelistPage.class, random, buildNumber, changeIds.get(0), revisionString);
        Changelist changelist = changelistPage.getChangelist();
        assertEquals(100, changelist.getChanges().size());
        assertTrue(changelistPage.isNextLinkPresent());
        assertFalse(changelistPage.isPreviousLinkPresent());

        changelistPage.clickNext();
        browser.waitForPageToLoad();
        changelist = changelistPage.getChangelist();
        assertEquals(CHANGE_COUNT - 100, changelist.getChanges().size());
        assertFalse(changelistPage.isNextLinkPresent());
        assertTrue(changelistPage.isPreviousLinkPresent());
    }
    
    public void testChangeAffectsViewableAndUnviewableProjects() throws Exception
    {
        String viewableProject = random + "-viewable";
        String unviewableProject = random + "-unviewable";
        String regularUser = random + "-user";
        
        addProject(viewableProject, true);
        String unviewableProjectPath = addProject(unviewableProject, true);
        xmlRpcHelper.insertTrivialUser(regularUser);
        
        // Remove permissions that allow normal users to view the invisible
        // project (so only admin can see it).
        xmlRpcHelper.deleteAllConfigs(PathUtils.getPath(unviewableProjectPath, Constants.Project.PERMISSIONS, PathUtils.WILDCARD_ANY_ELEMENT));
        
        xmlRpcHelper.runBuild(viewableProject);
        xmlRpcHelper.runBuild(unviewableProject);

        // Commit a change to the repository.  Note monitoring the SCM is
        // disabled for these projects, so no chance of a build being started
        // by this change.
        String revisionString = editAndCommitBuildFile();
        
        long viewableBuildNumber = xmlRpcHelper.runBuild(viewableProject);
        long unviewableBuildNumber = xmlRpcHelper.runBuild(unviewableProject);

        // Check the changes tab.
        browser.loginAsAdmin();
        BuildChangesPage changesPage = browser.openAndWaitFor(BuildChangesPage.class, viewableProject, viewableBuildNumber);
        List<Long> changeIds = changesPage.getChangeIds();
        assertEquals(1, changeIds.size());
        ViewChangelistPage changelistPage = browser.openAndWaitFor(ViewChangelistPage.class, viewableProject, viewableBuildNumber, changeIds.get(0), revisionString);
        List<Pair<String, Long>> builds = changelistPage.getBuilds();
        assertEquals(2, builds.size());
        assertThat(builds, hasItem(new Pair<String, Long>(viewableProject, viewableBuildNumber)));
        assertThat(builds, hasItem(new Pair<String, Long>(unviewableProject, unviewableBuildNumber)));
        
        // Dashboard project changes table should show popup for multiple
        // builds.
        DashboardPage dashboardPage = browser.openAndWaitFor(DashboardPage.class);
        DashboardPage.ProjectChange change = dashboardPage.getProjectChange(0);
        assertEquals(ResultState.SUCCESS, change.status);
        assertEquals("2 builds", change.builds);

        browser.logout();
        browser.login(regularUser, "");
        
        // Regular user should only see the visible project.
        changelistPage.openAndWaitFor();
        builds = changelistPage.getBuilds();
        assertEquals(1, builds.size());
        assertThat(builds, hasItem(new Pair<String, Long>(viewableProject, viewableBuildNumber)));
        
        // Check other pages that we can view where the changelist appears.
        browser.openAndWaitFor(ProjectHomePage.class, viewableProject);

        dashboardPage = browser.openAndWaitFor(DashboardPage.class);
        change = dashboardPage.getProjectChange(0);
        assertEquals(ResultState.SUCCESS, change.status);
        assertEquals(viewableProject + " :: build " + viewableBuildNumber, change.builds);
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

        browser.loginAsAdmin();
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
        return editAndCommitFile(TRIVIAL_ANT_REPOSITORY, CHANGE_FILENAME, CHANGE_COMMENT,
                "<?xml version=\"1.0\"?>\n" +
                "<project default=\"default\">\n" +
                "    <target name=\"default\">\n" +
                "        <echo message=\"" + random + "\"/>\n" +
                "    </target>\n" +
                "</project>");
    }

    private String editAndCommitFile(String repository, String filename, String comment, String newContent) throws IOException, SVNException
    {
        File wcDir = createTempDirectory();
        SubversionWorkspace workspace = new SubversionWorkspace(wcDir, CHANGE_AUTHOR, CHANGE_AUTHOR);
        try
        {
            workspace.doCheckout(repository);

            File buildFile = new File(wcDir, filename);
            assertTrue(buildFile.exists());
            FileSystemUtils.createFile(buildFile, newContent);

            return workspace.doCommit(comment, buildFile);
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
        browser.loginAsAdmin();

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
    
    public void testSummaryView() throws Exception
    {
        final String FEATURES_PROCESSOR = "features processor";

        String projectPath = xmlRpcHelper.insertSingleCommandProject(random, GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(ALL_ANT_REPOSITORY), xmlRpcHelper.getAntConfig());
        insertTestCapture(projectPath, JUNIT_PROCESSOR);

        insertFeaturesProcessor(random, FEATURES_PROCESSOR);
        String featuresArtifactPath = insertFileArtifact(projectPath, "features", "features.txt", FEATURES_PROCESSOR, false);
        
        Hashtable<String, Object> transformer = xmlRpcHelper.createDefaultConfig(LinkTransformerConfiguration.class);
        transformer.put("name", "issues");
        transformer.put("expression", "CIB-[0-9]+");
        transformer.put("url", "http://jira.zutubi.com/$0");
        xmlRpcHelper.insertConfig(getPath(projectPath, COMMIT_MESSAGE_TRANSFORMERS), transformer);

        // Run two builds, to generate a change between them.
        xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);
        editAndCommitFile(ALL_ANT_REPOSITORY, "expected-failures.txt", "CIB-123: fixed it", random);
        long buildNumber = xmlRpcHelper.runBuild(random);

        browser.loginAsAdmin();
        BuildSummaryPage summaryPage = browser.openAndWaitFor(BuildSummaryPage.class, random, buildNumber);
        assertTrue(summaryPage.isBuildBasicsPresent());
        assertEquals(asPair("status", "failure"), summaryPage.getBuildBasicsRow(0));

        assertTrue(summaryPage.isFeaturesTablePresent(Feature.Level.ERROR));
        assertTrue(summaryPage.isFeaturesTablePresent(Feature.Level.WARNING));
        assertFalse(summaryPage.isFeaturesTablePresent(Feature.Level.INFO));
        assertTrue(summaryPage.isTestFailuresTablePresent());
        
        assertTrue(summaryPage.isRelatedLinksTablePresent());
        assertEquals("CIB-123", summaryPage.getRelatedLinkText(0));

        // No featured artifacts in this build, check, then mark one as
        // featured and do another build.
        assertFalse(summaryPage.isFeaturedArtifactsTablePresent());
        Hashtable<String, Object> artifact = xmlRpcHelper.getConfig(featuresArtifactPath);
        artifact.put("featured", true);
        xmlRpcHelper.saveConfig(featuresArtifactPath, artifact, false);
        buildNumber = xmlRpcHelper.runBuild(random);

        summaryPage = browser.openAndWaitFor(BuildSummaryPage.class, random, buildNumber);
        assertTrue(summaryPage.isFeaturedArtifactsTablePresent());
        assertEquals("stage :: " + DEFAULT_STAGE, summaryPage.getFeaturedArtifactsRow(0));
        assertEquals("features", summaryPage.getFeaturedArtifactsRow(1));
    }

    public void testSummaryViewNoContentForRightPane() throws Exception
    {
        String projectName = random + "-project";

        addProject(projectName, true);
        xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);

        browser.loginAsAdmin();
        final BuildSummaryPage summaryPage = browser.openAndWaitFor(BuildSummaryPage.class, projectName, 1L);
        assertTrue(summaryPage.isRightPaneVisible());
        browser.logout();

        GlobalConfiguration globalConfig = configurationHelper.getConfiguration(GlobalConfiguration.SCOPE_NAME, GlobalConfiguration.class);
        if (!globalConfig.isAnonymousAccessEnabled())
        {
            globalConfig.setAnonymousAccessEnabled(true);
            configurationHelper.update(globalConfig, false);
            browser.newSession();
        }

        try
        {
            summaryPage.openAndWaitFor();
            AcceptanceTestUtils.waitForCondition(new Condition()
            {
                public boolean satisfied()
                {
                    return !summaryPage.isRightPaneVisible();
                }
            }, BUILD_TIMEOUT, "right pane to be hidden");
        }
        finally
        {
            globalConfig = configurationHelper.getConfiguration(GlobalConfiguration.SCOPE_NAME, GlobalConfiguration.class);
            globalConfig.setAnonymousAccessEnabled(false);
            configurationHelper.update(globalConfig, false);
        }
    }

    public void testDetailsView() throws Exception
    {
        final String UPSTREAM_ARTIFACT = "file";
        final String BUILD_FIELD_PROCESSOR = "build field processor";
        final String FEATURES_PROCESSOR = "features processor";

        String upstreamProjectName = random + "-upstream";
        Hashtable<String, Object> upstreamAntConfig = xmlRpcHelper.getAntConfig();
        upstreamAntConfig.put(Project.AntCommand.ARGUMENTS, "-Dcreate.list=file.txt");
        upstreamAntConfig.put(Project.AntCommand.TARGETS, "create");
        String upstreamProjectPath = xmlRpcHelper.insertSingleCommandProject(upstreamProjectName, GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(DEP_ANT_REPOSITORY), upstreamAntConfig);
        insertFileArtifact(upstreamProjectPath, UPSTREAM_ARTIFACT, "file.txt", null, true);
        xmlRpcHelper.runBuild(upstreamProjectName, BUILD_TIMEOUT);
        
        String mainProjectName = random + "-main";
        String mainProjectPath = xmlRpcHelper.insertSingleCommandProject(mainProjectName, GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(ALL_ANT_REPOSITORY), xmlRpcHelper.getAntConfig());
        insertTestCapture(mainProjectPath, JUNIT_PROCESSOR);

        String mainProcessorsPath = getPath(mainProjectPath, POSTPROCESSORS);
        Hashtable<String, Object> buildFieldsProcessorConfig = xmlRpcHelper.createDefaultConfig(CustomFieldsPostProcessorConfiguration.class);
        buildFieldsProcessorConfig.put(NAME, BUILD_FIELD_PROCESSOR);
        buildFieldsProcessorConfig.put("scope", FieldScope.BUILD.toString());
        xmlRpcHelper.insertConfig(mainProcessorsPath, buildFieldsProcessorConfig);

        insertFeaturesProcessor(mainProjectName, FEATURES_PROCESSOR);
        
        insertFileArtifact(mainProjectPath, "custom fields", "build.properties", BUILD_FIELD_PROCESSOR, false);
        insertFileArtifact(mainProjectPath, "stage fields", "stage.properties", CUSTOM_FIELD_PROCESSOR, false);
        insertFileArtifact(mainProjectPath, "features", "features.txt", FEATURES_PROCESSOR, false);

        String mainDependenciesPath = getPath(mainProjectPath, DEPENDENCIES);
        Hashtable<String, Object> dependenciesConfig = xmlRpcHelper.getConfig(mainDependenciesPath);
        dependenciesConfig.put("syncDestination", false);
        xmlRpcHelper.saveConfig(mainDependenciesPath, dependenciesConfig, false);
        
        Hashtable<String, Object> dependencyConfig = xmlRpcHelper.createDefaultConfig(DependencyConfiguration.class);
        dependencyConfig.put("project", upstreamProjectPath);
        xmlRpcHelper.insertConfig(getPath(mainDependenciesPath, DEPENDENCIES), dependencyConfig);
        
        xmlRpcHelper.runBuild(mainProjectName, BUILD_TIMEOUT);

        browser.loginAsAdmin();
        BuildDetailsPage detailsPage = browser.openAndWaitFor(BuildDetailsPage.class, mainProjectName, 1L);
        assertTrue(detailsPage.isBuildBasicsPresent());
        assertEquals(asPair("status", "failure"), detailsPage.getBuildBasicsRow(0));
        assertTrue(detailsPage.isFeaturesTablePresent(Feature.Level.ERROR));
        assertFalse(detailsPage.isFeaturesTablePresent(Feature.Level.WARNING));
        assertFalse(detailsPage.isFeaturesTablePresent(Feature.Level.INFO));
        assertTrue(detailsPage.isCustomFieldsTablePresent());
        assertEquals(asPair("property1", "value1"), detailsPage.getCustomField(0));
        assertTrue(detailsPage.isDependenciesTablePresent());
        
        detailsPage.clickStageAndWait(ProjectConfigurationWizard.DEFAULT_STAGE);
        assertFalse(detailsPage.isBuildBasicsPresent());
        assertTrue(detailsPage.isStageBasicsPresent());
        assertEquals(asPair("status", "failure"), detailsPage.getStageBasicsRow(0));
        assertEquals(asPair("recipe", "[default]"), detailsPage.getStageBasicsRow(1));
        assertTrue(detailsPage.isFeaturesTablePresent(Feature.Level.ERROR));
        assertFalse(detailsPage.isFeaturesTablePresent(Feature.Level.WARNING));
        assertFalse(detailsPage.isFeaturesTablePresent(Feature.Level.INFO));
        assertTrue(detailsPage.isCustomFieldsTablePresent());
        assertEquals(asPair("stageproperty1", "value1"), detailsPage.getCustomField(0));

        detailsPage.clickCommandAndWait(ProjectConfigurationWizard.DEFAULT_STAGE, ProjectConfigurationWizard.DEFAULT_COMMAND);
        assertFalse(detailsPage.isBuildBasicsPresent());
        assertFalse(detailsPage.isStageBasicsPresent());
        assertTrue(detailsPage.isCommandBasicsPresent());
        assertEquals(asPair("status", "failure"), detailsPage.getCommandBasicsRow(0));
        assertTrue(detailsPage.isCommandPropertiesPresent());
        assertEquals(asPair("build file", "build.xml"), detailsPage.getCommandPropertiesRow(0));
        assertTrue(detailsPage.isCommandImplicitArtifactsPresent());
        BuildDetailsPage.ImplicitArtifactRow row = detailsPage.getCommandImplicitArtifactRow(0);
        assertEquals("command output/output.txt", row.getPath());
        assertEquals(Arrays.asList("download", "decorate"), row.getActions());
        assertTrue(detailsPage.isFeaturesTablePresent(Feature.Level.ERROR));
        assertTrue(detailsPage.isFeaturesTablePresent(Feature.Level.WARNING));
        assertTrue(detailsPage.isFeaturesTablePresent(Feature.Level.INFO));
        assertTextPresent("error feature");
        assertTextPresent("warning feature");
        assertTextPresent("info feature");
        assertTextPresent("context line 1");
        assertTextPresent("info context line 2");
    }

    private void insertFeaturesProcessor(String projectName, String processorName) throws Exception
    {
        Hashtable<String, Object> featuresProcessorConfig = xmlRpcHelper.createDefaultConfig(RegexPostProcessorConfiguration.class);
        featuresProcessorConfig.put(NAME, processorName);
        featuresProcessorConfig.put("leadingContext", 2);
        featuresProcessorConfig.put("trailingContext", 2);
        Vector<Hashtable<String, Object>> patterns = new Vector<Hashtable<String, Object>>(3);
        patterns.add(createRegexPattern(Feature.Level.ERROR, "^error:"));
        patterns.add(createRegexPattern(Feature.Level.WARNING, "^warning:"));
        patterns.add(createRegexPattern(Feature.Level.INFO, "^info:"));
        featuresProcessorConfig.put("patterns", patterns);
        xmlRpcHelper.insertConfig(getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, projectName, POSTPROCESSORS), featuresProcessorConfig);
    }

    private Hashtable<String, Object> createRegexPattern(Feature.Level category, String expression)  throws Exception
    {
        Hashtable<String, Object> pattern = xmlRpcHelper.createDefaultConfig(RegexPatternConfiguration.class);
        pattern.put("category", category.toString());
        pattern.put("expression", expression);
        return pattern;
    }

    private String insertFileArtifact(String projectPath, String name, String file, String processorName, boolean publish) throws Exception
    {
        Hashtable<String, Object> artifactConfig = xmlRpcHelper.createDefaultConfig(FileArtifactConfiguration.class);
        artifactConfig.put(NAME, name);
        artifactConfig.put(FILE, file);
        if (processorName != null)
        {
            artifactConfig.put(POSTPROCESSORS, new Vector<String>(Arrays.asList(PathUtils.getPath(projectPath, POSTPROCESSORS, processorName))));
        }
        artifactConfig.put(PUBLISH, publish);
        return xmlRpcHelper.insertConfig(getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE_NAME, COMMANDS, DEFAULT_COMMAND, ARTIFACTS), artifactConfig);
    }
    
    public void testDetailsViewInProgress() throws Exception
    {
        File tempDir = createTempDirectory();
        try
        {
            final WaitProject project = projects.createWaitAntProject(tempDir, random);
            configurationHelper.insertProject(project.getConfig(), false);
            
            xmlRpcHelper.waitForProjectToInitialise(project.getName());
            xmlRpcHelper.triggerBuild(project.getName());
            xmlRpcHelper.waitForBuildInProgress(project.getName(), 1);
            
            browser.loginAsAdmin();
            final BuildDetailsPage detailsPage = browser.createPage(BuildDetailsPage.class, project.getName(), 1L);
            AcceptanceTestUtils.waitForCondition(new Condition()
            {
                public boolean satisfied()
                {
                    detailsPage.openAndWaitFor();
                    detailsPage.clickCommandAndWait(DEFAULT_STAGE, BootstrapCommandConfiguration.COMMAND_NAME);
                    return ResultState.SUCCESS.getPrettyString().equals(detailsPage.getBasicsValue("status"));
                }
            }, BUILD_TIMEOUT, "bootstrap command to complete");

            assertFalse(detailsPage.isBasicsRowPresent("errors"));
            assertFalse(detailsPage.isBasicsRowPresent("warnings"));
            
            project.releaseBuild();
            xmlRpcHelper.waitForBuildToComplete(project.getName(), 1);

            detailsPage.openAndWaitFor();
            detailsPage.clickCommandAndWait(DEFAULT_STAGE, BootstrapCommandConfiguration.COMMAND_NAME);

            assertTrue(detailsPage.isBasicsRowPresent("errors"));
            assertEquals("0", detailsPage.getBasicsValue("errors"));
            assertTrue(detailsPage.isBasicsRowPresent("warnings"));
            assertEquals("0", detailsPage.getBasicsValue("warnings"));
        }
        finally
        {
            removeDirectory(tempDir);
        }
    }
    
    public void testPulseEnvironmentVariables() throws Exception
    {
        browser.loginAsAdmin();
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

        browser.loginAsAdmin();
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

        browser.loginAsAdmin();
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

        browser.loginAsAdmin();
        triggerSuccessfulBuild(projectName, MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "referer=ref ee");
    }

    public void testProjectPropertyReferencesAgentName() throws Exception
    {
        String projectName = random + "-project";
        ensureProject(projectName);
        assignStageToAgent(projectName, DEFAULT_STAGE, MASTER_AGENT_NAME);
        xmlRpcHelper.insertProjectProperty(projectName, "pp", "ref ${agent}", true, true, false);

        browser.loginAsAdmin();
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

        browser.loginAsAdmin();
        triggerSuccessfulBuild(projectName, MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "rp=ref " + MASTER_AGENT_NAME);
    }

    public void testSuppressedProperty() throws Exception
    {
        String projectName = random + "-project";
        ensureProject(projectName);
        assignStageToAgent(projectName, DEFAULT_STAGE, MASTER_AGENT_NAME);
        String suppressedName = "PULSE_TEST_SUPPRESSED";
        String suppressedValue = random + "-suppress";
        xmlRpcHelper.insertProjectProperty(projectName, suppressedName, suppressedValue, false, true, false);

        browser.loginAsAdmin();
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

        browser.loginAsAdmin();
        goToArtifact(random, 1, OutputProducingCommandSupport.OUTPUT_NAME, OutputProducingCommandSupport.OUTPUT_FILE);
        assertTextPresent(TRIVIAL_ANT_REPOSITORY);
    }

    public void testBuildLogs() throws Exception
    {
        addProject(random, true);

        browser.loginAsAdmin();
        triggerSuccessfulBuild(random, MASTER_AGENT_NAME);

        // The logs tab, which should show us the first stage.
        BuildLogsPage logsPage = browser.openAndWaitFor(BuildLogsPage.class, random, 1L, DEFAULT_STAGE);
        assertTrue(logsPage.isLogAvailable());
        assertTextPresent(MESSAGE_RECIPE_COMPLETED);

        if (browser.isFirefox())
        {
            logsPage.clickDownloadLink();
            browser.waitForPageToLoad();
            assertTextPresent(MESSAGE_CHECKING_REQUIREMENTS);
        }

        // Direct to the build log (high-level build messages).
        BuildLogPage logPage = browser.openAndWaitFor(BuildLogPage.class, random, 1L);
        assertTrue(logPage.isLogAvailable());
        assertTextPresent(MESSAGE_BUILD_COMPLETED);

        StageLogPage stageLogPage = browser.createPage(StageLogPage.class, random, 1L, DEFAULT_STAGE);
        if (browser.isFirefox())
        {
            // Unfortunately JS security prevents us using the combo in proxy
            // mode.
            stageLogPage.openAndWaitFor();
        }
        else
        {
            // Use the combo to switch to a stage log.
            logPage.selectStage(DEFAULT_STAGE);
            stageLogPage.waitFor();
        }
        assertTrue(stageLogPage.isLogAvailable());
        assertTextPresent(MESSAGE_RECIPE_COMPLETED);

        // Change the settings via the popup
        int maxLines = stageLogPage.getMaxLines();
        TailSettingsDialog dialog = stageLogPage.clickConfigureAndWaitForDialog();
        dialog.setMaxLines(maxLines + 5);
        dialog.clickApply();
        browser.waitForPageToLoad();
        assertEquals(maxLines + 5, stageLogPage.getMaxLines());
    }

    public void testArtifactTab() throws Exception
    {
        String userLogin = random + "-user";
        String projectName = random + "-project";

        xmlRpcHelper.insertTrivialUser(userLogin);

        AntProjectHelper project = projects.createTrivialAntProject(projectName);
        FileArtifactConfiguration explicitArtifact = project.addArtifact("explicit", "build.xml");
        FileArtifactConfiguration featuredArtifact = project.addArtifact("featured", "build.xml");
        featuredArtifact.setFeatured(true);
        configurationHelper.insertProject(project.getConfig(), false);

        long buildNumber = xmlRpcHelper.runBuild(projectName);

        browser.login(userLogin, "");

        BuildArtifactsPage page = browser.openAndWaitFor(BuildArtifactsPage.class, projectName, buildNumber);
        assertEquals(User.DEFAULT_ARTIFACTS_FILTER, page.getCurrentFilter());
        assertFalse(page.isArtifactListed(OutputProducingCommandSupport.OUTPUT_NAME));
        assertTrue(page.isArtifactListed(explicitArtifact.getName()));
        assertTrue(page.isArtifactListed(featuredArtifact.getName()));

        page.setFilterAndWait("");
        assertTrue(page.isArtifactListed(OutputProducingCommandSupport.OUTPUT_NAME));
        assertTrue(page.isArtifactListed(explicitArtifact.getName()));
        assertTrue(page.isArtifactListed(featuredArtifact.getName()));

        page.setFilterAndWait("featured");
        assertFalse(page.isArtifactListed(OutputProducingCommandSupport.OUTPUT_NAME));
        assertFalse(page.isArtifactListed(explicitArtifact.getName()));
        assertTrue(page.isArtifactListed(featuredArtifact.getName()));

        page.clickSaveFilterAndWait();

        browser.refresh();
        page.waitForReload();
        
        assertEquals("featured", page.getCurrentFilter());
        assertFalse(page.isArtifactListed(OutputProducingCommandSupport.OUTPUT_NAME));
        assertFalse(page.isArtifactListed(explicitArtifact.getName()));
        assertTrue(page.isArtifactListed(featuredArtifact.getName()));

        assertTrue(page.isArtifactFileListed(featuredArtifact.getName(), "build.xml"));
    }

    public void testDownloadArtifactLink() throws Exception
    {
        // CIB-1724: download raw artifacts via 2.0 url scheme.
        addProject(random, true);
        long buildNumber = xmlRpcHelper.runBuild(random);
        Hashtable<String, Object> outputArtifact = getArtifact(random, buildNumber, "command output");
        String permalink = (String) outputArtifact.get("permalink");
        // This is to check for the new 2.0-ised URL.
        assertTrue(permalink.contains("/downloads/"));

        browser.loginAsAdmin();
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
        artifactConfig.put(Project.Command.FileArtifact.FILE, ARTIFACT_FILENAME);
        artifactConfig.put(TYPE, CONTENT_TYPE);
        xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE_NAME, COMMANDS, DEFAULT_COMMAND, ARTIFACTS), artifactConfig);

        int buildNumber = xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

        Hashtable<String, Object> artifact = getArtifact(random, buildNumber, ARTIFACT_NAME);
        String url = getArtifactFileUrl(ARTIFACT_FILENAME, artifact);
        Header contentTypeHeader = AcceptanceTestUtils.readHttpHeader(url, "Content-Type");
        assertNotNull(contentTypeHeader);
        assertEquals(CONTENT_TYPE, contentTypeHeader.getValue().trim());
    }

    public void testArtifactHashing() throws Exception
    {
        final String NAME_NOT_HASHED = "unabashed";
        final String NAME_HASHED = "hashed";
        final String BUILD_FILE = "build.xml";

        AntProjectHelper project = projects.createTrivialAntProject(random);
        project.addArtifact(NAME_NOT_HASHED, BUILD_FILE);
        FileArtifactConfiguration hashedArtifact = project.addArtifact(NAME_HASHED, BUILD_FILE);
        hashedArtifact.setCalculateHash(true);
        hashedArtifact.setHashAlgorithm(CommandContext.HashAlgorithm.MD5);
        configurationHelper.insertProject(project.getConfig(), false);

        long buildNumber = xmlRpcHelper.runBuild(random);

        Hashtable<String, Object> hashedInfo = getArtifact(random, buildNumber, NAME_HASHED);
        String content = AcceptanceTestUtils.readUriContent(getArtifactFileUrl(BUILD_FILE, hashedInfo));
        String expectedHash = SecurityUtils.md5Digest(content);

        browser.loginAsAdmin();

        BuildArtifactsPage page = browser.openAndWaitFor(BuildArtifactsPage.class, random, buildNumber);
        assertTrue(page.isArtifactFileListed(NAME_NOT_HASHED, BUILD_FILE));
        assertEquals("", page.getArtifactFileHash(NAME_NOT_HASHED, BUILD_FILE));
        assertTrue(page.isArtifactFileListed(NAME_HASHED, BUILD_FILE));
        assertEquals(expectedHash, page.getArtifactFileHash(NAME_HASHED, BUILD_FILE));
    }

    private Hashtable<String, Object> getArtifact(String project, long buildNumber, final String name) throws Exception
    {
        Vector<Hashtable<String, Object>> artifacts = xmlRpcHelper.getArtifactsInBuild(project, buildNumber);
        Hashtable<String, Object> artifact = CollectionUtils.find(artifacts, new Predicate<Hashtable<String, Object>>()
        {
            public boolean satisfied(Hashtable<String, Object> artifact)
            {
                return name.equals(artifact.get("name"));
            }
        });

        assertNotNull(artifact);
        return artifact;
    }

    private String getArtifactFileUrl(String filename, Hashtable<String, Object> artifact)
    {
        String base = browser.getBaseUrl();
        if (base.endsWith("/"))
        {
            base = base.substring(0, base.length() - 1);
        }

        return base + artifact.get("permalink") + filename;
    }

    public void testManualTriggerBuildWithPrompt() throws Exception
    {
        browser.loginAsAdmin();
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
        browser.loginAsAdmin();
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
        browser.loginAsAdmin();
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

        browser.loginAsAdmin();
        assertEnvironment(buildCompletedProject, 1, "PULSE_TP=tpv");
    }

    public void testVersionedBuildWithImports() throws Exception
    {
        browser.loginAsAdmin();
        xmlRpcHelper.insertProject(random, GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(VERSIONED_REPOSITORY), xmlRpcHelper.createVersionedConfig("pulse/pulse.xml"));

        triggerSuccessfulBuild(random, MASTER_AGENT_NAME);
    }

    public void testTestResults() throws Exception
    {
        final String SUCCESSFUL_TEST = "testAdd";

        Hashtable<String, Object> antConfig = xmlRpcHelper.getAntConfig();
        antConfig.put(Constants.Project.AntCommand.TARGETS, "test");
        String projectPath = xmlRpcHelper.insertSingleCommandProject(random, GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(TEST_ANT_REPOSITORY), antConfig);
        insertTestCapture(projectPath, JUNIT_PROCESSOR);

        TestSuitePage suitePage = buildAndCheckTestSummary(false, new TestResultSummary(0, 0, 1, 0, 2));

        // Test filtering out of successful tests (including stickiness).
        suitePage.openAndWaitFor();
        assertEquals(TestSuitePage.FILTER_NONE, suitePage.getCurrentFilter());
        assertTrue(suitePage.isTestCaseVisible(SUCCESSFUL_TEST));
        suitePage.setFilterAndWait(TestSuitePage.FILTER_BROKEN);
        assertEquals(TestSuitePage.FILTER_BROKEN, suitePage.getCurrentFilter());
        assertFalse(suitePage.isTestCaseVisible(SUCCESSFUL_TEST));
        
        suitePage.openAndWaitFor();
        assertEquals(TestSuitePage.FILTER_BROKEN, suitePage.getCurrentFilter());
        assertFalse(suitePage.isTestCaseVisible(SUCCESSFUL_TEST));
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

    private TestSuitePage buildAndCheckTestSummary(boolean expectedSuccess, TestResultSummary expectedSummary) throws Exception
    {
        long buildId = xmlRpcHelper.runBuild(random);
        Hashtable<String, Object> build = xmlRpcHelper.getBuild(random, (int) buildId);
        boolean success = (Boolean) build.get("succeeded");
        assertEquals(expectedSuccess, success);

        browser.loginAsAdmin();
        BuildTestsPage testsPage = browser.openAndWaitFor(BuildTestsPage.class, random, buildId);

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
        Vector<Hashtable<String, Object>> stages = (Vector<Hashtable<String, Object>>) build.get("stages");
        checkApiTestSummary(expectedSummary, stages.get(0));

        return suitePage;
    }

    private void checkApiTestSummary(TestResultSummary expectedSummary, Hashtable<String, Object> result)
    {
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

        browser.loginAsAdmin();

        // Test we can drill all the way down then back up again.
        BuildTestsPage testsPage = browser.openAndWaitFor(BuildTestsPage.class, random, buildId);

        assertTrue(testsPage.getTestSummary().getTotal() > 0);
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

        browser.loginAsAdmin();

        BuildTestsPage testsPage = browser.openAndWaitFor(BuildTestsPage.class, random, buildId);
        TestResultSummary expectedSummary = new TestResultSummary(0, 0, 3, 0, 583);

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

    public void testTestResultsShownForCompletedStagesBeforeBuildCompletes() throws Exception
    {
        final String SECOND_STAGE = "second";

        File tempDir = createTempDirectory();
        try
        {
            // Set up a project with two recipes and stages:
            //   - default: runs a recipe that completes quickly, with tests
            //   - second:  runs the default waiting recipe, which we can
            //              release when we choose (also with tests)
            final WaitProject project = projects.createWaitAntProject(tempDir, random);

            DirectoryArtifactConfiguration reportsArtifact = new DirectoryArtifactConfiguration("test reports", "reports/xml");
            PostProcessorConfiguration junitProcessor = project.getConfig().getPostProcessors().get(JUNIT_PROCESSOR);
            reportsArtifact.addPostProcessor(junitProcessor);
            
            SleepCommandConfiguration command = new SleepCommandConfiguration("noop");
            command.addArtifact(reportsArtifact);

            RecipeConfiguration completeRecipe = new RecipeConfiguration("complete");
            completeRecipe.addCommand(command);

            MultiRecipeTypeConfiguration type = (MultiRecipeTypeConfiguration) project.getConfig().getType();
            type.addRecipe(completeRecipe);
            
            project.getDefaultStage().setRecipe(completeRecipe.getName());

            project.addStage(SECOND_STAGE);
            reportsArtifact = project.addDirArtifact("test reports", "reports/xml");
            reportsArtifact.addPostProcessor(junitProcessor);
            
            configurationHelper.insertProject(project.getConfig(), false);
            
            xmlRpcHelper.waitForProjectToInitialise(project.getName());
            xmlRpcHelper.triggerBuild(project.getName());
            xmlRpcHelper.waitForBuildInProgress(project.getName(), 1);
            
            final String defaultStageName = project.getDefaultStage().getName();
            AcceptanceTestUtils.waitForCondition(new Condition()
            {
                public boolean satisfied()
                {
                    try
                    {
                        Hashtable<String, Object> build = xmlRpcHelper.getBuild(project.getName(), 1);
                        return stageIsComplete(build, defaultStageName);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }, BUILD_TIMEOUT, "stage to complete");

            browser.loginAsAdmin();
            BuildTestsPage testsPage = browser.openAndWaitFor(BuildTestsPage.class, project.getName(), 1L);
            assertTrue(testsPage.isStagePresent(defaultStageName));
            assertTrue(testsPage.isStageComplete(defaultStageName));
            assertTrue(testsPage.isStageLinkPresent(defaultStageName));
            assertTrue(testsPage.hasFailedTestsForStage(defaultStageName));

            assertTrue(testsPage.isStagePresent(SECOND_STAGE));
            assertFalse(testsPage.isStageComplete(SECOND_STAGE));
            assertFalse(testsPage.isStageLinkPresent(SECOND_STAGE));
            assertFalse(testsPage.hasFailedTestsForStage(SECOND_STAGE));
                
            project.releaseBuild();
            xmlRpcHelper.waitForBuildToComplete(project.getName(), 1);
            
            testsPage.openAndWaitFor();
            assertTrue(testsPage.isStagePresent(defaultStageName));
            assertTrue(testsPage.isStageComplete(defaultStageName));
            assertTrue(testsPage.hasFailedTestsForStage(defaultStageName));

            assertTrue(testsPage.isStagePresent(SECOND_STAGE));
            assertTrue(testsPage.isStageComplete(SECOND_STAGE));
            assertTrue(testsPage.isStageLinkPresent(SECOND_STAGE));
            assertTrue(testsPage.hasFailedTestsForStage(SECOND_STAGE));
        }
        finally
        {
            removeDirectory(tempDir);
        }
    }

    private boolean stageIsComplete(Hashtable<String, Object> build, String stageName)
    {
        Vector<Hashtable<String, Object>> stages = (Vector<Hashtable<String, Object>>) build.get("stages");
        for (Hashtable<String, Object> stage: stages)
        {
            if (stageName.equals(stage.get("name")))
            {
                return (Boolean) stage.get("completed");
            }
        }
        
        return false;
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

        setSchemeToIncrementalUpdate(random);

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
        String projectPath = createProjectWithTwoAntStages(random, "nosuchbuildfile.xml", "another-stage");
        setTerminateStageOnFailure(projectPath);

        long buildId = xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

        browser.loginAsAdmin();
        browser.openAndWaitFor(BuildSummaryPage.class, random, buildId);
        assertTextPresent(String.format("Build terminated due to failure of stage '%s'", DEFAULT_STAGE));

        Hashtable<String, Object> build = xmlRpcHelper.getBuild(random, (int)buildId);
        assertEquals(ResultState.TERMINATED, ResultState.fromPrettyString((String) build.get("status")));

        // assert stage status
        Vector<Hashtable<String, Object>> stages = (Vector<Hashtable<String, Object>>) build.get("stages");
        assertStageState(ResultState.FAILURE, ProjectConfigurationWizard.DEFAULT_STAGE, stages);
        assertStageState(ResultState.TERMINATED, "another-stage", stages);
    }

    public void testTerminateOnStageFailureStageSucceeds() throws Exception
    {
        String projectPath = createProjectWithTwoAntStages(random, "build.xml", "another-stage");
        setTerminateStageOnFailure(projectPath);

        long buildId = xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

        browser.loginAsAdmin();
        browser.openAndWaitFor(BuildSummaryPage.class, random, buildId);
        assertTextNotPresent("terminated");

        Hashtable<String, Object> build = xmlRpcHelper.getBuild(random, (int)buildId);
        assertEquals(ResultState.SUCCESS, ResultState.fromPrettyString((String) build.get("status")));

        Vector<Hashtable<String, Object>> stages = (Vector<Hashtable<String, Object>>) build.get("stages");
        assertStageState(ResultState.SUCCESS, ProjectConfigurationWizard.DEFAULT_STAGE, stages);
        assertStageState(ResultState.SUCCESS, "another-stage", stages);
    }

    public void testTerminateOnStageFailureLimit() throws Exception
    {
        String projectPath = createProjectWithTwoAntStages(random, "nosuchbuildfile.xml", "another-stage");
        String optionsPath = PathUtils.getPath(projectPath, Constants.Project.OPTIONS);
        Hashtable<String, Object> options = xmlRpcHelper.getConfig(optionsPath);
        options.put("stageFailureLimit", 1);
        xmlRpcHelper.saveConfig(optionsPath, options, false);

        long buildId = xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

        browser.loginAsAdmin();
        browser.openAndWaitFor(BuildSummaryPage.class, random, buildId);
        assertTextPresent("Build terminated due to the stage failure limit (1) being reached");

        Hashtable<String, Object> build = xmlRpcHelper.getBuild(random, (int)buildId);
        assertEquals(ResultState.TERMINATED, ResultState.fromPrettyString((String) build.get("status")));

        Vector<Hashtable<String, Object>> stages = (Vector<Hashtable<String, Object>>) build.get("stages");
        assertStageState(ResultState.FAILURE, ProjectConfigurationWizard.DEFAULT_STAGE, stages);
        assertStageState(ResultState.TERMINATED, "another-stage", stages);
    }

    private void assertStageState(ResultState expectedState, final String stageName, Vector<Hashtable<String, Object>> stages)
    {
        Hashtable<String, Object> stage = CollectionUtils.find(stages, new Predicate<Hashtable<String, Object>>()
        {
            public boolean satisfied(Hashtable<String, Object> detail)
            {
                return detail.get("name").equals(stageName);
            }
        });
        assertNotNull(stage);
        assertEquals(expectedState, ResultState.fromPrettyString((String) stage.get("status")));
    }

    public void testDeadlockUpdatingConfigAfterTrigger() throws Exception
    {
        addProject(random, true);
        String propertyPath = xmlRpcHelper.insertProjectProperty(random, "prop", "val", false, true, false);
        Hashtable<String, Object> property = xmlRpcHelper.getConfig(propertyPath);
        
        // Run through a few times to make the deadlock more likely to happen.
        // This is a balance between the speed of the test and likelihood of
        // triggering the problem.
        for (int i = 0; i < 3; i++)
        {
            Vector<String> ids = xmlRpcHelper.triggerBuild(random, new Hashtable<String, Object>());
            property.put("value", Integer.toString(i));
            xmlRpcHelper.saveConfig(propertyPath, property, false);
            Hashtable<String, Object> buildRequest = xmlRpcHelper.waitForBuildRequestToBeActivated(ids.get(0), BUILD_TIMEOUT);
            xmlRpcHelper.waitForBuildToComplete(random, Integer.parseInt((String) buildRequest.get("buildId")));
        }
    }
    
    public void testPulseFileTab() throws Exception
    {
        addProject(random, true);
        long buildId = xmlRpcHelper.runBuild(random);

        browser.loginAsAdmin();
        BuildFilePage buildFilePage = browser.openAndWaitFor(BuildFilePage.class, random, buildId);
        assertTrue(buildFilePage.isDownloadLinkPresent());
        assertTrue(buildFilePage.isHighlightedFilePresent());
        assertTextPresent("default-recipe=\"default\"");
    }
    
    public void testCleanBuild() throws Exception
    {
        String projectName = random + "-project";
        String agentName = random + "-agent";

        Hashtable<String, Object> agent = xmlRpcHelper.createEmptyConfig("zutubi.agentConfig");
        agent.put("name", agentName);
        agent.put("remote", false);

        String agentPath = xmlRpcHelper.insertTemplatedConfig(MasterConfigurationRegistry.AGENTS_SCOPE + "/" + AgentManager.GLOBAL_AGENT_NAME, agent, false);

        try
        {
            String projectPath = xmlRpcHelper.insertSimpleProject(projectName);
            assignStageToAgent(projectName, DEFAULT_STAGE, agentName);
            setSchemeToIncrementalUpdate(projectName);

            xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);
            xmlRpcHelper.doConfigAction(projectPath, ProjectConfigurationActions.ACTION_MARK_CLEAN);
            long buildId = xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);

            browser.loginAsAdmin();

            browser.openAndWaitFor(CommandArtifactPage.class, projectName, buildId, DEFAULT_STAGE, BootstrapCommandConfiguration.COMMAND_NAME, BootstrapCommand.OUTPUT_NAME + "/" + BootstrapCommand.FILES_FILE);
            assertTextPresent("build.xml");

            AgentStatusPage statusPage = browser.openAndWaitFor(AgentStatusPage.class, agentName);
            AgentStatusPage.SynchronisationMessage synchronisationMessage = statusPage.getSynchronisationMessage(0);
            assertEquals("clean up stage '" + DEFAULT_STAGE + "' of project '" + projectName + "'", synchronisationMessage.description);
        }
        finally
        {
            xmlRpcHelper.deleteConfig(agentPath);
        }
    }
    

    public void testCleanBuildTwoStagesOneAgent() throws Exception
    {
        final String SECOND_STAGE_NAME = "stage-left";

        String projectName = random + "-project";
        String agentName = random + "-agent";

        Hashtable<String, Object> agent = xmlRpcHelper.createEmptyConfig("zutubi.agentConfig");
        agent.put("name", agentName);
        agent.put("remote", false);

        String agentPath = xmlRpcHelper.insertTemplatedConfig(MasterConfigurationRegistry.AGENTS_SCOPE + "/" + AgentManager.GLOBAL_AGENT_NAME, agent, false);

        try
        {
            String projectPath = createProjectWithTwoAntStages(projectName, "build.xml", SECOND_STAGE_NAME);
            setSchemeToIncrementalUpdate(projectName);

            // First build establishes directories for both stages on master agent.
            xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);

            // For the next build, clean, but send the second stage to a
            // different agent.
            assignStageToAgent(projectName, SECOND_STAGE_NAME, agentName);
            xmlRpcHelper.doConfigAction(projectPath, ProjectConfigurationActions.ACTION_MARK_CLEAN);
            xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);

            // Finally, set the second stage back to the original agent.  Check
            // that it does not end up with the directory from the first build.
            // This is verified by ensuring the build.xml file is checked out.
            assignStageToAgent(projectName, SECOND_STAGE_NAME, AgentManager.MASTER_AGENT_NAME);
            long buildId = xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);

            browser.loginAsAdmin();

            browser.openAndWaitFor(CommandArtifactPage.class, projectName, buildId, SECOND_STAGE_NAME, BootstrapCommandConfiguration.COMMAND_NAME, BootstrapCommand.OUTPUT_NAME + "/" + BootstrapCommand.FILES_FILE);
            assertTextPresent("build.xml");
        }
        finally
        {
            xmlRpcHelper.deleteConfig(agentPath);
        }
    }
    
    private void setSchemeToIncrementalUpdate(String projectName) throws Exception
    {
        String svnPath = PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, projectName, Project.SCM);
        Hashtable<String, Object> svn = xmlRpcHelper.getConfig(svnPath);
        svn.put(Project.Scm.CHECKOUT_SCHEME, CheckoutScheme.INCREMENTAL_UPDATE.toString());
        xmlRpcHelper.saveConfig(svnPath, svn, false);
        xmlRpcHelper.waitForProjectToInitialise(projectName);
    }

    private void assignStageToAgent(String projectName, String stageName, String agentName) throws Exception
    {
        String stagePath = getPath(PROJECTS_SCOPE, projectName, "stages", stageName);
        Hashtable<String, Object> defaultStage = xmlRpcHelper.getConfig(stagePath);
        defaultStage.put("agent", getPath(AGENTS_SCOPE, agentName));
        xmlRpcHelper.saveConfig(stagePath, defaultStage, false);
    }
    
    public void testPulseFilePropertiesAddedToEnvironment() throws Exception
    {
        String projectPath = xmlRpcHelper.insertProject(random, GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(VERSIONED_REPOSITORY), xmlRpcHelper.createVersionedConfig("pulse/pulse.xml"));
        String stagePath = getPath(projectPath, Constants.Project.STAGES, DEFAULT_STAGE);
        Hashtable<String, Object> defaultStage = xmlRpcHelper.getConfig(stagePath);
        defaultStage.put(Project.Stage.RECIPE, "properties");
        xmlRpcHelper.saveConfig(stagePath, defaultStage, false);

        xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

        browser.loginAsAdmin();
        EnvironmentArtifactPage envPage = browser.openAndWaitFor(EnvironmentArtifactPage.class, random, 1L, DEFAULT_STAGE, "c1");
        assertTrue(envPage.isPulsePropertyPresentWithValue("outerp", "original-value"));
        assertTrue(envPage.isPulsePropertyPresentWithValue("p1", "original-value"));
        assertFalse(envPage.isPulsePropertyPresent("p2"));
        
        envPage = browser.openAndWaitFor(EnvironmentArtifactPage.class, random, 1L, DEFAULT_STAGE, "c2");
        assertTrue(envPage.isPulsePropertyPresentWithValue("outerp", "new-value"));
        assertTrue(envPage.isPulsePropertyPresentWithValue("p1", "new-value"));
        assertTrue(envPage.isPulsePropertyPresentWithValue("p2", "value"));
    }

    public void testTriggerBuildWithNewProperties() throws Exception
    {
        AntProjectHelper project = projects.createTrivialAntProject(random);
        project.addProperty("env", "value").setAddToEnvironment(true);
        project.addProperty("notenv", "value").setAddToEnvironment(false);
        configurationHelper.insertProject(project.getConfig(), false);

        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("new", "newvalue");
        Hashtable<String, Object> options = new Hashtable<String, Object>();
        options.put("properties", properties);
        xmlRpcHelper.triggerBuild(random, options);
        xmlRpcHelper.waitForBuildToComplete(random, 1);

        browser.loginAsAdmin();
        EnvironmentArtifactPage envPage = browser.openAndWaitFor(EnvironmentArtifactPage.class, random, 1L, DEFAULT_STAGE, DEFAULT_COMMAND);

        assertTrue(envPage.isPropertyPresentWithValue("env", "value"));
        assertFalse(envPage.isPropertyPresent("notenv"));
        assertFalse(envPage.isPropertyPresent("new"));
        assertTrue(envPage.isPulsePropertyPresentWithValue("env", "value"));
        assertTrue(envPage.isPulsePropertyPresentWithValue("notenv", "value"));
        assertTrue(envPage.isPulsePropertyPresentWithValue("new", "newvalue"));
    }

    public void testTriggerBuildWithExistingProperties() throws Exception
    {
        AntProjectHelper project = projects.createTrivialAntProject(random);
        project.addProperty("env", "value").setAddToEnvironment(true);
        project.addProperty("notenv", "value").setAddToEnvironment(false);
        configurationHelper.insertProject(project.getConfig(), false);

        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("env", "newvalue");
        Hashtable<String, Object> options = new Hashtable<String, Object>();
        options.put("properties", properties);
        xmlRpcHelper.triggerBuild(random, options);
        xmlRpcHelper.waitForBuildToComplete(random, 1);

        browser.loginAsAdmin();
        EnvironmentArtifactPage envPage = browser.openAndWaitFor(EnvironmentArtifactPage.class, random, 1L, DEFAULT_STAGE, DEFAULT_COMMAND);
        assertTrue(envPage.isPropertyPresentWithValue("env", "newvalue"));
        assertFalse(envPage.isPropertyPresent("notenv"));
        assertTrue(envPage.isPulsePropertyPresentWithValue("env", "newvalue"));
        assertTrue(envPage.isPulsePropertyPresentWithValue("notenv", "value"));
    }
    
    private String createProjectWithTwoAntStages(String projectName, String buildFile, String secondStageName) throws Exception
    {
        String projectPath = xmlRpcHelper.insertSingleCommandProject(projectName, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(TRIVIAL_ANT_REPOSITORY), xmlRpcHelper.getAntConfig(buildFile));
        Hashtable<String, String> keys = new Hashtable<String, String>();
        keys.put(DEFAULT_STAGE, secondStageName);
        xmlRpcHelper.cloneConfig(PathUtils.getPath(projectPath, Project.STAGES), keys);
        return projectPath;
    }

    private void setTerminateStageOnFailure(String projectPath) throws Exception
    {
        String defaultStagePath = PathUtils.getPath(projectPath, Project.STAGES, DEFAULT_STAGE);
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
        goToArtifact(projectName, buildId, ExecutableCommand.ENV_ARTIFACT_NAME, ExecutableCommand.ENV_FILENAME);
    }

    private void goToArtifact(String projectName, long buildId, String artifact, String file)
    {
        BuildArtifactsPage artifactsPage = browser.openAndWaitFor(BuildArtifactsPage.class, projectName, buildId);
        artifactsPage.setFilterAndWait("");
        artifactsPage.clickArtifactFileDownload(artifact, file);
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
