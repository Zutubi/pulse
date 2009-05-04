package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.dashboard.*;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.scm.WorkingCopyFactory;
import com.zutubi.pulse.core.scm.api.PersonalBuildUI;
import com.zutubi.pulse.core.scm.svn.SubversionWorkingCopy;
import com.zutubi.pulse.dev.personal.PersonalBuildClient;
import com.zutubi.pulse.dev.personal.PersonalBuildCommand;
import com.zutubi.pulse.dev.personal.PersonalBuildConfig;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.ConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.PostBuildHookConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.PostStageHookConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.PreBuildHookConfiguration;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
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
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Simple sanity checks for personal builds.
 */
@Test(dependsOnGroups = {"init.*"})
public class PersonalBuildAcceptanceTest extends SeleniumTestBase
{
    private static final String PROJECT_NAME = "PersonalBuildAcceptanceTest-Project";

    private File workingCopyDir;

    @BeforeMethod
    protected void setUp() throws Exception
    {
        super.setUp();

        WorkingCopyFactory.registerType("svn", SubversionWorkingCopy.class);
        workingCopyDir = FileSystemUtils.createTempDir("PersonalBuildAcceptanceTest", "");

        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();

        xmlRpcHelper.loginAsAdmin();
    }

    @AfterMethod
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
        long buildNumber = runPersonalBuild("failure");
        verifyPersonalBuildTabs(buildNumber, AgentManager.MASTER_AGENT_NAME);

        PersonalEnvironmentArtifactPage envPage = new PersonalEnvironmentArtifactPage(selenium, urls, PROJECT_NAME, buildNumber, "default", "build");
        envPage.goTo();
        assertTrue(envPage.isPropertyPresentWithValue(BuildProperties.PROPERTY_INCREMENTAL_BOOTSTRAP, Boolean.toString(false)));
        assertTrue(envPage.isPropertyPresentWithValue(BuildProperties.PROPERTY_LOCAL_BUILD, Boolean.toString(false)));
        assertTrue(envPage.isPropertyPresentWithValue(BuildProperties.PROPERTY_PERSONAL_BUILD, Boolean.toString(true)));
        // Make sure this view is not decorated (CIB-1711).
        assertTextNotPresent("logout");
    }

    public void testPersonalBuildChangesImportedFile() throws Exception
    {
        checkout(Constants.VERSIONED_REPOSITORY);
        makeChangeToImportedFile();
        createConfigFile(random);
        loginAsAdmin();

        xmlRpcHelper.insertProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.VERSIONED_REPOSITORY), xmlRpcHelper.createVersionedConfig("pulse/pulse.xml"));
        editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME, random);
        long buildNumber = runPersonalBuild("error");
        PersonalBuildSummaryPage summaryPage = new PersonalBuildSummaryPage(selenium, urls, buildNumber);
        summaryPage.goTo();
        assertTextPresent("Undefined type 'notrecognised'");
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
        long buildNumber = runPersonalBuild("failure");
        verifyPersonalBuildTabs(buildNumber, AGENT_NAME);
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
        long buildNumber = runPersonalBuild("failure");

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
        selenium.open(url + "raw/true");
        selenium.waitForPageToLoad(Long.toString(SeleniumUtils.DEFAULT_TIMEOUT));
        return selenium.getBodyText();
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
        FileSystemUtils.createFile(buildFile, "<?xml version=\"1.0\"?>\n" +
                "<project default=\"" + random + "\">\n" +
                "    <target name=\"" + random + "\">\n" +
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
        String stagePath = PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "stages", "default");
        Hashtable<String, Object> stage = xmlRpcHelper.getConfig(stagePath);
        stage.put("agent", PathUtils.getPath(ConfigurationRegistry.AGENTS_SCOPE, agent));
        xmlRpcHelper.saveConfig(stagePath, stage, false);
    }

    private long runPersonalBuild(String expectedStatus)
    {
        // Request the build and wait for it to complete
        TestPersonalBuildUI ui = requestPersonalBuild();

        List<String> warnings = ui.getWarnings();
        assertTrue("Got warnings: " + StringUtils.join("\n", warnings), warnings.isEmpty());
        List<String> errors = ui.getErrors();
        assertTrue("Got errors: " + StringUtils.join("\n", errors), errors.isEmpty());

        List<String> statuses = ui.getStatuses();
        assertTrue(statuses.size() > 0);
        assertTrue("Patch not accepted given status:\n" + StringUtils.join("\n", statuses), ui.isPatchAccepted());

        long buildNumber = ui.getBuildNumber();
        MyBuildsPage myBuildsPage = new MyBuildsPage(selenium, urls);
        myBuildsPage.goTo();
        SeleniumUtils.refreshUntilElement(selenium, MyBuildsPage.getBuildNumberId(buildNumber));
        assertElementNotPresent(MyBuildsPage.getBuildNumberId(buildNumber + 1));
        SeleniumUtils.refreshUntilText(selenium, MyBuildsPage.getBuildStatusId(buildNumber), expectedStatus);
        return buildNumber;
    }

    private void createConfigFile(String projectName) throws IOException
    {
        File configFile = new File(workingCopyDir, PersonalBuildConfig.PROPERTIES_FILENAME);
        Properties config = new Properties();
        config.put(PersonalBuildConfig.PROPERTY_PULSE_URL, baseUrl);
        config.put(PersonalBuildConfig.PROPERTY_PULSE_USER, "admin");
        config.put(PersonalBuildConfig.PROPERTY_PULSE_PASSWORD, "admin");
        config.put(PersonalBuildConfig.PROPERTY_PROJECT, projectName);

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

    private TestPersonalBuildUI requestPersonalBuild()
    {
        PersonalBuildConfig config = new PersonalBuildConfig(workingCopyDir, null);
        PersonalBuildClient client = new PersonalBuildClient(config);
        TestPersonalBuildUI ui = new TestPersonalBuildUI();
        client.setUI(ui);

        PersonalBuildCommand command = new PersonalBuildCommand();
        command.execute(client);

        return ui;
    }

    private void verifyPersonalBuildTabs(long buildNumber, String agent)
    {
        // Verify each tab in turn
        PersonalBuildSummaryPage summaryPage = new PersonalBuildSummaryPage(selenium, urls, buildNumber);
        summaryPage.goTo();
        assertTextPresent("nosuchcommand");
        SeleniumUtils.assertText(selenium, IDs.stageAgentCell(PROJECT_NAME, buildNumber, "default"), agent);

        selenium.click(IDs.buildDetailsTab());
        PersonalBuildDetailedViewPage detailedViewPage = new PersonalBuildDetailedViewPage(selenium, urls, buildNumber);
        detailedViewPage.waitFor();
        detailedViewPage.clickCommand("default", "build");
        assertTextPresent("nosuchcommand");

        selenium.click(IDs.buildChangesTab());
        PersonalBuildChangesPage changesPage = new PersonalBuildChangesPage(selenium, urls, buildNumber);
        changesPage.waitFor();
        assertTrue(Long.parseLong(changesPage.getCheckedOutRevision()) >= 2);
        assertEquals("build.xml", changesPage.getChangedFile(0));

        selenium.click(IDs.buildTestsTab());
        PersonalBuildTestsPage testsPage = new PersonalBuildTestsPage(selenium, urls, buildNumber);
        testsPage.waitFor();
        assertTrue(testsPage.isBuildComplete());
        assertFalse(testsPage.hasTests());

        selenium.click(IDs.buildFileTab());
        PersonalBuildFilePage filePage = new PersonalBuildFilePage(selenium, urls, buildNumber);
        filePage.waitFor();
        assertTrue(filePage.isHighlightedFilePresent());
        assertTextPresent("ant build");

        PersonalBuildArtifactsPage artifactsPage = new PersonalBuildArtifactsPage(selenium, urls, buildNumber);
        artifactsPage.goTo();
        SeleniumUtils.waitForLocator(selenium, artifactsPage.getCommandLocator("build"));

        selenium.click(IDs.buildWorkingCopyTab());
        PersonalBuildWorkingCopyPage wcPage = new PersonalBuildWorkingCopyPage(selenium, urls, buildNumber);
        wcPage.waitFor();
        assertTrue(wcPage.isWorkingCopyNotPresent());
    }

    private static class TestPersonalBuildUI implements PersonalBuildUI
    {
        private List<String> debugs = new LinkedList<String>();
        private List<String> statuses = new LinkedList<String>();
        private List<String> warnings = new LinkedList<String>();
        private List<String> errors = new LinkedList<String>();
        private long buildNumber = -1;

        public List<String> getDebugs()
        {
            return debugs;
        }

        public List<String> getStatuses()
        {
            return statuses;
        }

        public List<String> getWarnings()
        {
            return warnings;
        }

        public List<String> getErrors()
        {
            return errors;
        }

        public boolean isPatchAccepted()
        {
            return buildNumber > 0;
        }

        public long getBuildNumber()
        {
            return buildNumber;
        }

        public void debug(String message)
        {
            debugs.add(message);
        }

        public void status(String message)
        {
            if (message.startsWith("Patch accepted"))
            {
                String[] pieces = message.split(" ");
                String number = pieces[pieces.length - 1];
                number = number.substring(0, number.length() - 1);
                buildNumber = Long.parseLong(number);
            }

            statuses.add(message);
        }

        public void warning(String message)
        {
            warnings.add(message);
        }

        public void error(String message)
        {
            errors.add(message);
        }

        public void error(String message, Throwable throwable)
        {
            errors.add(message);
        }

        public void enterContext()
        {
        }

        public void exitContext()
        {
        }

        public String inputPrompt(String question)
        {
            return "";
        }

        public String inputPrompt(String prompt, String defaultResponse)
        {
            return defaultResponse;
        }

        public String passwordPrompt(String question)
        {
            return "";
        }

        public Response ynPrompt(String question, Response defaultResponse)
        {
            return defaultResponse;
        }

        public Response ynaPrompt(String question, Response defaultResponse)
        {
            return defaultResponse;
        }
    }
}
