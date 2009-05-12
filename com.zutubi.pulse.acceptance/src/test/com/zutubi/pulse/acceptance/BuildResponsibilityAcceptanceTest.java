package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.*;
import com.zutubi.pulse.acceptance.pages.dashboard.DashboardPage;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;

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
        xmlRpcHelper.logout();
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
        takeResponsibility();

        login(TEST_USER, "");

        // Clear on the project home tab
        ProjectHomePage homePage = new ProjectHomePage(selenium, urls, TEST_PROJECT);
        homePage.goTo();
        assertSelfResponsible(homePage);
        homePage.clickClearResponsible();
        selenium.waitForPageToLoad(LOAD_TIMEOUT);
        assertNobodyResponsible(homePage);

        // Clear on the build summary tab
        takeResponsibility();

        BuildSummaryPage summaryPage = new BuildSummaryPage(selenium, urls, TEST_PROJECT, 1);
        summaryPage.goTo();
        assertTrue(summaryPage.hasResponsibleUser());

        summaryPage.clickAction(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY);
        selenium.waitForPageToLoad(LOAD_TIMEOUT);
        assertNobodyResponsible(summaryPage);

        takeResponsibility();

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
        takeResponsibility();

        insertUser(random);     
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

    private void insertUser(String login) throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        try
        {
            xmlRpcHelper.insertTrivialUser(login);
        }
        finally
        {
            xmlRpcHelper.logout();
        }
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
        takeResponsibility();

        loginAsAdmin();
        page.goTo();

        assertTrue(page.isActionPresent(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY));
        page.clickClearResponsible();
        selenium.waitForPageToLoad(LOAD_TIMEOUT);
        assertNobodyResponsible(page);
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

    private void takeResponsibility() throws Exception
    {
        xmlRpcHelper.login(TEST_USER, "");
        try
        {
            xmlRpcHelper.takeResponsibility(TEST_PROJECT, TEST_COMMENT);

        }
        finally
        {
            xmlRpcHelper.logout();
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
