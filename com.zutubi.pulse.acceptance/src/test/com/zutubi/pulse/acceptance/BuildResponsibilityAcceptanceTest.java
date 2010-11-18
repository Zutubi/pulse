package com.zutubi.pulse.acceptance;

import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.DEFAULT_RECIPE_NAME;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.RECIPES;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.COMMANDS;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.DEFAULT_COMMAND;
import static com.zutubi.pulse.acceptance.Constants.Project.TYPE;
import com.zutubi.pulse.acceptance.pages.browse.*;
import com.zutubi.pulse.acceptance.pages.dashboard.DashboardPage;
import com.zutubi.pulse.acceptance.rpc.RpcClient;
import com.zutubi.pulse.core.test.TestUtils;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.USERS_SCOPE;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import com.zutubi.util.Condition;
import com.zutubi.util.StringUtils;

import java.util.Hashtable;

/**
 * Acceptance tests for taking/clearing responsibility for a build.
 */
public class BuildResponsibilityAcceptanceTest extends AcceptanceTestBase
{
    private static final String TEST_PROJECT = "responsibility-test-project";
    private static final String TEST_USER = "responsibility-user";
    private static final String TEST_COMMENT = "a comment here";

    private static final int BUILD_NUMBER = 1;
    private static final int RESPONSIBILITY_TIMEOUT = 30000;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        rpcClient.loginAsAdmin();
        rpcClient.RemoteApi.ensureProject(TEST_PROJECT);
        rpcClient.RemoteApi.ensureUser(TEST_USER);
        rpcClient.RemoteApi.ensureBuild(TEST_PROJECT, BUILD_NUMBER);
        rpcClient.RemoteApi.clearResponsibility(TEST_PROJECT);
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testTakeResponsibility()
    {
        takeResponsibilityHelper(getBrowser().createPage(ProjectHomePage.class, TEST_PROJECT));

        // Build pages for this project should show the responsibility.
        BuildSummaryPage summaryPage = getBrowser().openAndWaitFor(BuildSummaryPage.class, TEST_PROJECT, 1L);
        awaitAndAssertSelfResponsible(summaryPage);

        // Responsibilities should appear on your dashboard.
        DashboardPage dashboardPage = getBrowser().openAndWaitFor(DashboardPage.class);
        assertTrue(dashboardPage.hasResponsibilities());
        assertTrue(dashboardPage.hasResponsibility(TEST_PROJECT));
        assertTrue(dashboardPage.isResponsibilityPresent(null, TEST_PROJECT));

        // Responsibilility icons should appear in the browse view
        BrowsePage browsePage = getBrowser().openAndWaitFor(BrowsePage.class);
        assertTrue(browsePage.isResponsibilityPresent(null, TEST_PROJECT));
    }

    public void testTakeResponsibilityOnBuildPage()
    {
        takeResponsibilityHelper(getBrowser().createPage(BuildSummaryPage.class, TEST_PROJECT, 1L));
    }

    private void takeResponsibilityHelper(ResponsibilityPage page)
    {
        assertTrue(getBrowser().login(TEST_USER, ""));
        page.openAndWaitFor();
        awaitAndAssertNobodyResponsible(page);

        page.clickAction(ProjectConfigurationActions.ACTION_TAKE_RESPONSIBILITY);

        TakeResponsibilityDialog dialog = new TakeResponsibilityDialog(getBrowser());
        dialog.waitFor();
        dialog.typeInput(TEST_COMMENT);
        dialog.clickOk();

        awaitAndAssertSelfResponsible(page);
    }

    public void testClearResponsibility() throws Exception
    {
        takeResponsibility(TEST_PROJECT);

        assertTrue(getBrowser().login(TEST_USER, ""));

        // Clear on the project home tab
        final ProjectHomePage homePage = getBrowser().openAndWaitFor(ProjectHomePage.class, TEST_PROJECT);
        awaitAndAssertSelfResponsible(homePage);
        homePage.clickClearResponsible();
        awaitAndAssertNobodyResponsible(homePage);

        // Clear on the build summary tab
        takeResponsibility(TEST_PROJECT);

        BuildSummaryPage summaryPage = getBrowser().openAndWaitFor(BuildSummaryPage.class, TEST_PROJECT, 1L);
        assertTrue(summaryPage.hasResponsibleUser());

        summaryPage.clickAction(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY);
        awaitAndAssertNobodyResponsible(summaryPage);

        takeResponsibility(TEST_PROJECT);

        // Clear on the dashboard
        DashboardPage dashboardPage = getBrowser().openAndWaitFor(DashboardPage.class);
        assertTrue(dashboardPage.hasResponsibilities());
        dashboardPage.clearResponsibility(TEST_PROJECT);
        dashboardPage.waitForReload();
        assertFalse(dashboardPage.hasResponsibilities());
    }

    public void testOtherUserResponsible() throws Exception
    {
        takeResponsibility(TEST_PROJECT);

        rpcClient.RemoteApi.insertTrivialUser(random);
        assertTrue(getBrowser().login(random, ""));

        ProjectHomePage homePage = getBrowser().openAndWaitFor(ProjectHomePage.class, TEST_PROJECT);
        awaitAndAssertOtherResponsible(homePage);

        BuildSummaryPage summaryPage = getBrowser().openAndWaitFor(BuildSummaryPage.class, TEST_PROJECT, 1L);
        awaitAndAssertOtherResponsible(summaryPage);
        assertFalse(summaryPage.isActionPresent(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY));

        BrowsePage browsePage = getBrowser().openAndWaitFor(BrowsePage.class);
        assertTrue(browsePage.isResponsibilityPresent(null, TEST_PROJECT));
        
        DashboardPage dashboardPage = getBrowser().openAndWaitFor(DashboardPage.class);
        assertFalse(dashboardPage.hasResponsibilities());
    }

    public void testAdminCanClearResponsibility() throws Exception
    {
        adminClearHelper(getBrowser().createPage(ProjectHomePage.class, TEST_PROJECT));
    }

    public void testAdminCanClearResponsibilityOnBuildPage() throws Exception
    {
        adminClearHelper(getBrowser().createPage(BuildSummaryPage.class, TEST_PROJECT, 1L));
    }

    private void adminClearHelper(ResponsibilityPage page) throws Exception
    {
        takeResponsibility(TEST_PROJECT);

        getBrowser().loginAsAdmin();
        page.openAndWaitFor();

        assertTrue(page.isActionPresent(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY));
        page.clickClearResponsible();
        awaitAndAssertNobodyResponsible(page);
    }

    public void testAutoClearResponsibility() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(random, false);
        takeResponsibility(random);

        assertTrue(getBrowser().login(TEST_USER, ""));
        ProjectHomePage homePage = getBrowser().openAndWaitFor(ProjectHomePage.class, random);
        awaitAndAssertSelfResponsible(homePage);

        // Modify the config so the build fails.
        String antPath = PathUtils.getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE_NAME, COMMANDS, DEFAULT_COMMAND);
        Hashtable<String, Object> antConfig = rpcClient.RemoteApi.getConfig(antPath);
        antConfig.put(Constants.Project.AntCommand.TARGETS, "nosuchtarget");
        rpcClient.RemoteApi.saveConfig(antPath, antConfig, false);
        runBuild(random, false);

        homePage.openAndWaitFor();
        awaitAndAssertSelfResponsible(homePage);

        // Fix the build, so responsibility should clear.
        antConfig.put(Constants.Project.AntCommand.TARGETS, "");
        rpcClient.RemoteApi.saveConfig(antPath, antConfig, false);
        runBuild(random, true);

        homePage.openAndWaitFor();
        awaitAndAssertNobodyResponsible(homePage);
    }

    public void testAutoClearResponsibilityDisabled() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(random, false);
        takeResponsibility(random);

        String optionsPath = PathUtils.getPath(projectPath, Constants.Project.OPTIONS);
        Hashtable<String, Object> optionsConfig = rpcClient.RemoteApi.getConfig(optionsPath);
        optionsConfig.put(Constants.Project.Options.AUTO_CLEAR_RESPONSIBILITY, false);
        rpcClient.RemoteApi.saveConfig(optionsPath, optionsConfig, false);

        assertTrue(getBrowser().login(TEST_USER, ""));
        ProjectHomePage homePage = getBrowser().openAndWaitFor(ProjectHomePage.class, random);
        awaitAndAssertSelfResponsible(homePage);

        runBuild(random, true);

        awaitAndAssertSelfResponsible(homePage);
    }

    public void testCanDeleteUserWithResponsibility() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random, false);
        takeResponsibility(random);
        assertTrue(rpcClient.RemoteApi.deleteConfig(getPath(USERS_SCOPE, TEST_USER)));
    }

    private void runBuild(String project, Boolean expectedSuccess) throws Exception
    {
        int buildId = rpcClient.RemoteApi.runBuild(project);
        Hashtable<String, Object> build = rpcClient.RemoteApi.getBuild(project, buildId);
        assertEquals(expectedSuccess, build.get("succeeded"));
    }

    private void awaitAndAssertNobodyResponsible(ResponsibilityPage page)
    {
        TestUtils.waitForCondition(new NoResponsibilityCondition(page), RESPONSIBILITY_TIMEOUT, "responsibility to clear");
    }

    private void awaitAndAssertSelfResponsible(ResponsibilityPage page)
    {
        TestUtils.waitForCondition(new SelfResponsibleCondition(page), RESPONSIBILITY_TIMEOUT, "responsibility to be assigned to self");
    }

    private void awaitAndAssertOtherResponsible(ResponsibilityPage page)
    {
        TestUtils.waitForCondition(new OtherResponsibleCondition(page), RESPONSIBILITY_TIMEOUT, "responsibility to be assigned to other user");
    }

    private void takeResponsibility(String project) throws Exception
    {
        RpcClient rpcClient = new RpcClient();
        rpcClient.login(TEST_USER, "");
        try
        {
            rpcClient.RemoteApi.takeResponsibility(project, TEST_COMMENT);
        }
        finally
        {
            rpcClient.logout();
        }
    }

    private static class NoResponsibilityCondition implements Condition
    {
        private ResponsibilityPage page;

        private NoResponsibilityCondition(ResponsibilityPage page)
        {
            this.page = page;
        }

        public boolean satisfied()
        {
            return !page.hasResponsibleUser() &&
                    page.isActionPresent(ProjectConfigurationActions.ACTION_TAKE_RESPONSIBILITY) &&
                    !page.isActionPresent(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY);
        }
    }

    private static class HasResponsibilityCondition implements Condition
    {
        protected ResponsibilityPage page;
        private String message;
        private String comment;

        private HasResponsibilityCondition(ResponsibilityPage page, String message, String comment)
        {
            this.page = page;
            this.message = message;
            this.comment = comment;
        }

        public boolean satisfied()
        {
            return page.hasResponsibleUser() &&
                   StringUtils.equals(message, page.getResponsibleMessage()) &&
                   StringUtils.equals(comment, page.getResponsibleComment());
        }
    }

    private static class SelfResponsibleCondition extends HasResponsibilityCondition
    {
        private SelfResponsibleCondition(ResponsibilityPage page)
        {
            super(page, "You are currently responsible for build issues for this project.", TEST_COMMENT);
        }

        @Override
        public boolean satisfied()
        {
            return super.satisfied() && page.isClearResponsibilityPresent();
        }
    }

    private static class OtherResponsibleCondition extends HasResponsibilityCondition
    {
        private OtherResponsibleCondition(ResponsibilityPage page)
        {
            super(page, TEST_USER + " is currently responsible for build issues for this project.", TEST_COMMENT);
        }
    }
}
