package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.BuildLogsPage;
import com.zutubi.pulse.acceptance.pages.dashboard.*;
import com.zutubi.pulse.acceptance.support.PerforceUtils;
import static com.zutubi.pulse.acceptance.support.PerforceUtils.*;
import com.zutubi.pulse.acceptance.support.ProxyServer;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.patchformats.unified.UnifiedPatchFormat;
import com.zutubi.pulse.core.personal.TestPersonalBuildUI;
import com.zutubi.pulse.core.scm.WorkingCopyFactory;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.WorkingCopy;
import com.zutubi.pulse.core.scm.git.GitPatchFormat;
import com.zutubi.pulse.core.scm.git.GitWorkingCopy;
import com.zutubi.pulse.core.scm.p4.PerforceClient;
import static com.zutubi.pulse.core.scm.p4.PerforceConstants.*;
import com.zutubi.pulse.core.scm.p4.PerforceCore;
import com.zutubi.pulse.core.scm.p4.PerforceWorkingCopy;
import com.zutubi.pulse.core.scm.patch.DefaultPatchFormatFactory;
import com.zutubi.pulse.core.scm.svn.SubversionClient;
import com.zutubi.pulse.core.scm.svn.SubversionWorkingCopy;
import com.zutubi.pulse.dev.personal.PersonalBuildClient;
import com.zutubi.pulse.dev.personal.PersonalBuildCommand;
import com.zutubi.pulse.dev.personal.PersonalBuildConfig;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;
import com.zutubi.pulse.master.tove.config.project.hooks.*;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.*;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.io.IOUtils;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.util.Arrays.asList;
import java.util.*;

/**
 * Simple sanity checks for personal builds.
 */
public class PersonalBuildAcceptanceTest extends SeleniumTestBase
{
    private static final String PROJECT_NAME = "PersonalBuildAcceptanceTest-Project";
    private static final int BUILD_TIMEOUT = 90000;

    private File workingCopyDir;

    protected void setUp() throws Exception
    {
        super.setUp();

        WorkingCopyFactory.registerType("svn", SubversionWorkingCopy.class);
        WorkingCopyFactory.registerType("git", GitWorkingCopy.class);
        WorkingCopyFactory.registerType("p4", PerforceWorkingCopy.class);
        workingCopyDir = FileSystemUtils.createTempDir("PersonalBuildAcceptanceTest", "");

        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();

        xmlRpcHelper.loginAsAdmin();
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

        loginAsAdmin();
        ensureProject(PROJECT_NAME);
        editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, PROJECT_NAME);
        long buildNumber = runPersonalBuild(ResultState.FAILURE);
        verifyPersonalBuildTabs(PROJECT_NAME, buildNumber, AgentManager.MASTER_AGENT_NAME);

        PersonalEnvironmentArtifactPage envPage = browser.openAndWaitFor(PersonalEnvironmentArtifactPage.class, PROJECT_NAME, buildNumber, "default", "build");
        assertTrue(envPage.isPropertyPresentWithValue(BuildProperties.PROPERTY_INCREMENTAL_BOOTSTRAP, Boolean.toString(false)));
        assertTrue(envPage.isPropertyPresentWithValue(BuildProperties.PROPERTY_LOCAL_BUILD, Boolean.toString(false)));
        assertTrue(envPage.isPropertyPresentWithValue(BuildProperties.PROPERTY_PERSONAL_BUILD, Boolean.toString(true)));
        assertTrue(envPage.isPropertyPresentWithValue(BuildProperties.PROPERTY_OWNER, "admin"));
        assertTrue(envPage.isPropertyPresentWithValue(BuildProperties.PROPERTY_PERSONAL_USER, "admin"));
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

            loginAsAdmin();
            ensureProject(PROJECT_NAME);
            editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, PROJECT_NAME);
            long buildNumber = runPersonalBuild(ResultState.FAILURE);
            verifyPersonalBuildTabs(PROJECT_NAME, buildNumber, AgentManager.MASTER_AGENT_NAME);
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
        loginAsAdmin();

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

        loginAsAdmin();
        ensureAgent(AGENT_NAME);
        ensureProject(PROJECT_NAME);
        editStageToRunOnAgent(AGENT_NAME, PROJECT_NAME);
        long buildNumber = runPersonalBuild(ResultState.FAILURE);
        verifyPersonalBuildTabs(PROJECT_NAME, buildNumber, AGENT_NAME);
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

        loginAsAdmin();
        editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, random);
        long buildNumber = runPersonalBuild(ResultState.FAILURE);

        // Finally check that only the enabled hooks ran.
        String text = getLogText(urls.dashboardMyBuildLog(Long.toString(buildNumber)));
        assertFalse("Pre-build hook not for personal should not have run", text.contains("prebuildno"));
        assertTrue("Pre-build hook for personal should have run", text.contains("prebuildyes"));
        assertFalse("Post-build hook not for personal should not have run", text.contains("postbuildno"));
        assertTrue("Post-build hook for personal should have run", text.contains("postbuildyes"));

        text = getLogText(urls.dashboardMyStageLog(Long.toString(buildNumber), ProjectConfigurationWizard.DEFAULT_STAGE));
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

        loginAsAdmin();
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

        loginAsAdmin();
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

        loginAsAdmin();
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

        File buildFile = new File(workingCopyDir, "build.xml");
        FileSystemUtils.createFile(buildFile, "<?xml version=\"1.0\"?>\n" +
                "<project default=\"build\">\n" +
                "  <target name=\"build\">\n" +
                "    <fail message=\"Force build failure\"/>\n" +
                "  </target>\n" +
                "</project>");
        runGit(workingCopyDir, "commit", "-a", "-m", "Make it fail");
        
        xmlRpcHelper.waitForProjectToInitialise(random);

        loginAsAdmin();
        long buildNumber = runPersonalBuild(ResultState.FAILURE);
        browser.openAndWaitFor(PersonalBuildSummaryPage.class, buildNumber);
        assertTextPresent("Force build failure");
        
        PersonalBuildChangesPage changesPage = browser.openAndWaitFor(PersonalBuildChangesPage.class, buildNumber);
        assertEquals("0f267c3c48939fd51dacbbddcf15f530f82f1523", changesPage.getCheckedOutRevision());
        assertEquals("build.xml", changesPage.getChangedFile(0));
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
        runPerforcePersonalBuild();
    }

    public void testPerforcePersonalBuildRemappedFile() throws Exception
    {
        xmlRpcHelper.insertSingleCommandProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, PerforceUtils.createViewConfig(xmlRpcHelper, PerforceUtils.MAPPED_VIEW), xmlRpcHelper.getAntConfig("mapped/build.xml"));
        runPerforcePersonalBuild();
    }

    private void runPerforcePersonalBuild() throws Exception
    {
        editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, random);

        PerforceCore core = PerforceUtils.createCore();
        String clientName = PerforceUtils.WORKSPACE_PREFIX + random;
        core.createOrUpdateWorkspace(PerforceUtils.P4CLIENT, clientName, "Test workspace", workingCopyDir.getAbsolutePath(), null);
        try
        {
            core.setEnv(ENV_CLIENT, clientName);
            core.runP4(null, P4_COMMAND, COMMAND_SYNC);
            core.runP4(null, P4_COMMAND, COMMAND_EDIT, new File(workingCopyDir, "build.xml").getAbsolutePath());
            makeChangeToBuildFile();
            createConfigFile(random, asPair(PROPERTY_CLIENT, clientName), asPair(PROPERTY_PORT, P4PORT), asPair(PROPERTY_USER, P4USER), asPair(PROPERTY_PASSWORD, P4PASSWD));

            loginAsAdmin();
            long buildNumber = runPersonalBuild(ResultState.FAILURE);
            verifyPersonalBuildTabs(random, buildNumber, AgentManager.MASTER_AGENT_NAME);
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

        loginAsAdmin();
        editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, random);
        long buildNumber = runPersonalBuild(ResultState.FAILURE);

        browser.openAndWaitFor(PersonalBuildSummaryPage.class, buildNumber);
        assertTextPresent("unified diffs will sink you");

        PersonalBuildChangesPage changesPage = browser.openAndWaitFor(PersonalBuildChangesPage.class, buildNumber);
        assertEquals("build.xml", changesPage.getChangedFile(0));
    }

    public void testPatchToVersionedPulseFile() throws Exception
    {
        checkout(Constants.VERSIONED_REPOSITORY);
        File patchFile = copyInputToDirectory("txt", workingCopyDir);
        createConfigFile(random, asPair(PersonalBuildConfig.PROPERTY_PATCH_FILE, patchFile.getAbsolutePath()));

        loginAsAdmin();

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

    private String getLogText(String url)
    {
        browser.open(url);
        browser.waitForPageToLoad();
        return browser.getText("panel");
    }

    private void checkout(String url) throws SVNException
    {
        SVNUpdateClient client = new SVNUpdateClient(SVNWCUtil.createDefaultAuthenticationManager(), null);
        client.doCheckout(SVNURL.parseURIDecoded(url), workingCopyDir, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, false);
    }

    private void makeChangeToBuildFile() throws IOException
    {
        // Edit the build.xml file so we have an outstanding change
        File buildFile = new File(workingCopyDir, "build.xml");
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

    private long runPersonalBuild(ResultState expectedStatus)
    {
        // Request the build and wait for it to complete
        AcceptancePersonalBuildUI ui = requestPersonalBuild();

        List<String> warnings = ui.getWarningMessages();
        assertTrue("Got warnings: " + StringUtils.join("\n", warnings), warnings.isEmpty());
        List<String> errors = ui.getErrorMessages();
        assertTrue("Got errors: " + StringUtils.join("\n", errors), errors.isEmpty());

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
        File configFile = new File(workingCopyDir, PersonalBuildConfig.PROPERTIES_FILENAME);
        Properties config = new Properties();
        config.put(PersonalBuildConfig.PROPERTY_PULSE_URL, browser.getBaseUrl());
        config.put(PersonalBuildConfig.PROPERTY_PULSE_USER, "admin");
        config.put(PersonalBuildConfig.PROPERTY_PULSE_PASSWORD, "admin");
        config.put(PersonalBuildConfig.PROPERTY_PROJECT, projectName);

        for (Pair<String, ?> extra: extraProperties)
        {
            config.put(extra.first, extra.second.toString());
        }

        FileOutputStream os = null;
        try
        {
            os = new FileOutputStream(configFile);
            config.store(os, null);
        }
        finally
        {
            IOUtils.close(os);
        }
    }

    private AcceptancePersonalBuildUI requestPersonalBuild()
    {
        AcceptancePersonalBuildUI ui = new AcceptancePersonalBuildUI();
        PersonalBuildConfig config = new PersonalBuildConfig(workingCopyDir, ui);

        DefaultPatchFormatFactory patchFormatFactory = new DefaultPatchFormatFactory();
        patchFormatFactory.registerScm(SubversionClient.TYPE, DefaultPatchFormatFactory.FORMAT_STANDARD);
        patchFormatFactory.registerFormatType("git", GitPatchFormat.class);
        patchFormatFactory.registerScm("git", "git");
        patchFormatFactory.registerFormatType("unified", UnifiedPatchFormat.class);
        patchFormatFactory.registerScm(PerforceClient.TYPE, DefaultPatchFormatFactory.FORMAT_STANDARD);
        patchFormatFactory.setObjectFactory(new DefaultObjectFactory());

        PersonalBuildClient client = new PersonalBuildClient(config, ui);
        client.setPatchFormatFactory(patchFormatFactory);

        PersonalBuildCommand command = new PersonalBuildCommand();
        command.execute(client);
        return ui;
    }

    private void verifyPersonalBuildTabs(String projectName, long buildNumber, String agent)
    {
        // Verify each tab in turn
        browser.openAndWaitFor(PersonalBuildSummaryPage.class, buildNumber);
        assertTextPresent("nosuchcommand");
        assertEquals(agent, browser.getText(IDs.stageAgentCell(projectName, buildNumber, "default")));

        browser.click(IDs.buildLogsTab());
        BuildLogsPage logsPage = browser.createPage(BuildLogsPage.class, projectName, buildNumber, "default");
        logsPage.waitFor();
        assertTextPresent("Recipe '[default]' completed with status failure");
        
        browser.click(IDs.buildDetailsTab());
        PersonalBuildDetailedViewPage detailedViewPage = browser.createPage(PersonalBuildDetailedViewPage.class, buildNumber);
        detailedViewPage.waitFor();
        detailedViewPage.clickCommand("default", "build");
        assertTextPresent("nosuchcommand");

        browser.click(IDs.buildChangesTab());
        PersonalBuildChangesPage changesPage = browser.createPage(PersonalBuildChangesPage.class, buildNumber);
        changesPage.waitFor();
        // Just parse to make sure it's a number: asserting the revision has
        // proven too fragile.
        Long.parseLong(changesPage.getCheckedOutRevision());
        assertEquals("build.xml", changesPage.getChangedFile(0));

        browser.click(IDs.buildTestsTab());
        PersonalBuildTestsPage testsPage = browser.createPage(PersonalBuildTestsPage.class, buildNumber);
        testsPage.waitFor();
        assertTrue(testsPage.isBuildComplete());
        assertFalse(testsPage.hasTests());

        browser.click(IDs.buildFileTab());
        PersonalBuildFilePage filePage = browser.createPage(PersonalBuildFilePage.class, buildNumber);
        filePage.waitFor();
        assertTrue(filePage.isHighlightedFilePresent());
        assertTextPresent("<ant");

        PersonalBuildArtifactsPage artifactsPage = browser.openAndWaitFor(PersonalBuildArtifactsPage.class, buildNumber);
        browser.waitForLocator(artifactsPage.getCommandLocator("build"));
    }

    private static class AcceptancePersonalBuildUI extends TestPersonalBuildUI
    {
        private long buildNumber = -1;

        public boolean isPatchAccepted()
        {
            return buildNumber > 0;
        }

        public long getBuildNumber()
        {
            return buildNumber;
        }

        public void status(String message)
        {
            super.status(message);

            if (message.startsWith("Patch accepted"))
            {
                String[] pieces = message.split(" ");
                String number = pieces[pieces.length - 1];
                number = number.substring(0, number.length() - 1);
                buildNumber = Long.parseLong(number);
            }
        }

        public String inputPrompt(String question)
        {
            return "";
        }

        public String passwordPrompt(String question)
        {
            return "";
        }
    }
}
