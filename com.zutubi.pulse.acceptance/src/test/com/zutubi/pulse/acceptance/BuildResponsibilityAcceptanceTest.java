package com.zutubi.pulse.acceptance;

import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.DEFAULT_RECIPE;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.RECIPES;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.COMMANDS;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.DEFAULT_COMMAND;
import static com.zutubi.pulse.acceptance.Constants.Project.TYPE;
import com.zutubi.pulse.acceptance.pages.browse.*;
import com.zutubi.pulse.acceptance.pages.dashboard.DashboardPage;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.tove.type.record.PathUtils;

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
    private static final long LOAD_TIMEOUT = 30000;

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
        takeResponsibilityHelper(browser.createPage(ProjectHomePage.class, TEST_PROJECT));

        // Build pages for this project should show the responsibility.
        BuildSummaryPage summaryPage = browser.openAndWaitFor(BuildSummaryPage.class, TEST_PROJECT, 1L);
        assertSelfResponsible(summaryPage);

        // Responsibilities should appear on your dashboard.
        DashboardPage dashboardPage = browser.openAndWaitFor(DashboardPage.class);
        assertTrue(dashboardPage.hasResponsibilities());
        assertTrue(dashboardPage.hasResponsibility(TEST_PROJECT));
        assertTrue(dashboardPage.isResponsibilityPresent(null, TEST_PROJECT));

        // Responsibilility icons should appear in the browse view
        BrowsePage browsePage = browser.openAndWaitFor(BrowsePage.class);
        assertTrue(browsePage.isResponsibilityPresent(null, TEST_PROJECT));
    }

    public void testTakeResponsibilityOnBuildPage()
    {
        takeResponsibilityHelper(browser.createPage(BuildSummaryPage.class, TEST_PROJECT, 1L));
    }

    private void takeResponsibilityHelper(ResponsibilityPage page)
    {
        login(TEST_USER, "");
        page.openAndWaitFor();
        assertNobodyResponsible(page);

        page.clickAction(ProjectConfigurationActions.ACTION_TAKE_RESPONSIBILITY);

        TakeResponsibilityDialog dialog = new TakeResponsibilityDialog(browser);
        dialog.waitFor();
        dialog.typeComment(TEST_COMMENT);
        dialog.clickOk();

        browser.waitForPageToLoad(LOAD_TIMEOUT);
        assertSelfResponsible(page);
    }

    public void testClearResponsibility() throws Exception
    {
        takeResponsibility(TEST_PROJECT);

        login(TEST_USER, "");

        // Clear on the project home tab
        ProjectHomePage homePage = browser.openAndWaitFor(ProjectHomePage.class, TEST_PROJECT);
        assertSelfResponsible(homePage);
        homePage.clickClearResponsible();
        browser.waitForPageToLoad(LOAD_TIMEOUT);
        assertNobodyResponsible(homePage);

        // Clear on the build summary tab
        takeResponsibility(TEST_PROJECT);

        BuildSummaryPage summaryPage = browser.openAndWaitFor(BuildSummaryPage.class, TEST_PROJECT, 1L);
        assertTrue(summaryPage.hasResponsibleUser());

        summaryPage.clickAction(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY);
        browser.waitForPageToLoad(LOAD_TIMEOUT);
        assertNobodyResponsible(summaryPage);

        takeResponsibility(TEST_PROJECT);

        // Clear on the dashboard
        DashboardPage dashboardPage = browser.openAndWaitFor(DashboardPage.class);
        assertTrue(dashboardPage.hasResponsibilities());
        dashboardPage.clearResponsibility(TEST_PROJECT);
        browser.waitForPageToLoad(LOAD_TIMEOUT);
        assertFalse(dashboardPage.hasResponsibilities());
    }

    public void testOtherUserResponsible() throws Exception
    {
        takeResponsibility(TEST_PROJECT);

        xmlRpcHelper.insertTrivialUser(random);
        login(random, "");

        ProjectHomePage homePage = browser.openAndWaitFor(ProjectHomePage.class, TEST_PROJECT);
        assertOtherResponsible(homePage);

        BuildSummaryPage summaryPage = browser.openAndWaitFor(BuildSummaryPage.class, TEST_PROJECT, 1L);
        assertOtherResponsible(summaryPage);
        assertFalse(summaryPage.isActionPresent(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY));

        BrowsePage browsePage = browser.openAndWaitFor(BrowsePage.class);
        assertTrue(browsePage.isResponsibilityPresent(null, TEST_PROJECT));
        
        DashboardPage dashboardPage = browser.openAndWaitFor(DashboardPage.class);
        assertFalse(dashboardPage.hasResponsibilities());
    }

    public void testAdminCanClearResponsibility() throws Exception
    {
        adminClearHelper(browser.createPage(ProjectHomePage.class, TEST_PROJECT));
    }

    public void testAdminCanClearResponsibilityOnBuildPage() throws Exception
    {
        adminClearHelper(browser.createPage(BuildSummaryPage.class, TEST_PROJECT, 1L));
    }

    private void adminClearHelper(ResponsibilityPage page) throws Exception
    {
        takeResponsibility(TEST_PROJECT);

        loginAsAdmin();
        page.openAndWaitFor();

        assertTrue(page.isActionPresent(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY));
        page.clickClearResponsible();
        browser.waitForPageToLoad(LOAD_TIMEOUT);
        assertNobodyResponsible(page);
    }

    public void testAutoClearResponsibility() throws Exception
    {
        String projectPath = xmlRpcHelper.insertSimpleProject(random, false);
        takeResponsibility(random);

        login(TEST_USER, "");
        ProjectHomePage homePage = browser.openAndWaitFor(ProjectHomePage.class, random);
        assertSelfResponsible(homePage);

        // Modify the config so the build fails.
        String antPath = PathUtils.getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE, COMMANDS, DEFAULT_COMMAND);
        Hashtable<String, Object> antConfig = xmlRpcHelper.getConfig(antPath);
        antConfig.put(Constants.Project.AntCommand.TARGETS, "nosuchtarget");
        xmlRpcHelper.saveConfig(antPath, antConfig, false);
        runBuild(random, false);

        homePage.openAndWaitFor();
        assertSelfResponsible(homePage);

        // Fix the build, so responsibility should clear.
        antConfig.put(Constants.Project.AntCommand.TARGETS, "");
        xmlRpcHelper.saveConfig(antPath, antConfig, false);
        runBuild(random, true);

        homePage.openAndWaitFor();
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
        ProjectHomePage homePage = browser.openAndWaitFor(ProjectHomePage.class, random);
        assertSelfResponsible(homePage);

        runBuild(random, true);

        assertSelfResponsible(homePage);
    }

    private void runBuild(String project, Boolean expectedSuccess) throws Exception
    {
        int buildId = xmlRpcHelper.runBuild(project);
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
        XmlRpcHelper helper = new XmlRpcHelper();
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
            xmlRpcHelper.runBuild(project);
        }
    }
}
