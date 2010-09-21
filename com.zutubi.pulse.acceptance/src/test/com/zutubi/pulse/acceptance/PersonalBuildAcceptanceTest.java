package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.BuildLogsPage;
import com.zutubi.pulse.acceptance.pages.browse.PersonalBuildLogPage;
import com.zutubi.pulse.acceptance.pages.browse.PersonalBuildLogsPage;
import com.zutubi.pulse.acceptance.pages.dashboard.*;
import com.zutubi.pulse.acceptance.support.PerforceUtils;
import com.zutubi.pulse.acceptance.support.ProxyServer;
import com.zutubi.pulse.acceptance.utils.AcceptancePersonalBuildUI;
import com.zutubi.pulse.acceptance.utils.PersonalBuildRunner;
import com.zutubi.pulse.acceptance.utils.workspace.SubversionWorkspace;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.personal.PersonalBuildException;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.WorkingCopy;
import com.zutubi.pulse.core.scm.p4.PerforceCore;
import com.zutubi.pulse.core.scm.svn.SubversionClient;
import com.zutubi.pulse.dev.personal.PersonalBuildConfig;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;
import com.zutubi.pulse.master.tove.config.project.hooks.*;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.*;
import org.tmatesoft.svn.core.SVNException;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.ADMIN_CREDENTIALS;
import static com.zutubi.pulse.acceptance.support.PerforceUtils.*;
import static com.zutubi.pulse.core.scm.p4.PerforceConstants.*;
import static com.zutubi.util.CollectionUtils.asPair;
import static java.util.Arrays.asList;

/**
 * Simple sanity checks for personal builds.
 */
public class PersonalBuildAcceptanceTest extends SeleniumTestBase
{
    private static final String PROJECT_NAME = "PersonalBuildAcceptanceTest-Project";
    private static final int BUILD_TIMEOUT = 90000;
    private static final String DEFAULT_ANT_BUILD_FILE = "build.xml";

    private File workingCopyDir;
    private PersonalBuildRunner buildRunner;

    protected void setUp() throws Exception
    {
        super.setUp();

        workingCopyDir = FileSystemUtils.createTempDir("PersonalBuildAcceptanceTest", "");

        xmlRpcHelper.loginAsAdmin();

        buildRunner = new PersonalBuildRunner(xmlRpcHelper);
        buildRunner.setBase(workingCopyDir);
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        FileSystemUtils.rmdir(workingCopyDir);

        super.tearDown();
    }

    public void testPersonalBuild() throws Exception
    {
        checkout(Constants.TRIVIAL_ANT_REPOSITORY);
        makeChangeToBuildFile();
        createConfigFile(PROJECT_NAME);

        browser.loginAsAdmin();
        ensureProject(PROJECT_NAME);
        editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, PROJECT_NAME);
        long buildNumber = runPersonalBuild(ResultState.FAILURE);
        verifyPersonalBuildTabs(PROJECT_NAME, buildNumber, DEFAULT_ANT_BUILD_FILE);

        PersonalEnvironmentArtifactPage envPage = browser.openAndWaitFor(PersonalEnvironmentArtifactPage.class, PROJECT_NAME, buildNumber, "default", "build");
        assertTrue(envPage.isPulsePropertyPresentWithValue(BuildProperties.PROPERTY_INCREMENTAL_BOOTSTRAP, Boolean.toString(false)));
        assertTrue(envPage.isPulsePropertyPresentWithValue(BuildProperties.PROPERTY_LOCAL_BUILD, Boolean.toString(false)));
        assertTrue(envPage.isPulsePropertyPresentWithValue(BuildProperties.PROPERTY_PERSONAL_BUILD, Boolean.toString(true)));
        assertTrue(envPage.isPulsePropertyPresentWithValue(BuildProperties.PROPERTY_OWNER, ADMIN_CREDENTIALS.getUserName()));
        assertTrue(envPage.isPulsePropertyPresentWithValue(BuildProperties.PROPERTY_PERSONAL_USER, ADMIN_CREDENTIALS.getUserName()));
        // Make sure this view is not decorated (CIB-1711).
        assertTextNotPresent("logout");
        
        verifyPersonalBuildArtifacts(buildNumber);
    }

    private void verifyPersonalBuildArtifacts(long buildNumber) throws Exception
    {
        Vector<Hashtable<String, Object>> artifacts = xmlRpcHelper.getArtifactsInPersonalBuild((int) buildNumber);
        assertEquals(3, artifacts.size());

        Hashtable<String, Object> outputArtifact = CollectionUtils.find(artifacts, new Predicate<Hashtable<String, Object>>()
        {
            public boolean satisfied(Hashtable<String, Object> stringObjectHashtable)
            {
                return stringObjectHashtable.get("name").equals("command output");
            }
        });

        assertNotNull(outputArtifact);
        assertEquals("/dashboard/my/" + buildNumber + "/downloads/default/build/command%20output/", outputArtifact.get("permalink"));

        Vector<String> listing = xmlRpcHelper.getArtifactFileListingPersonal((int) buildNumber, "default", "build", "command output", "");
        assertEquals(1, listing.size());
        assertEquals("output.txt", listing.get(0));
    }

    public void testPersonalBuildViaProxy() throws Exception
    {
        final int PROXY_PORT = 8754;

        ProxyServer proxyServer = new ProxyServer(PROXY_PORT);
        proxyServer.start();

        try
        {
            checkout(Constants.TRIVIAL_ANT_REPOSITORY);
            makeChangeToBuildFile();
            createConfigFile(PROJECT_NAME, asPair(PersonalBuildConfig.PROPERTY_PROXY_HOST, "localhost"), asPair(PersonalBuildConfig.PROPERTY_PROXY_PORT, PROXY_PORT));

            browser.loginAsAdmin();
            ensureProject(PROJECT_NAME);
            editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, PROJECT_NAME);
            long buildNumber = runPersonalBuild(ResultState.FAILURE);
            verifyPersonalBuildTabs(PROJECT_NAME, buildNumber, DEFAULT_ANT_BUILD_FILE);
        }
        finally
        {
            proxyServer.stop();
        }
    }

    public void testPersonalBuildChangesImportedFile() throws Exception
    {
        checkout(Constants.VERSIONED_REPOSITORY);
        makeChangeToImportedFile();
        createConfigFile(random);
        browser.loginAsAdmin();

        xmlRpcHelper.insertProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.VERSIONED_REPOSITORY), xmlRpcHelper.createVersionedConfig(Constants.VERSIONED_PULSE_FILE));
        editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, random);
        long buildNumber = runPersonalBuild(ResultState.ERROR);
        browser.openAndWaitFor(PersonalBuildSummaryPage.class, buildNumber);
        assertTextPresent("Unknown child element 'notrecognised'");
    }

    public void testPersonalBuildOnAgent() throws Exception
    {
        checkout(Constants.TRIVIAL_ANT_REPOSITORY);
        makeChangeToBuildFile();
        createConfigFile(PROJECT_NAME);

        browser.loginAsAdmin();
        ensureAgent(AGENT_NAME);
        ensureProject(PROJECT_NAME);
        editStageToRunOnAgent(AGENT_NAME, PROJECT_NAME);
        long buildNumber = runPersonalBuild(ResultState.FAILURE);
        verifyPersonalBuildTabs(PROJECT_NAME, buildNumber, DEFAULT_ANT_BUILD_FILE);
    }

    public void testPersonalBuildWithHooks() throws Exception
    {
        String projectPath = addProject(random, true);
        String hooksPath = PathUtils.getPath(projectPath, "buildHooks");

        // Create two of each type of hook: one that runs for personal builds,
        // and another that doesn't.
        insertHook(hooksPath, PreBuildHookConfiguration.class, "prebuildno", false);
        insertHook(hooksPath, PreBuildHookConfiguration.class, "prebuildyes", true);
        insertHook(hooksPath, PostBuildHookConfiguration.class, "postbuildno", false);
        insertHook(hooksPath, PostBuildHookConfiguration.class, "postbuildyes", true);
        insertHook(hooksPath, PostStageHookConfiguration.class, "poststageno", false);
        insertHook(hooksPath, PostStageHookConfiguration.class, "poststageyes", true);

        // Now make a change and run a personal build.
        checkout(Constants.TRIVIAL_ANT_REPOSITORY);
        makeChangeToBuildFile();
        createConfigFile(random);

        browser.loginAsAdmin();
        editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, random);
        long buildNumber = runPersonalBuild(ResultState.FAILURE);

        // Finally check that only the enabled hooks ran.
        String text = getLogText(random, buildNumber);
        assertFalse("Pre-build hook not for personal should not have run", text.contains("prebuildno"));
        assertTrue("Pre-build hook for personal should have run", text.contains("prebuildyes"));
        assertFalse("Post-build hook not for personal should not have run", text.contains("postbuildno"));
        assertTrue("Post-build hook for personal should have run", text.contains("postbuildyes"));

        text = getLogText(random, buildNumber, ProjectConfigurationWizard.DEFAULT_STAGE);
        assertFalse("Post-stage hook not for personal should not have run", text.contains("poststageno"));
        assertTrue("Post-stage hook for personal should have run", text.contains("poststageyes"));
    }

    public void testManuallyTriggerHook() throws Exception
    {
        final String HOOK_NAME = "manual-hook";

        String projectPath = addProject(random, true);
        Hashtable<String, Object> hook = xmlRpcHelper.createEmptyConfig(ManualBuildHookConfiguration.class);
        hook.put("name", HOOK_NAME);
        xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, "buildHooks"), hook);

        // Now make a change and run a personal build.
        checkout(Constants.TRIVIAL_ANT_REPOSITORY);
        makeChangeToBuildFile();
        createConfigFile(random);

        browser.loginAsAdmin();
        editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, random);
        long buildNumber = runPersonalBuild(ResultState.FAILURE);

        PersonalBuildSummaryPage page = browser.openAndWaitFor(PersonalBuildSummaryPage.class, buildNumber);
        assertTrue(page.isHookPresent(HOOK_NAME));
        page.clickHook(HOOK_NAME);

        browser.waitForVisible("status-message");
        assertTextPresent("triggered hook '" + HOOK_NAME + "'");
    }

    public void testPersonalBuildFloatingRevision() throws Exception
    {
        checkout(Constants.TRIVIAL_ANT_REPOSITORY);
        makeChangeToBuildFile();
        createConfigFile(PROJECT_NAME, asPair(PersonalBuildConfig.PROPERTY_REVISION, WorkingCopy.REVISION_FLOATING));

        browser.loginAsAdmin();
        ensureProject(PROJECT_NAME);
        editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, PROJECT_NAME);
        long buildNumber = runPersonalBuild(ResultState.FAILURE);

        // Check that we actually built against the latest.
        SubversionClient client = new SubversionClient(Constants.TRIVIAL_ANT_REPOSITORY, false);
        Revision revision = client.getLatestRevision(null);

        PersonalBuildChangesPage changesPage = browser.openAndWaitFor(PersonalBuildChangesPage.class, buildNumber);
        assertEquals(revision.getRevisionString(), changesPage.getCheckedOutRevision());
    }

    public void testPersonalBuildConflicts() throws Exception
    {
        checkout(Constants.TRIVIAL_ANT_REPOSITORY);
        makeChangeToBuildFile();
        // Set revision to something before the last edit to the build file.
        createConfigFile(PROJECT_NAME, asPair(PersonalBuildConfig.PROPERTY_REVISION, "1"), asPair(PersonalBuildConfig.PROPERTY_UPDATE, false));

        browser.loginAsAdmin();
        ensureProject(PROJECT_NAME);
        editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, PROJECT_NAME);
        long buildNumber = runPersonalBuild(ResultState.ERROR);

        browser.openAndWaitFor(PersonalBuildSummaryPage.class, buildNumber);
        assertTextPresent("Patch does not apply cleanly");
    }

    public void testGitPersonalBuild() throws Exception
    {
        String gitUrl = Constants.getGitUrl();
        xmlRpcHelper.insertSingleCommandProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getGitConfig(gitUrl), xmlRpcHelper.getAntConfig());
        editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, random);

        FileSystemUtils.rmdir(workingCopyDir);
        runGit(null, "clone", gitUrl, workingCopyDir.getAbsolutePath());
        createConfigFile(random);

        File buildFile = new File(workingCopyDir, DEFAULT_ANT_BUILD_FILE);
        FileSystemUtils.createFile(buildFile, "<?xml version=\"1.0\"?>\n" +
                "<project default=\"build\">\n" +
                "  <target name=\"build\">\n" +
                "    <fail message=\"Force build failure\"/>\n" +
                "  </target>\n" +
                "</project>");
        runGit(workingCopyDir, "commit", "-a", "-m", "Make it fail");
        
        xmlRpcHelper.waitForProjectToInitialise(random);

        browser.loginAsAdmin();
        long buildNumber = runPersonalBuild(ResultState.FAILURE);
        browser.openAndWaitFor(PersonalBuildSummaryPage.class, buildNumber);
        assertTextPresent("Force build failure");
        
        PersonalBuildChangesPage changesPage = browser.openAndWaitFor(PersonalBuildChangesPage.class, buildNumber);
        assertEquals("0f267c3c48939fd51dacbbddcf15f530f82f1523", changesPage.getCheckedOutRevision());
        assertEquals(DEFAULT_ANT_BUILD_FILE, changesPage.getChangedFile(0));
    }

    private void runGit(File working, String... args) throws IOException
    {
        List<String> command = new LinkedList<String>();
        command.add("git");
        command.addAll(asList(args));

        ProcessBuilder pd = new ProcessBuilder(command);
        pd.redirectErrorStream(true);
        if (working != null)
        {
            pd.directory(working);
        }

        SystemUtils.runCommandWithInput(null, pd);
    }

    public void testPerforcePersonalBuild() throws Exception
    {
        xmlRpcHelper.insertSingleCommandProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, PerforceUtils.createSpecConfig(xmlRpcHelper), xmlRpcHelper.getAntConfig());
        runPerforcePersonalBuild(DEFAULT_ANT_BUILD_FILE, PerforceUtils.WORKSPACE_PREFIX + random, null);
    }

    public void testPerforcePersonalBuildRemappedFile() throws Exception
    {
        xmlRpcHelper.insertSingleCommandProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, PerforceUtils.createViewConfig(xmlRpcHelper, PerforceUtils.MAPPED_VIEW), xmlRpcHelper.getAntConfig("mapped/build.xml"));
        runPerforcePersonalBuild(DEFAULT_ANT_BUILD_FILE, PerforceUtils.WORKSPACE_PREFIX + random, null);
    }

    public void testPerforcePersonalBuildComplexClientOnDeveloperSide() throws Exception
    {
        buildRunner.setBase(new File(workingCopyDir, "trunk"));
        xmlRpcHelper.insertSingleCommandProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, PerforceUtils.createViewConfig(xmlRpcHelper, PerforceUtils.TRIVIAL_VIEW), xmlRpcHelper.getAntConfig(DEFAULT_ANT_BUILD_FILE));
        String clientName = PerforceUtils.WORKSPACE_PREFIX + random;
        runPerforcePersonalBuild("trunk/build.xml", clientName, "//depot/triviant/trunk/... //" + clientName + "/trunk/...");
    }

    public void testPerforcePersonalAddedAndDeletedFiles() throws Exception
    {
        xmlRpcHelper.insertSingleCommandProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, PerforceUtils.createViewConfig(xmlRpcHelper, PerforceUtils.MAPPED_VIEW), xmlRpcHelper.getAntConfig("mapped/newbuild.xml"));
        editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, random);

        PerforceCore core = PerforceUtils.createCore();
        core.createOrUpdateWorkspace(PerforceUtils.P4CLIENT, PerforceUtils.WORKSPACE_PREFIX + random, "Test workspace", workingCopyDir.getAbsolutePath(), null);
        try
        {
            core.setEnv(ENV_CLIENT, PerforceUtils.WORKSPACE_PREFIX + random);
            core.runP4(null, P4_COMMAND, COMMAND_SYNC);

            File originalBuildFile = new File(workingCopyDir, DEFAULT_ANT_BUILD_FILE);
            File newBuildFile = new File(workingCopyDir, "newbuild.xml");
            FileSystemUtils.copy(newBuildFile, originalBuildFile);

            core.runP4(null, P4_COMMAND, COMMAND_DELETE, originalBuildFile.getAbsolutePath());
            core.runP4(null, P4_COMMAND, COMMAND_ADD, newBuildFile.getAbsolutePath());
            createConfigFile(random, asPair(PROPERTY_CLIENT, PerforceUtils.WORKSPACE_PREFIX + random), asPair(PROPERTY_PORT, P4PORT), asPair(PROPERTY_USER, P4USER), asPair(PROPERTY_PASSWORD, P4PASSWD));

            browser.loginAsAdmin();
            long buildNumber = runPersonalBuild(ResultState.SUCCESS);
            // An unclean patch will raise warnings.
            Hashtable<String, Object> build = xmlRpcHelper.getPersonalBuild((int) buildNumber);
            assertEquals(0, build.get("warningCount"));
        }
        finally
        {
            PerforceUtils.deleteAllPulseWorkspaces(core);
        }
    }
    
    private void runPerforcePersonalBuild(String buildFilePath, String clientName, String developerClientMapping) throws Exception
    {
        editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, random);

        PerforceCore core = PerforceUtils.createCore();
        core.createOrUpdateWorkspace(PerforceUtils.P4CLIENT, clientName, "Test workspace", workingCopyDir.getAbsolutePath(), developerClientMapping);
        try
        {
            core.setEnv(ENV_CLIENT, clientName);
            core.runP4(null, P4_COMMAND, COMMAND_SYNC);
            core.runP4(null, P4_COMMAND, COMMAND_EDIT, new File(workingCopyDir, buildFilePath).getAbsolutePath());
            makeChangeToBuildFile(buildFilePath);
            createConfigFile(random, asPair(PROPERTY_CLIENT, clientName), asPair(PROPERTY_PORT, P4PORT), asPair(PROPERTY_USER, P4USER), asPair(PROPERTY_PASSWORD, P4PASSWD));

            browser.loginAsAdmin();
            long buildNumber = runPersonalBuild(ResultState.FAILURE);
            verifyPersonalBuildTabs(random, buildNumber, buildFilePath);
        }
        finally
        {
            PerforceUtils.deleteAllPulseWorkspaces(core);
        }
    }

    public void testUnifiedPatch() throws Exception
    {
        xmlRpcHelper.insertSingleCommandProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.FAIL_ANT_REPOSITORY), xmlRpcHelper.getAntConfig());

        File patchFile = copyInputToDirectory("txt", workingCopyDir);
        // Specify a revision and a patch file and no working copy should be
        // required.
        createConfigFile(random,
                asPair(PersonalBuildConfig.PROPERTY_REVISION, WorkingCopy.REVISION_FLOATING),
                asPair(PersonalBuildConfig.PROPERTY_PATCH_FILE, patchFile.getAbsolutePath()));

        browser.loginAsAdmin();
        editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, random);
        long buildNumber = runPersonalBuild(ResultState.FAILURE);

        browser.openAndWaitFor(PersonalBuildSummaryPage.class, buildNumber);
        assertTextPresent("unified diffs will sink you");

        PersonalBuildChangesPage changesPage = browser.openAndWaitFor(PersonalBuildChangesPage.class, buildNumber);
        assertEquals(DEFAULT_ANT_BUILD_FILE, changesPage.getChangedFile(0));
    }

    public void testPatchToVersionedPulseFile() throws Exception
    {
        checkout(Constants.VERSIONED_REPOSITORY);
        File patchFile = copyInputToDirectory("txt", workingCopyDir);
        createConfigFile(random, asPair(PersonalBuildConfig.PROPERTY_PATCH_FILE, patchFile.getAbsolutePath()));

        browser.loginAsAdmin();

        xmlRpcHelper.insertProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.VERSIONED_REPOSITORY), xmlRpcHelper.createVersionedConfig(Constants.VERSIONED_PULSE_FILE));
        editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, random);
        long buildNumber = runPersonalBuild(ResultState.ERROR);
        browser.openAndWaitFor(PersonalBuildSummaryPage.class, buildNumber);
        assertTextPresent("nosuchrecipe");
        
        browser.openAndWaitFor(PersonalBuildFilePage.class, buildNumber);
        assertTextPresent("default-recipe=\"nosuchrecipe\"");
    }

    private Hashtable<String, Object> insertHook(String hooksPath, Class<? extends BuildHookConfiguration> hookClass, String name, boolean runForPersonal) throws Exception
    {
        Hashtable<String, Object> hook = xmlRpcHelper.createEmptyConfig(hookClass);
        hook.put("name", name);
        hook.put("runForPersonal", runForPersonal);
        xmlRpcHelper.insertConfig(hooksPath, hook);
        return hook;
    }

    private String getLogText(String projectName, long buildNumber)
    {
        PersonalBuildLogPage page = browser.openAndWaitFor(PersonalBuildLogPage.class, projectName, buildNumber);
        return page.getLog();
    }

    private String getLogText(String projectName, long buildNumber, String stageName)
    {
        PersonalBuildLogsPage page = browser.openAndWaitFor(PersonalBuildLogsPage.class, projectName, buildNumber, stageName);
        return page.getLog();
    }

    private void checkout(String url) throws SVNException
    {
        SubversionWorkspace workspace = new SubversionWorkspace(workingCopyDir, "pulse", "pulse");
        workspace.doCheckout(url);
    }

    private void makeChangeToBuildFile() throws IOException
    {
        makeChangeToBuildFile(DEFAULT_ANT_BUILD_FILE);
    }

    private void makeChangeToBuildFile(String path) throws IOException
    {
        // Edit the build.xml file so we have an outstanding change
        File buildFile = new File(workingCopyDir, path);
        String target = RandomUtils.randomString(10);
        FileSystemUtils.createFile(buildFile, "<?xml version=\"1.0\"?>\n" +
                "<project default=\"" + target + "\">\n" +
                "    <target name=\"" + target + "\">\n" +
                "        <nosuchcommand/>\n" +
                "    </target>\n" +
                "</project>");
    }

    private void makeChangeToImportedFile() throws IOException
    {
        File includedFile = new File(workingCopyDir, "properties.xml");
        FileSystemUtils.createFile(includedFile, "<?xml version=\"1.0\"?>\n" +
                "<project><notrecognised/></project>\n");
    }

    private void editStageToRunOnAgent(String agent, String projectName) throws Exception
    {
        String stagePath = PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, projectName, "stages", "default");
        Hashtable<String, Object> stage = xmlRpcHelper.getConfig(stagePath);
        stage.put("agent", PathUtils.getPath(MasterConfigurationRegistry.AGENTS_SCOPE, agent));
        xmlRpcHelper.saveConfig(stagePath, stage, false);
    }

    private long runPersonalBuild(ResultState expectedStatus) throws IOException, PersonalBuildException
    {
        // Request the build and wait for it to complete
        AcceptancePersonalBuildUI ui = requestPersonalBuild();

        List<String> statuses = ui.getStatusMessages();
        assertTrue(statuses.size() > 0);
        assertTrue("Patch not accepted given status:\n" + StringUtils.join("\n", statuses), ui.isPatchAccepted());

        long buildNumber = ui.getBuildNumber();
        browser.openAndWaitFor(MyBuildsPage.class);
        browser.refreshUntilElement(MyBuildsPage.getBuildNumberId(buildNumber));
        assertElementNotPresent(MyBuildsPage.getBuildNumberId(buildNumber + 1));
        browser.refreshUntilText(MyBuildsPage.getBuildStatusId(buildNumber), BUILD_TIMEOUT, expectedStatus.getPrettyString());
        return buildNumber;
    }

    private void createConfigFile(String projectName, Pair<String, ?>... extraProperties) throws IOException
    {
        buildRunner.createConfigFile(browser.getBaseUrl(), ADMIN_CREDENTIALS.getUserName(), ADMIN_CREDENTIALS.getPassword(), projectName, extraProperties);
    }

    private AcceptancePersonalBuildUI requestPersonalBuild() throws IOException, PersonalBuildException
    {
        return buildRunner.triggerBuild();
    }

    private void verifyPersonalBuildTabs(String projectName, long buildNumber, String buildFilePath)
    {
        // Verify each tab in turn
        browser.openAndWaitFor(PersonalBuildSummaryPage.class, buildNumber);
        assertTextPresent("nosuchcommand");

        browser.click(IDs.buildLogsTab());
        BuildLogsPage logsPage = browser.createPage(BuildLogsPage.class, projectName, buildNumber, "default");
        logsPage.waitFor();
        assertTextPresent("Recipe '[default]' completed with status failure");
        
        browser.click(IDs.buildDetailsTab());
        PersonalBuildDetailsPage detailsPage = browser.createPage(PersonalBuildDetailsPage.class, buildNumber);
        detailsPage.waitFor();
        detailsPage.clickCommandAndWait("default", "build");
        assertTextPresent("nosuchcommand");

        browser.click(IDs.buildChangesTab());
        PersonalBuildChangesPage changesPage = browser.createPage(PersonalBuildChangesPage.class, buildNumber);
        changesPage.waitFor();
        // Just parse to make sure it's a number: asserting the revision has
        // proven too fragile.
        Long.parseLong(changesPage.getCheckedOutRevision());
        assertEquals(buildFilePath, changesPage.getChangedFile(0));

        browser.click(IDs.buildTestsTab());
        PersonalBuildTestsPage testsPage = browser.createPage(PersonalBuildTestsPage.class, buildNumber);
        testsPage.waitFor();
        assertEquals(0, testsPage.getTestSummary().getTotal());

        browser.click(IDs.buildFileTab());
        PersonalBuildFilePage filePage = browser.createPage(PersonalBuildFilePage.class, buildNumber);
        filePage.waitFor();
        assertTrue(filePage.isHighlightedFilePresent());
        assertTextPresent("<ant");

        PersonalBuildArtifactsPage artifactsPage = browser.openAndWaitFor(PersonalBuildArtifactsPage.class, buildNumber);
        artifactsPage.setFilterAndWait("");
        browser.waitForLocator(artifactsPage.getCommandLocator("build"));
    }
}
