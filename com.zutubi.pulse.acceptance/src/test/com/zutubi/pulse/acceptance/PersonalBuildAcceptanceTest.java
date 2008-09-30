package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.dashboard.*;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.core.scm.WorkingCopyFactory;
import com.zutubi.pulse.core.scm.svn.SubversionWorkingCopy;
import com.zutubi.pulse.personal.PersonalBuildClient;
import com.zutubi.pulse.personal.PersonalBuildCommand;
import com.zutubi.pulse.personal.PersonalBuildConfig;
import com.zutubi.pulse.personal.PersonalBuildUI;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.tove.config.ConfigurationRegistry;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.io.IOUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
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
        SVNUpdateClient client = new SVNUpdateClient(SVNWCUtil.createDefaultAuthenticationManager(), null);
        client.doCheckout(SVNURL.parseURIDecoded("svn://localhost:3088/accept/trunk/triviant"), workingCopyDir, SVNRevision.HEAD, SVNRevision.HEAD, true);

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
        makeChange();
        createConfigFile();
        loginAsAdmin();
        ensureProject(PROJECT_NAME);
        editStageToRunOnAgent(AgentManager.MASTER_AGENT_NAME);
        long buildNumber = runPersonalBuild();
        verifyPersonalBuildTabs(buildNumber, AgentManager.MASTER_AGENT_NAME);
    }

    public void testPersonalBuildOnAgent() throws Exception
    {
        makeChange();
        createConfigFile();
        loginAsAdmin();
        ensureAgent(AGENT_NAME);
        ensureProject(PROJECT_NAME);
        editStageToRunOnAgent(AGENT_NAME);
        long buildNumber = runPersonalBuild();
        verifyPersonalBuildTabs(buildNumber, AGENT_NAME);
    }

    private void makeChange() throws IOException
    {
        // Edit the build.xml file so we have an outstanding change
        File buildFile = new File(workingCopyDir, "build.xml");
        String fileContents = IOUtils.fileToString(buildFile);
        fileContents = fileContents.replaceAll("sleep", "nosuchcommand");
        FileSystemUtils.createFile(buildFile, fileContents);
    }

    private void editStageToRunOnAgent(String agent) throws Exception
    {
        String stagePath = PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, PROJECT_NAME, "stages", "default");
        Hashtable<String, Object> stage = xmlRpcHelper.getConfig(stagePath);
        stage.put("agent", PathUtils.getPath(ConfigurationRegistry.AGENTS_SCOPE, agent));
        xmlRpcHelper.saveConfig(stagePath, stage, false);
    }

    private long runPersonalBuild()
    {
        // Request the build and wait for it to complete
        TestPersonalBuildUI ui = requestPersonalBuild();

        assertTrue(ui.getStatuses().size() > 0);
        assertTrue(ui.isPatchAccepted());
        assertEquals(0, ui.getWarnings().size());
        assertEquals(0, ui.getErrors().size());

        long buildNumber = ui.getBuildNumber();
        MyBuildsPage myBuildsPage = new MyBuildsPage(selenium, urls);
        myBuildsPage.goTo();
        assertElementPresent(MyBuildsPage.getBuildNumberId(buildNumber));
        assertElementNotPresent(MyBuildsPage.getBuildNumberId(buildNumber + 1));
        SeleniumUtils.refreshUntilText(selenium, MyBuildsPage.getBuildStatusId(buildNumber), "failure");
        return buildNumber;
    }

    private void createConfigFile() throws IOException
    {
        File configFile = new File(workingCopyDir, PersonalBuildConfig.PROPERTIES_FILENAME);
        Properties config = new Properties();
        config.put(PersonalBuildConfig.PROPERTY_PULSE_URL, baseUrl);
        config.put(PersonalBuildConfig.PROPERTY_PULSE_USER, "admin");
        config.put(PersonalBuildConfig.PROPERTY_PULSE_PASSWORD, "admin");
        config.put(PersonalBuildConfig.PROPERTY_PROJECT, PROJECT_NAME);

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
        assertEquals("2", changesPage.getCheckedOutRevision());
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

        public void setVerbosity(Verbosity verbosity)
        {
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
