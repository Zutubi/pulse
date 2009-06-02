package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.*;
import com.zutubi.pulse.acceptance.pages.dashboard.DashboardPage;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.tove.type.record.PathUtils;

import java.net.URL;
import java.util.Hashtable;

/**
 * Acceptance tests for taking/clearing responsibility for a build.
 */
public class BuildResponsibilityAcceptanceTest extends SeleniumTestBase
{
    private static final String TEST_PROJECT = "responsibility-test-project";
    private static final String TEST_USER = "responsibility-user";
    private static final String TEST_COMMENT = "a comment here";

    private static final int BUILD_NUMBER = 1;
    private static final long BUILD_TIMEOUT = 90000;
    private static final String LOAD_TIMEOUT ="30000";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        xmlRpcHelper.loginAsAdmin();
        xmlRpcHelper.ensureProject(TEST_PROJECT);
        xmlRpcHelper.ensureUser(TEST_USER);
        ensureBuild(TEST_PROJECT);
        xmlRpcHelper.clearResponsibility(TEST_PROJECT);
    }

    @Override
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testTakeResponsibility()
    {
        takeResponsibilityHelper(new ProjectHomePage(selenium, urls, TEST_PROJECT));

        // Build pages for this project should show the responsibility.
        BuildSummaryPage summaryPage = new BuildSummaryPage(selenium, urls, TEST_PROJECT, 1);
        summaryPage.goTo();
        assertSelfResponsible(summaryPage);

        // Responsibilities should appear on your dashboard.
        DashboardPage dashboardPage = new DashboardPage(selenium, urls);
        dashboardPage.goTo();

        assertTrue(dashboardPage.hasResponsibilities());
        assertTrue(dashboardPage.hasResponsibility(TEST_PROJECT));
        assertTrue(dashboardPage.isResponsibilityPresent(null, TEST_PROJECT));

        // Responsibilility icons should appear in the browse view
        BrowsePage browsePage = new BrowsePage(selenium, urls);
        browsePage.goTo();
        assertTrue(browsePage.isResponsibilityPresent(null, TEST_PROJECT));
    }

    public void testTakeResponsibilityOnBuildPage()
    {
        takeResponsibilityHelper(new BuildSummaryPage(selenium, urls, TEST_PROJECT, 1));
    }

    private void takeResponsibilityHelper(ResponsibilityPage page)
    {
        login(TEST_USER, "");
        page.goTo();
        assertNobodyResponsible(page);

        page.clickAction(ProjectConfigurationActions.ACTION_TAKE_RESPONSIBILITY);

        TakeResponsibilityDialog dialog = new TakeResponsibilityDialog(selenium);
        dialog.waitFor();
        dialog.typeComment(TEST_COMMENT);
        dialog.clickOk();

        selenium.waitForPageToLoad(LOAD_TIMEOUT);
        assertSelfResponsible(page);
    }

    public void testClearResponsibility() throws Exception
    {
        takeResponsibility(TEST_PROJECT);

        login(TEST_USER, "");

        // Clear on the project home tab
        ProjectHomePage homePage = new ProjectHomePage(selenium, urls, TEST_PROJECT);
        homePage.goTo();
        assertSelfResponsible(homePage);
        homePage.clickClearResponsible();
        selenium.waitForPageToLoad(LOAD_TIMEOUT);
        assertNobodyResponsible(homePage);

        // Clear on the build summary tab
        takeResponsibility(TEST_PROJECT);

        BuildSummaryPage summaryPage = new BuildSummaryPage(selenium, urls, TEST_PROJECT, 1);
        summaryPage.goTo();
        assertTrue(summaryPage.hasResponsibleUser());

        summaryPage.clickAction(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY);
        selenium.waitForPageToLoad(LOAD_TIMEOUT);
        assertNobodyResponsible(summaryPage);

        takeResponsibility(TEST_PROJECT);

        // Clear on the dashboard
        DashboardPage dashboardPage = new DashboardPage(selenium, urls);
        dashboardPage.goTo();
        assertTrue(dashboardPage.hasResponsibilities());
        dashboardPage.clearResponsibility(TEST_PROJECT);
        selenium.waitForPageToLoad(LOAD_TIMEOUT);
        assertFalse(dashboardPage.hasResponsibilities());
    }

    public void testOtherUserResponsible() throws Exception
    {
        takeResponsibility(TEST_PROJECT);

        xmlRpcHelper.insertTrivialUser(random);
        login(random, "");

        ProjectHomePage homePage = new ProjectHomePage(selenium, urls, TEST_PROJECT);
        homePage.goTo();
        assertOtherResponsible(homePage);

        BuildSummaryPage summaryPage = new BuildSummaryPage(selenium, urls, TEST_PROJECT, 1);
        summaryPage.goTo();
        assertOtherResponsible(summaryPage);
        assertFalse(summaryPage.isActionPresent(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY));

        BrowsePage browsePage = new BrowsePage(selenium, urls);
        browsePage.goTo();
        assertTrue(browsePage.isResponsibilityPresent(null, TEST_PROJECT));
        
        DashboardPage dashboardPage = new DashboardPage(selenium, urls);
        dashboardPage.goTo();
        assertFalse(dashboardPage.hasResponsibilities());
    }

    public void testAdminCanClearResponsibility() throws Exception
    {
        adminClearHelper(new ProjectHomePage(selenium, urls, TEST_PROJECT));
    }

    public void testAdminCanClearResponsibilityOnBuildPage() throws Exception
    {
        adminClearHelper(new BuildSummaryPage(selenium, urls, TEST_PROJECT, 1));
    }

    private void adminClearHelper(ResponsibilityPage page) throws Exception
    {
        takeResponsibility(TEST_PROJECT);

        loginAsAdmin();
        page.goTo();

        assertTrue(page.isActionPresent(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY));
        page.clickClearResponsible();
        selenium.waitForPageToLoad(LOAD_TIMEOUT);
        assertNobodyResponsible(page);
    }

    public void testAutoClearResponsibility() throws Exception
    {
        String projectPath = xmlRpcHelper.insertSimpleProject(random, false);
        takeResponsibility(random);

        login(TEST_USER, "");
        ProjectHomePage homePage = new ProjectHomePage(selenium, urls, random);
        homePage.goTo();
        assertSelfResponsible(homePage);

        // Modify the config so the build fails.
        String antPath = PathUtils.getPath(projectPath, Constants.Project.TYPE);
        Hashtable<String, Object> antConfig = xmlRpcHelper.getAntConfig();
        antConfig.put(Constants.Project.AntType.TARGETS, "nosuchtarget");
        xmlRpcHelper.saveConfig(antPath, antConfig, false);
        runBuild(random, false);

        homePage.goTo();
        assertSelfResponsible(homePage);

        // Fix the build, so responsibility should clear.
        antConfig.put(Constants.Project.AntType.TARGETS, "");
        xmlRpcHelper.saveConfig(antPath, antConfig, false);
        runBuild(random, true);

        homePage.goTo();
        assertNobodyResponsible(homePage);
    }

    public void testAutoClearResponsibilityDisabled() throws Exception
    {
        String projectPath = xmlRpcHelper.insertSimpleProject(random, false);
        takeResponsibility(random);

        String optionsPath = PathUtils.getPath(projectPath, Constants.Project.OPTIONS);
        Hashtable<String, Object> optionsConfig = xmlRpcHelper.getConfig(optionsPath);
        optionsConfig.put(Constants.Project.Options.AUTO_CLEAR_RESPONSIBILITY, false);
        xmlRpcHelper.saveConfig(optionsPath, optionsConfig, false);

        login(TEST_USER, "");
        ProjectHomePage homePage = new ProjectHomePage(selenium, urls, random);
        homePage.goTo();
        assertSelfResponsible(homePage);

        runBuild(random, true);

        assertSelfResponsible(homePage);
    }

    private void runBuild(String project, Boolean expectedSuccess) throws Exception
    {
        int buildId = xmlRpcHelper.runBuild(project, BUILD_TIMEOUT);
        Hashtable<String, Object> build = xmlRpcHelper.getBuild(project, buildId);
        assertEquals(expectedSuccess, build.get("succeeded"));
    }

    private void assertNobodyResponsible(ResponsibilityPage page)
    {
        assertFalse(page.hasResponsibleUser());
        assertTrue(page.isActionPresent(ProjectConfigurationActions.ACTION_TAKE_RESPONSIBILITY));
        assertFalse(page.isActionPresent(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY));
    }

    private void assertSelfResponsible(ResponsibilityPage page)
    {
        assertTrue(page.hasResponsibleUser());
        assertEquals("You are currently responsible for build issues for this project.", page.getResponsibleMessage());
        assertEquals(TEST_COMMENT, page.getResponsibleComment());
    }

    private void assertOtherResponsible(ResponsibilityPage page)
    {
        assertTrue(page.hasResponsibleUser());
        assertEquals(TEST_USER + " is currently responsible for build issues for this project.", page.getResponsibleMessage());
        assertFalse(page.isClearResponsibilityPresent());
    }

    private void takeResponsibility(String project) throws Exception
    {
        XmlRpcHelper helper = new XmlRpcHelper(new URL(baseUrl + "xmlrpc"));
        helper.login(TEST_USER, "");
        try
        {
            helper.takeResponsibility(project, TEST_COMMENT);

        }
        finally
        {
            helper.logout();
        }
    }

    private void ensureBuild(String project) throws Exception
    {
        if (xmlRpcHelper.getBuild(project, BUILD_NUMBER) == null)
        {
            xmlRpcHelper.runBuild(project, BUILD_TIMEOUT);
        }
    }
}
