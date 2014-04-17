package com.zutubi.pulse.acceptance;

import com.google.common.base.Predicate;
import com.google.common.io.Files;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.acceptance.forms.admin.BuildStageForm;
import com.zutubi.pulse.acceptance.forms.admin.TriggerBuildForm;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.agents.AgentStatusPage;
import com.zutubi.pulse.acceptance.pages.agents.SynchronisationMessageTable;
import com.zutubi.pulse.acceptance.pages.browse.*;
import com.zutubi.pulse.acceptance.pages.dashboard.DashboardPage;
import com.zutubi.pulse.acceptance.utils.AntProjectHelper;
import com.zutubi.pulse.acceptance.utils.Repository;
import com.zutubi.pulse.acceptance.utils.TriviAntProject;
import com.zutubi.pulse.acceptance.utils.WaitProject;
import com.zutubi.pulse.acceptance.utils.workspace.SubversionWorkspace;
import com.zutubi.pulse.core.BootstrapCommand;
import com.zutubi.pulse.core.BootstrapCommandConfiguration;
import com.zutubi.pulse.core.RegexPatternConfiguration;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.api.DirectoryArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.FileArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.OutputProducingCommandSupport;
import com.zutubi.pulse.core.commands.core.*;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.FieldScope;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.TestResultSummary;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceVersionConfiguration;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.*;
import com.zutubi.pulse.master.tove.config.project.changeviewer.FisheyeConfiguration;
import com.zutubi.pulse.master.tove.config.project.commit.LinkTransformerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.BuildCompletedTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.CustomTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.MultiRecipeTypeConfiguration;
import com.zutubi.pulse.master.xwork.actions.project.ViewChangesAction;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.Condition;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.SecurityUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import org.apache.commons.httpclient.Header;
import org.tmatesoft.svn.core.SVNException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import static com.google.common.collect.Iterables.find;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

/**
 * An acceptance test that adds a very simple project and runs a build as a
 * sanity test.
 */
public class BuildAcceptanceTest extends AcceptanceTestBase
{
    private static final Logger LOG = Logger.getLogger(BuildAcceptanceTest.class);

    private static final int BUILD_TIMEOUT = 120000;

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

    protected void setUp() throws Exception
    {
        super.setUp();
        rpcClient.loginAsAdmin();

        Vector<String> agents = rpcClient.RemoteApi.getConfigListing(AGENTS_SCOPE);
        for (String agent : agents)
        {
            if (!agent.equals(GLOBAL_AGENT_NAME) && !agent.equals(MASTER_AGENT_NAME))
            {
                rpcClient.RemoteApi.deleteConfig(PathUtils.getPath(AGENTS_SCOPE, agent));
            }
        }

        repository = new Repository();
    }

    protected void tearDown() throws Exception
    {
        rpcClient.cancelIncompleteBuilds();
        rpcClient.logout();
        super.tearDown();
    }

    public void testSimpleBuild() throws Exception
    {
        getBrowser().loginAsAdmin();
        rpcClient.RemoteApi.insertSimpleProject(random);

        triggerSuccessfulBuild(random);

        // Check changes page handling of no changes.
        BuildChangesPage changesPage = getBrowser().openAndWaitFor(BuildChangesPage.class, random, 1L);
        assertFalse(changesPage.hasChanges());
        assertFalse(changesPage.isCompareToPopDownPresent());
        getBrowser().waitForTextPresent(Messages.getInstance(ViewChangesAction.class).format("changes.none"));

        // Check some properties
        EnvironmentArtifactPage envPage = getBrowser().openAndWaitFor(EnvironmentArtifactPage.class, random, 1L, "default", "build");
        assertTrue(envPage.isPulsePropertyPresentWithValue(BuildProperties.PROPERTY_LOCAL_BUILD, Boolean.toString(false)));
        assertTrue(envPage.isPulsePropertyPresentWithValue(BuildProperties.PROPERTY_PERSONAL_BUILD, Boolean.toString(false)));
        assertTrue(envPage.isPulsePropertyPresentWithValue(BuildProperties.PROPERTY_OWNER, random));
        assertTrue(envPage.isPulsePropertyPresentWithValue(BuildProperties.PROPERTY_RECIPE_STATUS, "success"));
    }
    
    public void testNoChangesBetweenBuilds() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random);
        rpcClient.RemoteApi.runBuild(random, BUILD_TIMEOUT);
        rpcClient.RemoteApi.runBuild(random, BUILD_TIMEOUT);

        getBrowser().loginAsAdmin();

        BuildChangesPage changesPage = getBrowser().openAndWaitFor(BuildChangesPage.class, random, 2L);
        assertFalse(changesPage.hasChanges());
        // Unlike where there are no previous builds, in this case we expect
        // the compare-to pop down.
        assertTrue(changesPage.isCompareToPopDownPresent());
        getBrowser().waitForTextPresent(Messages.getInstance(ViewChangesAction.class).format("changes.none"));
    }

    public void testChangesBetweenBuilds() throws Exception
    {
        // Run an initial build
        TriviAntProject project = projectConfigurations.createTrivialAntProject(random);
        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);
        rpcClient.RemoteApi.runBuild(random);

        // Commit a change to the repository.  Note monitoring the SCM is
        // disabled for these projects, so no chance of a build being started
        // by this change.
        String revisionString = project.editAndCommitBuildFile(CHANGE_COMMENT, random);
        long buildNumber = rpcClient.RemoteApi.runBuild(random);

        // Check the changes tab.
        getBrowser().loginAsAdmin();
        BuildChangesPage changesPage = getBrowser().openAndWaitFor(BuildChangesPage.class, random, buildNumber);
        getBrowser().waitForTextPresent(changesPage.formatChangesSince(buildNumber));

        List<Changelist> changelists = changesPage.getChangelists();
        assertEquals(1, changelists.size());
        assertBuildFileChangelist(changelists.get(0), revisionString);

        // Pop down the compare to box to make sure it appears.
        changesPage.clickCompareToPopDown();

        // Check the changelist view too.
        List<Long> changeIds = changesPage.getChangeIds();
        assertEquals(1, changeIds.size());
        ViewChangelistPage changelistPage = getBrowser().openAndWaitFor(ViewChangelistPage.class, random, buildNumber, changeIds.get(0), revisionString);
        assertBuildFileChangelist(changelistPage.getChangelist(), revisionString);
        
        // Check appearance of change on user's dashboard.
        DashboardPage dashboardPage = getBrowser().openAndWaitFor(DashboardPage.class);
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
        rpcClient.RemoteApi.insertSingleCommandProject(random, GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getSubversionConfig(subversionUrl), rpcClient.RemoteApi.getAntConfig());
        rpcClient.RemoteApi.runBuild(random);

        // Commit a large change to the repository.
        String revisionString;
        try
        {
            workspace.doCheckout(subversionUrl);

            File[] files = new File[CHANGE_COUNT];
            for (int i = 0; i < CHANGE_COUNT; i++)
            {
                files[i] = new File(wcDir, String.format("file-%03d.txt", i));
                Files.write("content", files[i], Charset.defaultCharset());
            }
            
            workspace.doAdd(files);
            revisionString = workspace.doCommit(CHANGE_COMMENT, files);
        }
        finally
        {
            IOUtils.close(workspace);
        }
        
        long buildNumber = rpcClient.RemoteApi.runBuild(random);

        getBrowser().loginAsAdmin();
        BuildChangesPage changesPage = getBrowser().openAndWaitFor(BuildChangesPage.class, random, buildNumber);
        getBrowser().waitForTextPresent(String.format("%d more files", CHANGE_COUNT - 5));

        List<Long> changeIds = changesPage.getChangeIds();
        assertEquals(1, changeIds.size());
        ViewChangelistPage changelistPage = getBrowser().openAndWaitFor(ViewChangelistPage.class, random, buildNumber, changeIds.get(0), revisionString);
        Changelist changelist = changelistPage.getChangelist();
        assertEquals(100, changelist.getChanges().size());
        assertTrue(changelistPage.isNextLinkPresent());
        assertFalse(changelistPage.isPreviousLinkPresent());

        changelistPage.clickNext();
        changelistPage.waitFor();
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
        
        TriviAntProject viewable = projectConfigurations.createTrivialAntProject(viewableProject);
        CONFIGURATION_HELPER.insertProject(viewable.getConfig(), false);
        TriviAntProject unviewable = projectConfigurations.createTrivialAntProject(unviewableProject);
        CONFIGURATION_HELPER.insertProject(unviewable.getConfig(), false);
        rpcClient.RemoteApi.insertTrivialUser(regularUser);
        
        // Remove permissions that allow normal users to view the invisible
        // project (so only admin can see it).
        rpcClient.RemoteApi.deleteAllConfigs(PathUtils.getPath(unviewable.getConfig().getConfigurationPath(), Constants.Project.PERMISSIONS, PathUtils.WILDCARD_ANY_ELEMENT));
        
        rpcClient.RemoteApi.runBuild(viewableProject);
        rpcClient.RemoteApi.runBuild(unviewableProject);

        // Commit a change to the repository.  Note monitoring the SCM is
        // disabled for these projects, so no chance of a build being started
        // by this change.
        String revisionString = viewable.editAndCommitBuildFile(CHANGE_COMMENT, random);
        
        long viewableBuildNumber = rpcClient.RemoteApi.runBuild(viewableProject);
        long unviewableBuildNumber = rpcClient.RemoteApi.runBuild(unviewableProject);

        // Check the changes tab.
        getBrowser().loginAsAdmin();
        BuildChangesPage changesPage = getBrowser().openAndWaitFor(BuildChangesPage.class, viewableProject, viewableBuildNumber);
        List<Long> changeIds = changesPage.getChangeIds();
        assertEquals(1, changeIds.size());
        ViewChangelistPage changelistPage = getBrowser().openAndWaitFor(ViewChangelistPage.class, viewableProject, viewableBuildNumber, changeIds.get(0), revisionString);
        List<BuildInfo> builds = changelistPage.getBuilds();
        assertEquals(2, builds.size());
        assertThat(builds, hasItem(new BuildInfo(viewableProject, (int) viewableBuildNumber, ResultState.SUCCESS, null)));
        assertThat(builds, hasItem(new BuildInfo(unviewableProject, (int) unviewableBuildNumber, ResultState.SUCCESS, null)));

        // Dashboard project changes table should show popup for multiple
        // builds.
        DashboardPage dashboardPage = getBrowser().openAndWaitFor(DashboardPage.class);
        DashboardPage.ProjectChange change = dashboardPage.getProjectChange(0);
        assertEquals(ResultState.SUCCESS, change.status);
        assertEquals("2 builds", change.builds);

        getBrowser().logout();
        getBrowser().loginAndWait(regularUser, "");
        
        // Regular user should only see the visible project.
        changelistPage.openAndWaitFor();
        builds = changelistPage.getBuilds();
        assertEquals(1, builds.size());
        assertThat(builds, hasItem(new BuildInfo(viewableProject, (int) viewableBuildNumber, ResultState.SUCCESS, null)));
        
        // Check other pages that we can view where the changelist appears.
        getBrowser().openAndWaitFor(ProjectHomePage.class, viewableProject);

        dashboardPage = getBrowser().openAndWaitFor(DashboardPage.class);
        change = dashboardPage.getProjectChange(0);
        assertEquals(ResultState.SUCCESS, change.status);
        assertEquals(viewableProject + " :: build " + viewableBuildNumber, change.builds);
    }
    
    public void testChangeViewerLinks() throws Exception
    {
        final String FISHEYE_BASE = "http://fisheye";
        final String FISHEYE_PROJECT = "project";

        TriviAntProject project = projectConfigurations.createTrivialAntProject(random);
        FisheyeConfiguration changeViewer = new FisheyeConfiguration(FISHEYE_BASE, FISHEYE_PROJECT);
        project.getConfig().setChangeViewer(changeViewer);
        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);

        rpcClient.RemoteApi.runBuild(random);
        String revisionString = project.editAndCommitBuildFile(CHANGE_AUTHOR, random);
        long buildNumber = rpcClient.RemoteApi.runBuild(random);

        String changelistLink = FISHEYE_BASE + "/changelog/" + FISHEYE_PROJECT + "/?cs=" + revisionString;

        getBrowser().loginAsAdmin();
        BuildChangesPage changesPage = getBrowser().openAndWaitFor(BuildChangesPage.class, random, buildNumber);
        assertTrue(getBrowser().isLinkToPresent(changelistLink));

        getBrowser().openAndWaitFor(ViewChangelistPage.class, random, buildNumber, changesPage.getChangeIds().get(0), revisionString);

        String prefixPart = FISHEYE_BASE + "/browse/";
        String filePart = FISHEYE_PROJECT + "/accept/trunk/triviant/" + CHANGE_FILENAME;
        assertTrue(getBrowser().isLinkToPresent(changelistLink));
        assertTrue(getBrowser().isLinkToPresent(prefixPart + filePart + "?r=" + revisionString));
        assertTrue(getBrowser().isLinkToPresent(prefixPart + "~raw,r=" + revisionString + "/" + filePart));
        assertTrue(getBrowser().isLinkToPresent(prefixPart + filePart + "?r1=" + new Revision(revisionString).calculatePreviousNumericalRevision() + "&r2=" + revisionString));
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
        rpcClient.RemoteApi.insertSimpleProject(random);
        getBrowser().loginAsAdmin();

        String agentHandle;
        rpcClient.RemoteApi.ensureAgent(AGENT_NAME);
        agentHandle = rpcClient.RemoteApi.getConfigHandle("agents/" + AGENT_NAME);

        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, random, false);
        ProjectConfigPage configPage = hierarchyPage.clickConfigure();
        configPage.waitFor();
        ListPage stagesPage = configPage.clickCollection(ProjectConfigPage.BUILD_STAGES_BASE, ProjectConfigPage.BUILD_STAGES_DISPLAY);
        stagesPage.waitFor();
        stagesPage.clickView("default");

        BuildStageForm stageForm = getBrowser().createForm(BuildStageForm.class, true);
        stageForm.waitFor();

        stageForm.applyFormElements("", agentHandle);

        triggerSuccessfulBuild(random);

        Hashtable<String, Object> build = rpcClient.RemoteApi.getBuild(random, 1);
        @SuppressWarnings({"unchecked"})
        Vector<Hashtable<String, Object>> stages = (Vector<Hashtable<String, Object>>) build.get("stages");
        assertEquals(AGENT_NAME, stages.get(0).get("agent"));
    }
    
    public void testSummaryView() throws Exception
    {
        final String FEATURES_PROCESSOR = "features processor";

        String projectPath = rpcClient.RemoteApi.insertSingleCommandProject(random, GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getSubversionConfig(ALL_ANT_REPOSITORY), rpcClient.RemoteApi.getAntConfig());
        insertTestCapture(projectPath, JUNIT_PROCESSOR);

        insertFeaturesProcessor(random, FEATURES_PROCESSOR);
        String featuresArtifactPath = insertFileArtifact(projectPath, "features", "features.txt", FEATURES_PROCESSOR, false);
        
        Hashtable<String, Object> transformer = rpcClient.RemoteApi.createDefaultConfig(LinkTransformerConfiguration.class);
        transformer.put("name", "issues");
        transformer.put("expression", "CIB-[0-9]+");
        transformer.put("url", "http://jira.zutubi.com/$0");
        rpcClient.RemoteApi.insertConfig(getPath(projectPath, COMMIT_MESSAGE_TRANSFORMERS), transformer);

        // Run two builds, to generate changes between them.
        rpcClient.RemoteApi.runBuild(random, BUILD_TIMEOUT);
        editAndCommitFile(ALL_ANT_REPOSITORY, "expected-failures.txt", "CIB-123: fixed it", RandomUtils.insecureRandomString(10));
        editAndCommitFile(ALL_ANT_REPOSITORY, "expected-failures.txt", "CIB-123: fixed it harder", RandomUtils.insecureRandomString(10));
        editAndCommitFile(ALL_ANT_REPOSITORY, "expected-failures.txt", "CIB-200: higher number", RandomUtils.insecureRandomString(10));
        editAndCommitFile(ALL_ANT_REPOSITORY, "expected-failures.txt", "CIB-100: lower number", RandomUtils.insecureRandomString(10));
        long buildNumber = rpcClient.RemoteApi.runBuild(random);

        getBrowser().loginAsAdmin();
        BuildSummaryPage summaryPage = getBrowser().openAndWaitFor(BuildSummaryPage.class, random, buildNumber);
        assertEquals(ResultState.FAILURE, summaryPage.getBuildStatus());

        assertTrue(summaryPage.isErrorListPresent());
        assertTrue(summaryPage.isWarningListPresent());
        assertTrue(summaryPage.isTestFailuresTablePresent());
        
        assertTrue(summaryPage.isRelatedLinksTablePresent());
        assertEquals(3, summaryPage.getRelatedLinksCount());
        assertEquals("CIB-100", summaryPage.getRelatedLinkText(0));
        assertEquals("CIB-123", summaryPage.getRelatedLinkText(1));
        assertEquals("CIB-200", summaryPage.getRelatedLinkText(2));

        // No featured artifacts in this build, check, then mark one as
        // featured and do another build.
        assertFalse(summaryPage.isFeaturedArtifactsTablePresent());
        Hashtable<String, Object> artifact = rpcClient.RemoteApi.getConfig(featuresArtifactPath);
        artifact.put("featured", true);
        rpcClient.RemoteApi.saveConfig(featuresArtifactPath, artifact, false);
        buildNumber = rpcClient.RemoteApi.runBuild(random);

        summaryPage = getBrowser().openAndWaitFor(BuildSummaryPage.class, random, buildNumber);
        assertTrue(summaryPage.isFeaturedArtifactsTablePresent());
        assertEquals("stage :: " + DEFAULT_STAGE, summaryPage.getFeaturedArtifactsRow(0));
        assertEquals("features", summaryPage.getFeaturedArtifactsRow(1));
    }

    private String editAndCommitFile(String repository, String filename, String comment, String newContent) throws IOException, SVNException
    {
        File wcDir = FileSystemUtils.createTempDir(getName());
        SubversionWorkspace workspace = new SubversionWorkspace(wcDir, CHANGE_AUTHOR, CHANGE_AUTHOR);
        try
        {
            workspace.doCheckout(repository);
            return workspace.editAndCommitFile(filename, comment, newContent);
        }
        finally
        {
            IOUtils.close(workspace);
        }
    }

    /*
     FIXME: temporarily disabled to facilitate 2.3.0 release.
    public void testSummaryViewNoContentForRightPane() throws Exception
    {
        String projectName = random + "-project";

        rpcClient.RemoteApi.insertSimpleProject(projectName);
        rpcClient.RemoteApi.runBuild(projectName, BUILD_TIMEOUT);

        getBrowser().loginAsAdmin();
        final BuildSummaryPage summaryPage = getBrowser().openAndWaitFor(BuildSummaryPage.class, projectName, 1L);
        assertTrue(summaryPage.isRightPaneVisible());
        getBrowser().logout();

        GlobalConfiguration globalConfig = configurationHelper.getConfiguration(GlobalConfiguration.SCOPE_NAME, GlobalConfiguration.class);
        if (!globalConfig.isAnonymousAccessEnabled())
        {
            globalConfig.setAnonymousAccessEnabled(true);
            configurationHelper.update(globalConfig, false);
            getBrowser().newSession();
        }

        try
        {
            summaryPage.openAndWaitFor();
            TestUtils.waitForCondition(new Condition()
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
    */
    
    public void testDetailsView() throws Exception
    {
        final String UPSTREAM_ARTIFACT = "file";
        final String BUILD_FIELD_PROCESSOR = "build field processor";
        final String FEATURES_PROCESSOR = "features processor";

        String upstreamProjectName = random + "-upstream";
        Hashtable<String, Object> upstreamAntConfig = rpcClient.RemoteApi.getAntConfig();
        upstreamAntConfig.put(Project.AntCommand.ARGUMENTS, "-Dcreate.list=file.txt");
        upstreamAntConfig.put(Project.AntCommand.TARGETS, "create");
        String upstreamProjectPath = rpcClient.RemoteApi.insertSingleCommandProject(upstreamProjectName, GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getSubversionConfig(DEP_ANT_REPOSITORY), upstreamAntConfig);
        insertFileArtifact(upstreamProjectPath, UPSTREAM_ARTIFACT, "file.txt", null, true);
        rpcClient.RemoteApi.runBuild(upstreamProjectName, BUILD_TIMEOUT);
        
        String mainProjectName = random + "-main";
        String mainProjectPath = rpcClient.RemoteApi.insertSingleCommandProject(mainProjectName, GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getSubversionConfig(ALL_ANT_REPOSITORY), rpcClient.RemoteApi.getAntConfig());
        insertTestCapture(mainProjectPath, JUNIT_PROCESSOR);

        String mainProcessorsPath = getPath(mainProjectPath, POSTPROCESSORS);
        Hashtable<String, Object> buildFieldsProcessorConfig = rpcClient.RemoteApi.createDefaultConfig(CustomFieldsPostProcessorConfiguration.class);
        buildFieldsProcessorConfig.put(NAME, BUILD_FIELD_PROCESSOR);
        buildFieldsProcessorConfig.put("scope", FieldScope.BUILD.toString());
        rpcClient.RemoteApi.insertConfig(mainProcessorsPath, buildFieldsProcessorConfig);

        insertFeaturesProcessor(mainProjectName, FEATURES_PROCESSOR);
        
        insertFileArtifact(mainProjectPath, "custom fields", "build.properties", BUILD_FIELD_PROCESSOR, false);
        insertFileArtifact(mainProjectPath, "stage fields", "stage.properties", CUSTOM_FIELD_PROCESSOR, false);
        insertFileArtifact(mainProjectPath, "features", "features.txt", FEATURES_PROCESSOR, false);

        String mainDependenciesPath = getPath(mainProjectPath, DEPENDENCIES);
        Hashtable<String, Object> dependenciesConfig = rpcClient.RemoteApi.getConfig(mainDependenciesPath);
        dependenciesConfig.put("syncDestination", false);
        rpcClient.RemoteApi.saveConfig(mainDependenciesPath, dependenciesConfig, false);
        
        Hashtable<String, Object> dependencyConfig = rpcClient.RemoteApi.createDefaultConfig(DependencyConfiguration.class);
        dependencyConfig.put("project", upstreamProjectPath);
        rpcClient.RemoteApi.insertConfig(getPath(mainDependenciesPath, DEPENDENCIES), dependencyConfig);
        
        rpcClient.RemoteApi.runBuild(mainProjectName, BUILD_TIMEOUT);

        getBrowser().loginAsAdmin();
        BuildDetailsPage detailsPage = getBrowser().openAndWaitFor(BuildDetailsPage.class, mainProjectName, 1L);
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
        getBrowser().waitForTextPresent("error feature");
        getBrowser().waitForTextPresent("warning feature");
        getBrowser().waitForTextPresent("info feature");
        getBrowser().waitForTextPresent("context line 1");
        getBrowser().waitForTextPresent("info context line 2");
    }

    private void insertFeaturesProcessor(String projectName, String processorName) throws Exception
    {
        Hashtable<String, Object> featuresProcessorConfig = rpcClient.RemoteApi.createDefaultConfig(RegexPostProcessorConfiguration.class);
        featuresProcessorConfig.put(NAME, processorName);
        featuresProcessorConfig.put("leadingContext", 2);
        featuresProcessorConfig.put("trailingContext", 2);
        Vector<Hashtable<String, Object>> patterns = new Vector<Hashtable<String, Object>>(3);
        patterns.add(createRegexPattern(Feature.Level.ERROR, "^error:"));
        patterns.add(createRegexPattern(Feature.Level.WARNING, "^warning:"));
        patterns.add(createRegexPattern(Feature.Level.INFO, "^info:"));
        featuresProcessorConfig.put("patterns", patterns);
        rpcClient.RemoteApi.insertConfig(getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, projectName, POSTPROCESSORS), featuresProcessorConfig);
    }

    private Hashtable<String, Object> createRegexPattern(Feature.Level category, String expression)  throws Exception
    {
        Hashtable<String, Object> pattern = rpcClient.RemoteApi.createDefaultConfig(RegexPatternConfiguration.class);
        pattern.put("category", category.toString());
        pattern.put("expression", expression);
        return pattern;
    }

    private String insertFileArtifact(String projectPath, String name, String file, String processorName, boolean publish) throws Exception
    {
        Hashtable<String, Object> artifactConfig = rpcClient.RemoteApi.createDefaultConfig(FileArtifactConfiguration.class);
        artifactConfig.put(NAME, name);
        artifactConfig.put(FILE, file);
        if (processorName != null)
        {
            artifactConfig.put(POSTPROCESSORS, new Vector<String>(Arrays.asList(PathUtils.getPath(projectPath, POSTPROCESSORS, processorName))));
        }
        artifactConfig.put(PUBLISH, publish);
        return rpcClient.RemoteApi.insertConfig(getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE_NAME, COMMANDS, DEFAULT_COMMAND, ARTIFACTS), artifactConfig);
    }
    
    public void testDetailsViewInProgress() throws Exception
    {
        File tempDir = createTempDirectory();
        try
        {
            final WaitProject project = projectConfigurations.createWaitAntProject(random, tempDir, false);
            CONFIGURATION_HELPER.insertProject(project.getConfig(), false);
            
            rpcClient.RemoteApi.triggerBuild(project.getName());
            rpcClient.RemoteApi.waitForBuildInProgress(project.getName(), 1);
            
            getBrowser().loginAsAdmin();
            final BuildDetailsPage detailsPage = getBrowser().createPage(BuildDetailsPage.class, project.getName(), 1L);
            TestUtils.waitForCondition(new Condition()
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
            rpcClient.RemoteApi.waitForBuildToComplete(project.getName(), 1);

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
        getBrowser().loginAsAdmin();
        rpcClient.RemoteApi.ensureProject(random);

        rpcClient.RemoteApi.insertProjectProperty(random, "pname", "pvalue", true, false);

        triggerSuccessfulBuild(random);
        assertEnvironment(random, 1, "pname=pvalue", "PULSE_PNAME=pvalue", "PULSE_BUILD_NUMBER=1");
    }

    public void testImportedResources() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(MASTER_AGENT_NAME, resourceName);
        rpcClient.RemoteApi.insertConfig(getPath(resourcePath, "properties"), rpcClient.RemoteApi.createProperty("test-property", "test-value", false, false));

        String projectName = random + "-project";
        rpcClient.RemoteApi.ensureProject(projectName);
        rpcClient.RemoteApi.insertConfig(getPath(PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

        getBrowser().loginAsAdmin();
        triggerSuccessfulBuild(projectName);
        assertEnvironment(projectName, 1, "PULSE_TEST-PROPERTY=test-value");
    }

    public void testResourceRequirementsReferencingProperties() throws Exception
    {
        String projectResourceName = random + "-project-resource";
        String stageResourceName = random + "-stage-resource";
        addResource(MASTER_AGENT_NAME, projectResourceName, "1.0");
        addResource(MASTER_AGENT_NAME, stageResourceName, "2.0");

        String projectName = random + "-project";
        String projectPath = getPath(PROJECTS_SCOPE, projectName);
        String stagePath = getPath(projectPath, "stages", "default");
        rpcClient.RemoteApi.ensureProject(projectName);
        rpcClient.RemoteApi.insertConfig(getPath(projectPath, "requirements"), createRequiredResource(projectResourceName, "$(p.version)"));
        rpcClient.RemoteApi.insertConfig(getPath(stagePath, "requirements"), createRequiredResource(stageResourceName, "$(s.version)"));
        rpcClient.RemoteApi.insertProjectProperty(projectName, "p.version", "1.0");
        rpcClient.RemoteApi.insertProjectProperty(projectName, "s.version", "1.0");
        rpcClient.RemoteApi.insertConfig(getPath(stagePath, "properties"), rpcClient.RemoteApi.createProperty("s.version", "2.0"));
        
        getBrowser().loginAsAdmin();
        triggerSuccessfulBuild(projectName);
    }

    public void testScmReferencesProjectProperty() throws Exception
    {
        rpcClient.RemoteApi.insertSingleCommandProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false,
                rpcClient.RemoteApi.getSubversionConfig("svn://localhost:3088/accept/$(branch)/triviant"),
                rpcClient.RemoteApi.getAntConfig());
        rpcClient.RemoteApi.insertProjectProperty(random, "branch", "trunk");
        getBrowser().loginAsAdmin();
        triggerSuccessfulBuild(random);
        assertEnvironment(random, 1, "PULSE_SVN_URL=svn://localhost:3088/accept/trunk/triviant");
    }

    public void testProjectPropertyReferencesResourceProperty() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(MASTER_AGENT_NAME, resourceName);
        rpcClient.RemoteApi.insertConfig(getPath(resourcePath, "properties"), rpcClient.RemoteApi.createProperty("rp", "rv", false, false));

        String projectName = random + "-project";
        String projectPath = getPath(PROJECTS_SCOPE, projectName);
        rpcClient.RemoteApi.ensureProject(projectName);
        rpcClient.RemoteApi.insertConfig(getPath(projectPath, "requirements"), createRequiredResource(resourceName, null));
        rpcClient.RemoteApi.insertConfig(getPath(projectPath, "properties"), rpcClient.RemoteApi.createProperty("pp", "ref $(rp)", true, false));

        getBrowser().loginAsAdmin();
        triggerSuccessfulBuild(projectName);
        assertEnvironment(projectName, 1, "pp=ref rv");
    }

    public void testResourcePropertyReferencesEarlierProperty() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(MASTER_AGENT_NAME, resourceName);
        String propertiesPath = getPath(resourcePath, "properties");
        rpcClient.RemoteApi.insertConfig(propertiesPath, rpcClient.RemoteApi.createProperty("referee", "ee", false, false));
        rpcClient.RemoteApi.insertConfig(propertiesPath, rpcClient.RemoteApi.createProperty("referer", "ref $(referee)", true, false));

        String projectName = random + "-project";
        rpcClient.RemoteApi.ensureProject(projectName);
        rpcClient.RemoteApi.insertConfig(getPath(PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

        getBrowser().loginAsAdmin();
        triggerSuccessfulBuild(projectName);
        assertEnvironment(projectName, 1, "referer=ref ee");
    }

    public void testProjectPropertyReferencesAgentName() throws Exception
    {
        String projectName = random + "-project";
        rpcClient.RemoteApi.ensureProject(projectName);
        assignStageToAgent(projectName, DEFAULT_STAGE, MASTER_AGENT_NAME);
        rpcClient.RemoteApi.insertProjectProperty(projectName, "pp", "ref $(agent)", true, false);

        getBrowser().loginAsAdmin();
        triggerSuccessfulBuild(projectName);
        assertEnvironment(projectName, 1, "pp=ref " + MASTER_AGENT_NAME);
    }

    public void testResourcePropertyReferencesAgentName() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(MASTER_AGENT_NAME, resourceName);
        rpcClient.RemoteApi.insertConfig(getPath(resourcePath, "properties"), rpcClient.RemoteApi.createProperty("rp", "ref $(agent)", true, false));

        String projectName = random + "-project";
        rpcClient.RemoteApi.ensureProject(projectName);
        rpcClient.RemoteApi.insertConfig(getPath(PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

        getBrowser().loginAsAdmin();
        triggerSuccessfulBuild(projectName);
        assertEnvironment(projectName, 1, "rp=ref " + MASTER_AGENT_NAME);
    }

    public void testSelfReferencingProperty() throws Exception
    {
        // CIB-3090.
        String projectName = random + "-project";
        rpcClient.RemoteApi.ensureProject(projectName);
        rpcClient.RemoteApi.insertProjectProperty(projectName, "prop", "$(prop):project", false, false);
        rpcClient.RemoteApi.insertOrUpdateStageProperty(projectName, "default", "prop", "$(prop):stage");

        getBrowser().loginAsAdmin();

        // Using a manual trigger with prompt checks that the properties coming from the prompt
        // behave as expected.
        ProjectHomePage home = getBrowser().openAndWaitFor(ProjectHomePage.class, projectName);
        home.triggerBuild();

        TriggerBuildForm form = getBrowser().createForm(TriggerBuildForm.class);
        form.addProperty("prop");
        form.waitFor();
        form.triggerFormElements();

        waitForBuildOnProjectHomePage(projectName);
        assertEnvironment(projectName, 1, "PULSE_PROP=$(prop):project:stage");
    }

    public void testSuppressedProperty() throws Exception
    {
        String projectName = random + "-project";
        rpcClient.RemoteApi.ensureProject(projectName);
        assignStageToAgent(projectName, DEFAULT_STAGE, MASTER_AGENT_NAME);
        String suppressedName = "PULSE_TEST_SUPPRESSED";
        String suppressedValue = random + "-suppress";
        rpcClient.RemoteApi.insertProjectProperty(projectName, suppressedName, suppressedValue, true, false);

        getBrowser().loginAsAdmin();
        triggerSuccessfulBuild(projectName);
        goToEnv(projectName, 1);
        getBrowser().waitForTextPresent(suppressedName);
        assertFalse(getBrowser().isTextPresent(suppressedValue));
    }

    public void testScmPropertiesAvailableInPulseFile() throws Exception
    {
        Hashtable<String, Object> type = rpcClient.RemoteApi.createEmptyConfig(CustomTypeConfiguration.class);
        type.put("pulseFileString", "<?xml version=\"1.0\"?>\n" +
                "<project default-recipe=\"default\"><recipe name=\"default\"><print name=\"mess\" message=\"$(svn.url)\"/></recipe></project>");
        rpcClient.RemoteApi.insertProject(random, GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getSubversionConfig(TRIVIAL_ANT_REPOSITORY), type);
        rpcClient.RemoteApi.runBuild(random);

        getBrowser().loginAsAdmin();
        goToArtifact(random, 1, OutputProducingCommandSupport.OUTPUT_NAME, OutputProducingCommandSupport.OUTPUT_FILE);
        getBrowser().waitForTextPresent(TRIVIAL_ANT_REPOSITORY);
    }

    public void testBuildLogs() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random);

        getBrowser().loginAsAdmin();
        triggerSuccessfulBuild(random);

        // The logs tab, which should show us the first stage.
        BuildLogsPage logsPage = getBrowser().openAndWaitFor(BuildLogsPage.class, random, 1L, DEFAULT_STAGE);
        assertTrue(logsPage.isLogAvailable());
        getBrowser().waitForTextPresent(MESSAGE_RECIPE_COMPLETED);

        if (getBrowser().isFirefox())
        {
            logsPage.clickDownloadLink();
            getBrowser().waitForTextPresent(MESSAGE_CHECKING_REQUIREMENTS);
        }

        // Direct to the build log (high-level build messages).
        BuildLogPage logPage = getBrowser().openAndWaitFor(BuildLogPage.class, random, 1L);
        assertTrue(logPage.isLogAvailable());
        getBrowser().waitForTextPresent(MESSAGE_BUILD_COMPLETED);

        final StageLogPage stageLogPage = getBrowser().createPage(StageLogPage.class, random, 1L, DEFAULT_STAGE);
        if (getBrowser().isFirefox())
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
        getBrowser().waitForTextPresent(MESSAGE_RECIPE_COMPLETED);

        // Change the settings via the popup
        final int maxLines = stageLogPage.getMaxLines();
        TailSettingsDialog dialog = stageLogPage.clickConfigureAndWaitForDialog();
        dialog.setMaxLines(maxLines + 5);
        dialog.clickApply();
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return stageLogPage.getMaxLines() == maxLines + 5;
            }
        }, SeleniumBrowser.DEFAULT_TIMEOUT, "max lines to be updated");
    }

    public void testArtifactTab() throws Exception
    {
        String userLogin = random + "-user";
        String projectName = random + "-project";

        rpcClient.RemoteApi.insertTrivialUser(userLogin);

        AntProjectHelper project = projectConfigurations.createTrivialAntProject(projectName);
        FileArtifactConfiguration explicitArtifact = project.addArtifact("explicit", "build.xml");
        FileArtifactConfiguration featuredArtifact = project.addArtifact("featured", "build.xml");
        featuredArtifact.setFeatured(true);
        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);

        long buildNumber = rpcClient.RemoteApi.runBuild(projectName);

        getBrowser().loginAndWait(userLogin, "");

        BuildArtifactsPage page = getBrowser().openAndWaitFor(BuildArtifactsPage.class, projectName, buildNumber);
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

        getBrowser().refresh();

        assertEquals("featured", page.getCurrentFilter());
        assertFalse(page.isArtifactListed(OutputProducingCommandSupport.OUTPUT_NAME));
        assertFalse(page.isArtifactListed(explicitArtifact.getName()));
        assertTrue(page.isArtifactListed(featuredArtifact.getName()));

        assertTrue(page.isArtifactFileListed(featuredArtifact.getName(), "build.xml"));
    }

    public void testDownloadArtifactLink() throws Exception
    {
        // CIB-1724: download raw artifacts via 2.0 url scheme.
        rpcClient.RemoteApi.insertSimpleProject(random);
        long buildNumber = rpcClient.RemoteApi.runBuild(random);
        Hashtable<String, Object> outputArtifact = getArtifact(random, buildNumber, "command output");
        String permalink = (String) outputArtifact.get("permalink");
        // This is to check for the new 2.0-ised URL.
        assertTrue(permalink.contains("/downloads/"));

        getBrowser().loginAsAdmin();
        getBrowser().open(StringUtils.join("/", true, urls.base(), permalink + "output.txt"));
        getBrowser().waitForTextPresent("BUILD SUCCESSFUL");
    }

    public void testSetArtifactContentType() throws Exception
    {
        final String ARTIFACT_NAME = "ant build file";
        final String ARTIFACT_FILENAME = "build.xml";
        final String CONTENT_TYPE = "application/test";

        String projectPath = rpcClient.RemoteApi.insertSimpleProject(random);

        Hashtable<String, Object> artifactConfig = rpcClient.RemoteApi.createDefaultConfig(FileArtifactConfiguration.class);
        artifactConfig.put(NAME, ARTIFACT_NAME);
        artifactConfig.put(Project.Command.FileArtifact.FILE, ARTIFACT_FILENAME);
        artifactConfig.put(TYPE, CONTENT_TYPE);
        rpcClient.RemoteApi.insertConfig(PathUtils.getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE_NAME, COMMANDS, DEFAULT_COMMAND, ARTIFACTS), artifactConfig);

        int buildNumber = rpcClient.RemoteApi.runBuild(random, BUILD_TIMEOUT);

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

        AntProjectHelper project = projectConfigurations.createTrivialAntProject(random);
        project.addArtifact(NAME_NOT_HASHED, BUILD_FILE);
        FileArtifactConfiguration hashedArtifact = project.addArtifact(NAME_HASHED, BUILD_FILE);
        hashedArtifact.setCalculateHash(true);
        hashedArtifact.setHashAlgorithm(CommandContext.HashAlgorithm.MD5);
        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);

        long buildNumber = rpcClient.RemoteApi.runBuild(random);

        Hashtable<String, Object> hashedInfo = getArtifact(random, buildNumber, NAME_HASHED);
        String content = AcceptanceTestUtils.readUriContent(getArtifactFileUrl(BUILD_FILE, hashedInfo));
        String expectedHash = SecurityUtils.md5Digest(content);

        getBrowser().loginAsAdmin();

        BuildArtifactsPage page = getBrowser().openAndWaitFor(BuildArtifactsPage.class, random, buildNumber);
        assertTrue(page.isArtifactFileListed(NAME_NOT_HASHED, BUILD_FILE));
        assertEquals("", page.getArtifactFileHash(NAME_NOT_HASHED, BUILD_FILE));
        assertTrue(page.isArtifactFileListed(NAME_HASHED, BUILD_FILE));
        assertEquals(expectedHash, page.getArtifactFileHash(NAME_HASHED, BUILD_FILE));
    }

    private Hashtable<String, Object> getArtifact(String project, long buildNumber, final String name) throws Exception
    {
        Vector<Hashtable<String, Object>> artifacts = rpcClient.RemoteApi.getArtifactsInBuild(project, buildNumber);
        Hashtable<String, Object> artifact = find(artifacts, new Predicate<Hashtable<String, Object>>()
        {
            public boolean apply(Hashtable<String, Object> artifact)
            {
                return name.equals(artifact.get("name"));
            }
        }, null);

        assertNotNull(artifact);
        return artifact;
    }

    private String getArtifactFileUrl(String filename, Hashtable<String, Object> artifact)
    {
        return baseUrl + artifact.get("permalink") + filename;
    }

    public void testManualTriggerBuildWithPrompt() throws Exception
    {
        getBrowser().loginAsAdmin();
        rpcClient.RemoteApi.ensureProject(random);

        // add the pname=pvalue property to the build.
        rpcClient.RemoteApi.insertProjectProperty(random, "pname", "pvalue", true, false);

        // trigger a build
        ProjectHomePage home = getBrowser().openAndWaitFor(ProjectHomePage.class, random);
        home.triggerBuild();

        // we should be prompted for a revision and a pname value.
        TriggerBuildForm form = getBrowser().createForm(TriggerBuildForm.class);
        form.waitFor();
        form.waitFor();

        // leave the revision blank
        form.triggerFormElements(asPair("status", STATUS_INTEGRATION));

        // next page is the project homepage.
        waitForBuildOnProjectHomePage(random);
    }

    /**
     * Check that the prompted property values that in a manual build are added to the build,
     * but do not change the project configuration.
     *
     * @throws Exception on error.
     */
    public void testManualTriggerBuildWithPromptAllowsPropertyValueOverride() throws Exception
    {
        getBrowser().loginAsAdmin();
        rpcClient.RemoteApi.ensureProject(random);

        // add the pname=pvalue property to the build.
        rpcClient.RemoteApi.insertProjectProperty(random, "pname", "pvalue", true, false);

        // trigger a build
        ProjectHomePage home = getBrowser().openAndWaitFor(ProjectHomePage.class, random);
        home.triggerBuild();

        // we should be prompted for a revision and a pname value.
        TriggerBuildForm form = getBrowser().createForm(TriggerBuildForm.class);
        form.addProperty("pname");
        form.waitFor();
        // leave the revision blank, update pname to qvalue.
        form.triggerFormElements(asPair("status",STATUS_INTEGRATION), asPair("pproperty.pname", "qvalue"));

        // next page is the project homepage.
        waitForBuildOnProjectHomePage(random);

        // verify that the correct property value was used in the build.
        assertEnvironment(random, 1, "pname=qvalue", "PULSE_PNAME=qvalue");

        // go back to the properties page and ensure that the value is pvalue.
        ListPage propertiesPage = getBrowser().openAndWaitFor(ListPage.class, getPropertiesPath(random));

        assertEquals("pname", propertiesPage.getCellContent(0, 0));
        assertEquals("pvalue", propertiesPage.getCellContent(0, 1));
    }

    public void testManualTriggerBuildWithPromptAllowsStatusSelection() throws Exception
    {
        getBrowser().loginAsAdmin();
        rpcClient.RemoteApi.ensureProject(random);

        // trigger a build
        ProjectHomePage home = getBrowser().openAndWaitFor(ProjectHomePage.class, random);
        home.triggerBuild();

        TriggerBuildForm triggerBuildForm = getBrowser().createForm(TriggerBuildForm.class);
        triggerBuildForm.waitFor();
        triggerBuildForm.triggerFormElements(asPair("status", STATUS_RELEASE));

        // next page is the project homepage.
        waitForBuildOnProjectHomePage(random);

        assertEquals(STATUS_RELEASE, repository.getIvyModuleDescriptor(random, 1).getStatus());
    }

    public void testTriggerProperties() throws Exception
    {
        String manualProject = random + "-manual";
        String buildCompletedProject = random + "-completed";

        rpcClient.RemoteApi.ensureProject(manualProject);
        rpcClient.RemoteApi.ensureProject(buildCompletedProject);

        Hashtable<String, Object> buildCompletedTrigger = rpcClient.RemoteApi.createEmptyConfig(BuildCompletedTriggerConfiguration.class);
        buildCompletedTrigger.put("name", "cascade");
        buildCompletedTrigger.put("project", PathUtils.getPath(PROJECTS_SCOPE, manualProject));

        Hashtable<String, Object> property = rpcClient.RemoteApi.createProperty("tp", "tpv");
        Hashtable<String, Hashtable<String, Object>> properties = new Hashtable<String, Hashtable<String, Object>>();
        properties.put("trigger property", property);
        buildCompletedTrigger.put("properties", properties);

        rpcClient.RemoteApi.insertConfig(PathUtils.getPath(PROJECTS_SCOPE, buildCompletedProject, "triggers"), buildCompletedTrigger);
        rpcClient.RemoteApi.runBuild(manualProject);
        rpcClient.RemoteApi.waitForBuildToComplete(buildCompletedProject, 1);

        getBrowser().loginAsAdmin();
        assertEnvironment(buildCompletedProject, 1, "PULSE_TP=tpv");
    }

    public void testAgentProperties() throws Exception
    {
        String projectName = random + "-project";
        String propertyName = random + "-prop";
        final String projectValue = random + "-projectval";
        final String agentValue = random + "-agentval";

        rpcClient.RemoteApi.ensureProject(projectName);
        assignStageToAgent(projectName, DEFAULT_STAGE, MASTER_AGENT_NAME);
        rpcClient.RemoteApi.insertProjectProperty(projectName, propertyName, projectValue, true, false);
        rpcClient.RemoteApi.insertAgentProperty(MASTER_AGENT_NAME, propertyName, agentValue, true, false);

        getBrowser().loginAsAdmin();
        triggerSuccessfulBuild(projectName);
        goToEnv(projectName, 1);
        getBrowser().waitForTextPresent(agentValue);
        assertFalse(getBrowser().isTextPresent(projectValue));
    }

    public void testVersionedBuildWithImports() throws Exception
    {
        getBrowser().loginAsAdmin();
        rpcClient.RemoteApi.insertProject(random, GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getSubversionConfig(VERSIONED_REPOSITORY), rpcClient.RemoteApi.createVersionedConfig("pulse/pulse.xml"));

        triggerSuccessfulBuild(random);
    }

    public void testTestResults() throws Exception
    {
        final String SUCCESSFUL_TEST = "testAdd";

        Hashtable<String, Object> antConfig = rpcClient.RemoteApi.getAntConfig();
        antConfig.put(Constants.Project.AntCommand.TARGETS, "test");
        String projectPath = rpcClient.RemoteApi.insertSingleCommandProject(random, GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getSubversionConfig(TEST_ANT_REPOSITORY), antConfig);
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
        Hashtable<String, Object> config = rpcClient.RemoteApi.getAntConfig();
        config.put(Constants.Project.AntCommand.TARGETS, "test");
        config.put(Constants.Project.AntCommand.ARGUMENTS, "-Dignore.test.result=true");
        String projectPath = rpcClient.RemoteApi.insertSingleCommandProject(random, GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getSubversionConfig(TEST_ANT_REPOSITORY), config);

        // Capture the results, change our processor to load the expected
        // failures.
        insertTestCapture(projectPath, JUNIT_PROCESSOR);
        String ppPath = PathUtils.getPath(projectPath, Project.POST_PROCESSORS, JUNIT_PROCESSOR);
        config = rpcClient.RemoteApi.getConfig(ppPath);
        config.put("expectedFailureFile", "expected-failures.txt");
        rpcClient.RemoteApi.saveConfig(ppPath, config, false);

        // Make sure the ant processor doesn't fail the build.
        ppPath = PathUtils.getPath(projectPath, Project.POST_PROCESSORS, ANT_PROCESSOR);
        config = rpcClient.RemoteApi.getConfig(ppPath);
        config.put("failOnError", false);
        rpcClient.RemoteApi.saveConfig(ppPath, config, false);

        buildAndCheckTestSummary(true, new TestResultSummary(1, 0, 0, 0, 2));
    }

    private TestSuitePage buildAndCheckTestSummary(boolean expectedSuccess, TestResultSummary expectedSummary) throws Exception
    {
        long buildId = rpcClient.RemoteApi.runBuild(random);
        Hashtable<String, Object> build = rpcClient.RemoteApi.getBuild(random, (int) buildId);
        boolean success = (Boolean) build.get("succeeded");
        assertEquals(expectedSuccess, success);

        getBrowser().loginAsAdmin();
        BuildTestsPage testsPage = getBrowser().openAndWaitFor(BuildTestsPage.class, random, buildId);

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

        return suitePage;
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
        final String PROCESSOR_SUITE = "sweety";

        Hashtable<String, Object> antConfig = rpcClient.RemoteApi.getAntConfig();
        antConfig.put(Constants.Project.AntCommand.TARGETS, "test");
        String projectPath = rpcClient.RemoteApi.insertSingleCommandProject(random, GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getSubversionConfig(TEST_ANT_REPOSITORY), antConfig);

        Hashtable<String, Object> junitProcessor = rpcClient.RemoteApi.createDefaultConfig(JUnitReportPostProcessorConfiguration.class);
        junitProcessor.put(NAME, PROCESSOR_NAME);
        junitProcessor.put("suite", PROCESSOR_SUITE);
        rpcClient.RemoteApi.insertConfig(PathUtils.getPath(projectPath, POSTPROCESSORS), junitProcessor);

        insertTestCapture(projectPath, PROCESSOR_NAME);

        long buildId = rpcClient.RemoteApi.runBuild(random);

        getBrowser().loginAsAdmin();

        // Test we can drill all the way down then back up again.
        BuildTestsPage testsPage = getBrowser().openAndWaitFor(BuildTestsPage.class, random, buildId);

        assertTrue(testsPage.getTestSummary().getTotal() > 0);
        StageTestsPage stageTestsPage = testsPage.clickStageAndWait("default");
        TestSuitePage topSuitePage = stageTestsPage.clickSuiteAndWait(PROCESSOR_SUITE);
        TestSuitePage nestedSuitePage = topSuitePage.clickSuiteAndWait("com.zutubi.testant.UnitTest");

        nestedSuitePage.clickSuiteCrumb(PROCESSOR_SUITE);
        topSuitePage.waitFor();
        topSuitePage.clickStageCrumb();
        stageTestsPage.waitFor();
        stageTestsPage.clickAllCrumb();
        testsPage.waitFor();
    }

    public void testOCUnitTestResults() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSingleCommandProject(random, GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getSubversionConfig(OCUNIT_REPOSITORY), rpcClient.RemoteApi.getAntConfig());

        Hashtable<String, Object> artifactConfig = rpcClient.RemoteApi.createDefaultConfig(FileArtifactConfiguration.class);
        artifactConfig.put("name", "test report");
        artifactConfig.put("file", "results.txt");
        artifactConfig.put(POSTPROCESSORS, new Vector<String>(Arrays.asList(PathUtils.getPath(projectPath, POSTPROCESSORS, "ocunit output processor"))));
        rpcClient.RemoteApi.insertConfig(PathUtils.getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE_NAME, COMMANDS, DEFAULT_COMMAND, ARTIFACTS), artifactConfig);

        long buildId = rpcClient.RemoteApi.runBuild(random, BUILD_TIMEOUT);

        getBrowser().loginAsAdmin();

        BuildTestsPage testsPage = getBrowser().openAndWaitFor(BuildTestsPage.class, random, buildId);
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
            final WaitProject project = projectConfigurations.createWaitAntProject(random, tempDir, false);

            DirectoryArtifactConfiguration reportsArtifact = new DirectoryArtifactConfiguration("test reports", "reports/xml");
            PostProcessorConfiguration junitProcessor = CONFIGURATION_HELPER.getPostProcessor(JUNIT_PROCESSOR, JUnitReportPostProcessorConfiguration.class);
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
            
            CONFIGURATION_HELPER.insertProject(project.getConfig(), false);
            
            // Sadly inserting the stages does not always yield the correct order, so we need to ensure it's right.
            final String defaultStageName = project.getDefaultStage().getName();
            String stagesPath = PathUtils.getPath(project.getConfig().getConfigurationPath(), "stages");
            rpcClient.RemoteApi.setConfigOrder(stagesPath, defaultStageName, SECOND_STAGE);

            rpcClient.RemoteApi.triggerBuild(project.getName());
            rpcClient.RemoteApi.waitForBuildInProgress(project.getName(), 1);

            TestUtils.waitForCondition(new Condition()
            {
                public boolean satisfied()
                {
                    try
                    {
                        Hashtable<String, Object> build = rpcClient.RemoteApi.getBuild(project.getName(), 1);
                        return stageIsComplete(build, defaultStageName);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }, BUILD_TIMEOUT, "stage to complete");

            getBrowser().loginAsAdmin();
            BuildTestsPage testsPage = getBrowser().openAndWaitFor(BuildTestsPage.class, project.getName(), 1L);
            assertTrue(testsPage.isStagePresent(defaultStageName));
            assertTrue(testsPage.isStageComplete(defaultStageName));
            assertTrue(testsPage.isStageLinkPresent(defaultStageName));
            assertTrue(testsPage.hasFailedTestsForStage(defaultStageName));

            assertTrue(testsPage.isStagePresent(SECOND_STAGE));
            assertFalse(testsPage.isStageComplete(SECOND_STAGE));
            assertFalse(testsPage.isStageLinkPresent(SECOND_STAGE));
            assertFalse(testsPage.hasFailedTestsForStage(SECOND_STAGE));
                
            project.releaseBuild();
            rpcClient.RemoteApi.waitForBuildToComplete(project.getName(), 1);
            
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
        @SuppressWarnings({"unchecked"})
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

        String templatePath = rpcClient.RemoteApi.insertSimpleProject(template, true);
        String leaderPath = rpcClient.RemoteApi.insertSimpleProject(leader, template, false);
        rpcClient.RemoteApi.insertSimpleProject(follower1, template, false);
        rpcClient.RemoteApi.insertSimpleProject(follower2, template, false);

        // Build follower1 before setting leader, to test the id not being less
        // than an existing build.
        assertEquals(1, rpcClient.RemoteApi.runBuild(follower1, BUILD_TIMEOUT));

        // Set the leader in the template.
        String optionsPath = PathUtils.getPath(templatePath, Constants.Project.OPTIONS);
        Hashtable<String, Object> templateOptions = rpcClient.RemoteApi.getConfig(optionsPath);
        templateOptions.put(Constants.Project.Options.ID_LEADER, leaderPath);
        rpcClient.RemoteApi.saveConfig(optionsPath, templateOptions, false);

        // Make sure projects are sharing sequence.
        assertEquals(2, rpcClient.RemoteApi.runBuild(follower1, BUILD_TIMEOUT));
        assertEquals(3, rpcClient.RemoteApi.runBuild(follower2, BUILD_TIMEOUT));
        assertEquals(4, rpcClient.RemoteApi.runBuild(follower1, BUILD_TIMEOUT));

        // Clear the leader
        templateOptions.put(Constants.Project.Options.ID_LEADER, "");
        rpcClient.RemoteApi.saveConfig(optionsPath, templateOptions, false);

        // Make sure follower2 is back on its own sequence.
        assertEquals(4, rpcClient.RemoteApi.runBuild(follower2, BUILD_TIMEOUT));
    }

    public void testPersistentWorkDir() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(random, false);

        String bootstrapPath = PathUtils.getPath(projectPath, Project.BOOTSTRAP);
        Hashtable<String, Object> bootstrap = rpcClient.RemoteApi.getConfig(bootstrapPath);
        bootstrap.put(Bootstrap.CHECKOUT_TYPE, CheckoutType.INCREMENTAL_CHECKOUT.name());
        bootstrap.put(Bootstrap.BUILD_TYPE, BuildType.INCREMENTAL_BUILD.name());
        bootstrap.put(Bootstrap.PERSISTENT_DIR_PATTERN, "$(data.dir)/customwork/$(project)");
        rpcClient.RemoteApi.saveConfig(bootstrapPath, bootstrap, false);

        String stagePath = PathUtils.getPath(projectPath, Constants.Project.STAGES, "default");
        Hashtable<String, Object> stage = rpcClient.RemoteApi.getConfig(stagePath);
        stage.put(Constants.Project.Stage.AGENT, PathUtils.getPath(AGENTS_SCOPE, MASTER_AGENT_NAME));
        rpcClient.RemoteApi.saveConfig(stagePath, stage, false);

        rpcClient.RemoteApi.runBuild(random, BUILD_TIMEOUT);

        String dataDir = rpcClient.RemoteApi.getServerInfo().get(ConfigurationManager.CORE_PROPERTY_PULSE_DATA_DIR);
        File workDir = new File(FileSystemUtils.composeFilename(dataDir, "customwork", random));
        assertTrue(workDir.isDirectory());
        File buildFile = new File(workDir, "build.xml");
        assertTrue(buildFile.isFile());
    }

    public void testCustomTempDir() throws Exception
    {
        File tempDir = createTempDirectory();
        try
        {
            final WaitProject project = projectConfigurations.createWaitAntProject(random, tempDir, false);
            final String tempDirPattern = tempDir.getAbsolutePath() + "/base";
            project.getConfig().getBootstrap().setTempDirPattern(tempDirPattern);
            project.getDefaultStage().setAgent(CONFIGURATION_HELPER.getAgentReference(AgentManager.MASTER_AGENT_NAME));
            CONFIGURATION_HELPER.insertProject(project.getConfig(), false);

            rpcClient.RemoteApi.triggerBuild(project.getName());
            rpcClient.RemoteApi.waitForBuildInProgress(project.getName(), 1);

            TestUtils.waitForCondition(new Condition()
            {
                public boolean satisfied()
                {
                    File buildFile = new File(tempDirPattern, "build.xml");
                    return buildFile.isFile();
                }
            }, BUILD_TIMEOUT, "build file to appear in customr temp directory");

            project.releaseBuild();
            rpcClient.RemoteApi.waitForBuildToComplete(project.getName(), 1);
            
            assertFalse(new File(tempDirPattern).exists());
        }
        finally
        {
            try
            {
                FileSystemUtils.rmdir(tempDir);
            }
            catch (IOException e)
            {
                LOG.warning(e);
            }
        }
    }

    public void testTerminateOnStageFailure() throws Exception
    {
        String projectPath = createProjectWithTwoAntStages(random, "nosuchbuildfile.xml", "another-stage");
        setTerminateStageOnFailure(projectPath);

        long buildId = rpcClient.RemoteApi.runBuild(random, BUILD_TIMEOUT);

        getBrowser().loginAsAdmin();
        getBrowser().openAndWaitFor(BuildSummaryPage.class, random, buildId);
        getBrowser().waitForTextPresent(String.format("Build terminated due to failure of stage '%s'", DEFAULT_STAGE));

        Hashtable<String, Object> build = rpcClient.RemoteApi.getBuild(random, (int)buildId);
        assertResultState(ResultState.FAILURE, build);
        assertStageState(ResultState.FAILURE, ProjectConfigurationWizard.DEFAULT_STAGE, build.get("stages"));
        assertStageState(ResultState.CANCELLED, "another-stage", build.get("stages"));
    }

    public void testTerminateOnStageFailureStageSucceeds() throws Exception
    {
        String projectPath = createProjectWithTwoAntStages(random, "build.xml", "another-stage");
        setTerminateStageOnFailure(projectPath);

        long buildId = rpcClient.RemoteApi.runBuild(random, BUILD_TIMEOUT);

        getBrowser().loginAsAdmin();
        getBrowser().openAndWaitFor(BuildSummaryPage.class, random, buildId);
        assertFalse(getBrowser().isTextPresent("terminated"));

        Hashtable<String, Object> build = rpcClient.RemoteApi.getBuild(random, (int)buildId);
        assertResultState(ResultState.SUCCESS, build);

        assertStageState(ResultState.SUCCESS, ProjectConfigurationWizard.DEFAULT_STAGE, build.get("stages"));
        assertStageState(ResultState.SUCCESS, "another-stage", build.get("stages"));
    }

    public void testTerminateOnStageFailureLimit() throws Exception
    {
        String projectPath = createProjectWithTwoAntStages(random, "nosuchbuildfile.xml", "another-stage");
        String optionsPath = PathUtils.getPath(projectPath, Constants.Project.OPTIONS);
        Hashtable<String, Object> options = rpcClient.RemoteApi.getConfig(optionsPath);
        options.put("stageFailureLimit", 1);
        rpcClient.RemoteApi.saveConfig(optionsPath, options, false);

        long buildId = rpcClient.RemoteApi.runBuild(random, BUILD_TIMEOUT);

        getBrowser().loginAsAdmin();
        getBrowser().openAndWaitFor(BuildSummaryPage.class, random, buildId);
        getBrowser().waitForTextPresent("Build terminated due to the stage failure limit (1) being reached");

        Hashtable<String, Object> build = rpcClient.RemoteApi.getBuild(random, (int)buildId);
        assertResultState(ResultState.FAILURE, build);
        assertStageState(ResultState.FAILURE, ProjectConfigurationWizard.DEFAULT_STAGE, build.get("stages"));
        assertStageState(ResultState.CANCELLED, "another-stage", build.get("stages"));
    }

    private void assertResultState(ResultState expectedState, Hashtable<String, Object> result)
    {
        assertEquals(expectedState, ResultState.fromPrettyString((String) result.get("status")));
    }

    private void assertStageState(ResultState expectedState, final String stageName, Object stages)
    {
        @SuppressWarnings({"unchecked"})
        Vector<Hashtable<String, Object>> stagesVector = (Vector<Hashtable<String, Object>>) stages;
        Hashtable<String, Object> stage = find(stagesVector, new Predicate<Hashtable<String, Object>>()
        {
            public boolean apply(Hashtable<String, Object> detail)
            {
                return detail.get("name").equals(stageName);
            }
        }, null);
        assertNotNull(stage);
        assertEquals(expectedState, ResultState.fromPrettyString((String) stage.get("status")));
    }

    public void testDeadlockUpdatingConfigAfterTrigger() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random);
        String propertyPath = rpcClient.RemoteApi.insertProjectProperty(random, "prop", "val", true, false);
        Hashtable<String, Object> property = rpcClient.RemoteApi.getConfig(propertyPath);
        
        // Run through a few times to make the deadlock more likely to happen.
        // This is a balance between the speed of the test and likelihood of
        // triggering the problem.
        for (int i = 0; i < 3; i++)
        {
            Vector<String> ids = rpcClient.RemoteApi.triggerBuild(random, new Hashtable<String, Object>());
            property.put("value", Integer.toString(i));
            rpcClient.RemoteApi.saveConfig(propertyPath, property, false);
            Hashtable<String, Object> buildRequest = rpcClient.RemoteApi.waitForBuildRequestToBeActivated(ids.get(0), BUILD_TIMEOUT);
            rpcClient.RemoteApi.waitForBuildToComplete(random, Integer.parseInt((String) buildRequest.get("buildId")));
        }
    }
    
    public void testPulseFileTab() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random);
        long buildId = rpcClient.RemoteApi.runBuild(random);

        getBrowser().loginAsAdmin();
        BuildFilePage buildFilePage = getBrowser().openAndWaitFor(BuildFilePage.class, random, buildId);
        assertTrue(buildFilePage.isDownloadLinkPresent());
        assertTrue(buildFilePage.isHighlightedFilePresent());
        getBrowser().waitForTextPresent("default-recipe=\"default\"");
    }
    
    public void testCleanBuild() throws Exception
    {
        String projectName = random + "-project";
        String agentName = random + "-agent";

        Hashtable<String, Object> agent = rpcClient.RemoteApi.createEmptyConfig("zutubi.agentConfig");
        agent.put("name", agentName);
        agent.put("remote", false);

        String agentPath = rpcClient.RemoteApi.insertTemplatedConfig(MasterConfigurationRegistry.AGENTS_SCOPE + "/" + AgentManager.GLOBAL_AGENT_NAME, agent, false);

        try
        {
            String projectPath = rpcClient.RemoteApi.insertSimpleProject(projectName);
            assignStageToAgent(projectName, DEFAULT_STAGE, agentName);
            setToIncrementalCheckoutAndBuild(projectName);

            rpcClient.RemoteApi.runBuild(projectName, BUILD_TIMEOUT);
            rpcClient.RemoteApi.doConfigAction(projectPath, ProjectConfigurationActions.ACTION_MARK_CLEAN);
            long buildId = rpcClient.RemoteApi.runBuild(projectName, BUILD_TIMEOUT);

            getBrowser().loginAsAdmin();

            getBrowser().openAndWaitFor(CommandArtifactPage.class, projectName, buildId, DEFAULT_STAGE, BootstrapCommandConfiguration.COMMAND_NAME, BootstrapCommand.OUTPUT_NAME + "/" + BootstrapCommand.FILES_FILE);
            getBrowser().waitForTextPresent("build.xml");

            AgentStatusPage statusPage = getBrowser().openAndWaitFor(AgentStatusPage.class, agentName);
            SynchronisationMessageTable.SynchronisationMessage synchronisationMessage = statusPage.getSynchronisationMessagesTable().getMessage(0);
            assertEquals("clean up stage '" + DEFAULT_STAGE + "' of project '" + projectName + "'", synchronisationMessage.description);
        }
        finally
        {
            rpcClient.RemoteApi.deleteConfig(agentPath);
        }
    }
    

    public void testCleanBuildTwoStagesOneAgent() throws Exception
    {
        final String SECOND_STAGE_NAME = "stage-left";

        String projectName = random + "-project";
        String agentName = random + "-agent";

        Hashtable<String, Object> agent = rpcClient.RemoteApi.createEmptyConfig("zutubi.agentConfig");
        agent.put("name", agentName);
        agent.put("remote", false);

        String agentPath = rpcClient.RemoteApi.insertTemplatedConfig(MasterConfigurationRegistry.AGENTS_SCOPE + "/" + AgentManager.GLOBAL_AGENT_NAME, agent, false);

        try
        {
            String projectPath = createProjectWithTwoAntStages(projectName, "build.xml", SECOND_STAGE_NAME);
            setToIncrementalCheckoutAndBuild(projectName);

            // First build establishes directories for both stages on master agent.
            rpcClient.RemoteApi.runBuild(projectName, BUILD_TIMEOUT);

            // For the next build, clean, but send the second stage to a
            // different agent.
            assignStageToAgent(projectName, SECOND_STAGE_NAME, agentName);
            rpcClient.RemoteApi.doConfigAction(projectPath, ProjectConfigurationActions.ACTION_MARK_CLEAN);
            rpcClient.RemoteApi.runBuild(projectName, BUILD_TIMEOUT);

            // Finally, set the second stage back to the original agent.  Check
            // that it does not end up with the directory from the first build.
            // This is verified by ensuring the build.xml file is checked out.
            assignStageToAgent(projectName, SECOND_STAGE_NAME, AgentManager.MASTER_AGENT_NAME);
            long buildId = rpcClient.RemoteApi.runBuild(projectName, BUILD_TIMEOUT);

            getBrowser().loginAsAdmin();

            getBrowser().openAndWaitFor(CommandArtifactPage.class, projectName, buildId, SECOND_STAGE_NAME, BootstrapCommandConfiguration.COMMAND_NAME, BootstrapCommand.OUTPUT_NAME + "/" + BootstrapCommand.FILES_FILE);
            getBrowser().waitForTextPresent("build.xml");
        }
        finally
        {
            rpcClient.RemoteApi.deleteConfig(agentPath);
        }
    }
    
    private void setToIncrementalCheckoutAndBuild(String projectName) throws Exception
    {
        String bootstrapPath = PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, projectName, Project.BOOTSTRAP);
        Hashtable<String, Object> bootstrap = rpcClient.RemoteApi.getConfig(bootstrapPath);
        bootstrap.put(Bootstrap.CHECKOUT_TYPE, CheckoutType.INCREMENTAL_CHECKOUT.toString());
        bootstrap.put(Bootstrap.BUILD_TYPE, BuildType.INCREMENTAL_BUILD.toString());
        rpcClient.RemoteApi.saveConfig(bootstrapPath, bootstrap, false);
        rpcClient.RemoteApi.waitForProjectToInitialise(projectName);
    }

    public void testPulseFilePropertiesAddedToEnvironment() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertProject(random, GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getSubversionConfig(VERSIONED_REPOSITORY), rpcClient.RemoteApi.createVersionedConfig("pulse/pulse.xml"));
        String stagePath = getPath(projectPath, Constants.Project.STAGES, DEFAULT_STAGE);
        Hashtable<String, Object> defaultStage = rpcClient.RemoteApi.getConfig(stagePath);
        defaultStage.put(Project.Stage.RECIPE, "properties");
        rpcClient.RemoteApi.saveConfig(stagePath, defaultStage, false);

        rpcClient.RemoteApi.runBuild(random, BUILD_TIMEOUT);

        getBrowser().loginAsAdmin();
        EnvironmentArtifactPage envPage = getBrowser().openAndWaitFor(EnvironmentArtifactPage.class, random, 1L, DEFAULT_STAGE, "c1");
        assertTrue(envPage.isPulsePropertyPresentWithValue("outerp", "original-value"));
        assertTrue(envPage.isPulsePropertyPresentWithValue("p1", "original-value"));
        assertFalse(envPage.isPulsePropertyPresent("p2"));
        
        envPage = getBrowser().openAndWaitFor(EnvironmentArtifactPage.class, random, 1L, DEFAULT_STAGE, "c2");
        assertTrue(envPage.isPulsePropertyPresentWithValue("outerp", "new-value"));
        assertTrue(envPage.isPulsePropertyPresentWithValue("p1", "new-value"));
        assertTrue(envPage.isPulsePropertyPresentWithValue("p2", "value"));
    }

    public void testTriggerBuildWithNewProperties() throws Exception
    {
        AntProjectHelper project = projectConfigurations.createTrivialAntProject(random);
        project.addProperty("env", "value").setAddToEnvironment(true);
        project.addProperty("notenv", "value").setAddToEnvironment(false);
        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);

        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("new", "newvalue");
        Hashtable<String, Object> options = new Hashtable<String, Object>();
        options.put("properties", properties);
        rpcClient.RemoteApi.triggerBuild(random, options);
        rpcClient.RemoteApi.waitForBuildToComplete(random, 1);

        getBrowser().loginAsAdmin();
        EnvironmentArtifactPage envPage = getBrowser().openAndWaitFor(EnvironmentArtifactPage.class, random, 1L, DEFAULT_STAGE, DEFAULT_COMMAND);

        assertTrue(envPage.isPropertyPresentWithValue("env", "value"));
        assertFalse(envPage.isPropertyPresent("notenv"));
        assertFalse(envPage.isPropertyPresent("new"));
        assertTrue(envPage.isPulsePropertyPresentWithValue("env", "value"));
        assertTrue(envPage.isPulsePropertyPresentWithValue("notenv", "value"));
        assertTrue(envPage.isPulsePropertyPresentWithValue("new", "newvalue"));
    }

    public void testTriggerBuildWithExistingProperties() throws Exception
    {
        AntProjectHelper project = projectConfigurations.createTrivialAntProject(random);
        project.addProperty("env", "value").setAddToEnvironment(true);
        project.addProperty("notenv", "value").setAddToEnvironment(false);
        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);

        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("env", "newvalue");
        Hashtable<String, Object> options = new Hashtable<String, Object>();
        options.put("properties", properties);
        rpcClient.RemoteApi.triggerBuild(random, options);
        rpcClient.RemoteApi.waitForBuildToComplete(random, 1);

        getBrowser().loginAsAdmin();
        EnvironmentArtifactPage envPage = getBrowser().openAndWaitFor(EnvironmentArtifactPage.class, random, 1L, DEFAULT_STAGE, DEFAULT_COMMAND);
        assertTrue(envPage.isPropertyPresentWithValue("env", "newvalue"));
        assertFalse(envPage.isPropertyPresent("notenv"));
        assertTrue(envPage.isPulsePropertyPresentWithValue("env", "newvalue"));
        assertTrue(envPage.isPulsePropertyPresentWithValue("notenv", "value"));
    }

    public void testTriggerBuildWithBooleanProperty() throws Exception
    {
        AntProjectHelper project = projectConfigurations.createTrivialAntProject(random);
        project.getConfig().getOptions().setIsolateChangelists(true);
        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);

        rpcClient.RemoteApi.runBuild(random, BUILD_TIMEOUT);
        
        Hashtable<String, Object> options = new Hashtable<String, Object>();
        options.put("force", false);
        Vector<String> requests = rpcClient.RemoteApi.triggerBuild(random, options);
        assertEquals(0, requests.size());
        
        options.put("force", true);
        requests = rpcClient.RemoteApi.triggerBuild(random, options);
        assertEquals(1, requests.size());
        Hashtable<String, Object> build = rpcClient.RemoteApi.waitForBuildRequestToBeActivated(requests.get(0), BUILD_TIMEOUT);
        rpcClient.RemoteApi.waitForBuildToComplete(random, Integer.parseInt((String) build.get("buildId")));
    }
    
    private String createProjectWithTwoAntStages(String projectName, String buildFile, String secondStageName) throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSingleCommandProject(projectName, ProjectManager.GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getSubversionConfig(TRIVIAL_ANT_REPOSITORY), rpcClient.RemoteApi.getAntConfig(buildFile));
        Hashtable<String, String> keys = new Hashtable<String, String>();
        keys.put(DEFAULT_STAGE, secondStageName);
        rpcClient.RemoteApi.cloneConfig(PathUtils.getPath(projectPath, Project.STAGES), keys);
        return projectPath;
    }

    private void setTerminateStageOnFailure(String projectPath) throws Exception
    {
        String defaultStagePath = PathUtils.getPath(projectPath, Project.STAGES, DEFAULT_STAGE);
        Hashtable<String, Object> stage = rpcClient.RemoteApi.getConfig(defaultStagePath);
        stage.put("terminateBuildOnFailure", true);
        rpcClient.RemoteApi.saveConfig(defaultStagePath, stage, false);
    }

    private void insertTestCapture(String projectPath, String processorName) throws Exception
    {
        Hashtable<String, Object> dirArtifactConfig = rpcClient.RemoteApi.createDefaultConfig(DirectoryArtifactConfiguration.class);
        dirArtifactConfig.put(NAME, "xml reports");
        dirArtifactConfig.put(BASE, "build/reports/xml");
        dirArtifactConfig.put(POSTPROCESSORS, new Vector<String>(Arrays.asList(PathUtils.getPath(projectPath, POSTPROCESSORS, processorName))));
        rpcClient.RemoteApi.insertConfig(PathUtils.getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE_NAME, COMMANDS, DEFAULT_COMMAND, ARTIFACTS), dirArtifactConfig);
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
            getBrowser().waitForTextPresent(env);
        }
    }

    private void goToEnv(String projectName, long buildId)
    {
        goToArtifact(projectName, buildId, ExecutableCommand.ENV_ARTIFACT_NAME, ExecutableCommand.ENV_FILENAME);
    }

    private void goToArtifact(String projectName, long buildId, String artifact, String file)
    {
        BuildArtifactsPage artifactsPage = getBrowser().openAndWaitFor(BuildArtifactsPage.class, projectName, buildId);
        artifactsPage.setFilterAndWait("");
        artifactsPage.clickArtifactFileDownload(artifact, file);
    }

    private String addResource(String agent, String name) throws Exception
    {
        return addResource(agent, name, null);
    }

    private String addResource(String agent, String name, String version) throws Exception
    {
        Hashtable<String, Object> resource = rpcClient.RemoteApi.createDefaultConfig(ResourceConfiguration.class);
        resource.put("name", name);
        if (version != null)
        {
            Hashtable<String, Object> versionConfig = rpcClient.RemoteApi.createDefaultConfig(ResourceVersionConfiguration.class);
            versionConfig.put("value", version);
            Hashtable<String, Object> versions = new Hashtable<String, Object>();
            versions.put(version, versionConfig);
            resource.put("versions", versions);
        }
        
        return rpcClient.RemoteApi.insertConfig(getPath(AGENTS_SCOPE, agent, "resources"), resource);
    }

    private Hashtable<String, Object> createRequiredResource(String resource, String version) throws Exception
    {
        Hashtable<String, Object> requirement = rpcClient.RemoteApi.createDefaultConfig(ResourceRequirementConfiguration.class);
        requirement.put("resource", resource);
        if (StringUtils.stringSet(version))
        {
            requirement.put("version", version);
            requirement.put("defaultVersion", false);
        }

        return requirement;
    }

    private void triggerSuccessfulBuild(String projectName) throws Exception
    {
        rpcClient.RemoteApi.triggerBuild(projectName);
        waitForBuildOnProjectHomePage(projectName);
    }

    private void waitForBuildOnProjectHomePage(String projectName)
    {
        ProjectHomePage projectHomePage = getBrowser().openAndWaitFor(ProjectHomePage.class, projectName);
        ResultState state = projectHomePage.waitForLatestCompletedBuild(1, BUILD_TIMEOUT);
        assertEquals(ResultState.SUCCESS, state);
    }
}
