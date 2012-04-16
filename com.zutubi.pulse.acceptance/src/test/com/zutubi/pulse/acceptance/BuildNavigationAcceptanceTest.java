package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.PulseToolbar;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.acceptance.pages.dashboard.MyBuildsPage;
import com.zutubi.pulse.acceptance.pages.dashboard.PersonalBuildSummaryPage;
import com.zutubi.pulse.acceptance.utils.BuildRunner;
import com.zutubi.pulse.acceptance.utils.PersonalBuildRunner;
import com.zutubi.pulse.acceptance.utils.ProjectConfigurationHelper;
import com.zutubi.pulse.acceptance.utils.UserConfigurations;
import com.zutubi.pulse.acceptance.utils.workspace.SubversionWorkspace;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;

/**
 * Acceptance tests for the build navigation portion of the breadcrumbs.
 */
public class BuildNavigationAcceptanceTest extends AcceptanceTestBase
{
    //NOTE: This acceptance test is structured slightly differently to avoid the
    //      creation of a heap of projects and builds.  It follows the workflow
    //      and testing of a single project.

    private BuildRunner buildRunner;
    private UserConfigurations users;
    private String projectName;
    private PulseToolbar toolbar;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        buildRunner = new BuildRunner(rpcClient.RemoteApi);

        users = new UserConfigurations();

        rpcClient.loginAsAdmin();

        toolbar = new PulseToolbar(getBrowser());
    }

    @Override
    protected void tearDown() throws Exception
    {
        getBrowser().logout();
        rpcClient.logout();

        super.tearDown();
    }

    public void testBuildNavigation() throws Exception
    {
        projectName = randomName() + "\"' ;?&<html>";

        // create project.
        ProjectConfigurationHelper project = projectConfigurations.createTrivialAntProject(projectName);
        configurationHelper.insertProject(project.getConfig(), false);

        getBrowser().loginAsAdmin();

        doTestNoBuildForProjectHomepage();

        // run single build
        buildRunner.triggerSuccessfulBuild(project.getConfig());

        doTestSingleBuild();

        // run some more builds.
        buildRunner.triggerSuccessfulBuild(project.getConfig());
        buildRunner.triggerSuccessfulBuild(project.getConfig());
        buildRunner.triggerSuccessfulBuild(project.getConfig());
        buildRunner.triggerSuccessfulBuild(project.getConfig());
        buildRunner.triggerSuccessfulBuild(project.getConfig());

        doTestMultipleBuilds();

        doTestPopupMenu();
    }

    private void doTestNoBuildForProjectHomepage()
    {
        // go to project home page - ensure that the standard stuff is there.
        getBrowser().openAndWaitFor(ProjectHomePage.class, projectName);

        assertTrue(toolbar.isProjectLinkPresent());
        assertFalse(toolbar.isBuildNavPresent());
        assertFalse(toolbar.isBuildNavMenuPresent());
    }

    private void doTestSingleBuild()
    {
        getBrowser().openAndWaitFor(BuildSummaryPage.class, projectName, 1L);

        toolbar.waitForBuildNav();

        assertFalse(toolbar.isBuildNavMenuPresent());
        assertTrue(toolbar.isBuildNavLinkPresent(1));
    }

    private void doTestMultipleBuilds()
    {
        getBrowser().openAndWaitFor(BuildSummaryPage.class, projectName, 3L);

        toolbar.waitForBuildNav();

        assertTrue(toolbar.isBuildNavMenuPresent());
        assertTrue(toolbar.isBuildNavItemPresent(1));
        assertTrue(toolbar.isBuildNavItemPresent(2));
        assertTrue(toolbar.isBuildNavLinkPresent(3));
        assertTrue(toolbar.isBuildNavItemPresent(4));
        assertTrue(toolbar.isBuildNavItemPresent(5));
        assertFalse(toolbar.isBuildNavItemPresent(6));

        // check navigation
        toolbar.clickBuildNavItem(5);
        BuildSummaryPage page = getBrowser().createPage(BuildSummaryPage.class, projectName, 5L);
        page.waitFor();

        // wait for reload?
        toolbar.waitForBuildNav();

        assertTrue(toolbar.isBuildNavLinkPresent(5));
        assertFalse(toolbar.isBuildNavItemPresent(1));
        assertTrue(toolbar.isBuildNavItemPresent(6));
    }

    private void doTestPopupMenu()
    {
        getBrowser().openAndWaitFor(BuildSummaryPage.class, projectName, 3L);

        toolbar.waitForBuildNav();

        assertFalse(toolbar.isNextSuccessfulBuildLinkPresent());

        assertTrue(toolbar.isBuildNavMenuPresent());
        toolbar.clickOnNavMenu();

        assertTrue(toolbar.isNextSuccessfulBuildLinkPresent());
        assertTrue(toolbar.isPreviousSuccessfulBuildLinkPresent());
        assertTrue(toolbar.isLatestBuildLinkPresent());
        assertFalse(toolbar.isNextBrokenBuildLinkPresent());
        assertFalse(toolbar.isPreviousBrokenBuildLinkPresent());

        getBrowser().openAndWaitFor(BuildSummaryPage.class, projectName, 1L);

        toolbar.waitForBuildNav();
        toolbar.clickOnNavMenu();
        assertTrue(toolbar.isNextSuccessfulBuildLinkPresent());
        assertTrue(toolbar.isLatestBuildLinkPresent());
        assertFalse(toolbar.isPreviousSuccessfulBuildLinkPresent());

        toolbar.clickNextSuccessfulBuildLink();
        BuildSummaryPage page = getBrowser().createPage(BuildSummaryPage.class, projectName, 2L);
        page.waitFor();

        toolbar.waitForBuildNav();
        assertTrue(toolbar.isBuildNavLinkPresent(2));

        toolbar.clickOnNavMenu();
        assertTrue(toolbar.isNextSuccessfulBuildLinkPresent());
        assertTrue(toolbar.isPreviousSuccessfulBuildLinkPresent());
        assertTrue(toolbar.isLatestBuildLinkPresent());

        toolbar.clickLatestBuildLink();

        page = getBrowser().createPage(BuildSummaryPage.class, projectName, 6L);
        page.waitFor();

        toolbar.waitForBuildNav();
        assertFalse(toolbar.isLatestBuildLinkPresent());
    }

    public void testPersonalBuildNavigation() throws Exception
    {
        // create project and user.
        projectName = randomName();

        ProjectConfigurationHelper project = projectConfigurations.createTrivialAntProject(projectName);
        configurationHelper.insertProject(project.getConfig(), false);

        String userName = randomName();
        UserConfiguration user = users.createSimpleUser(userName);
        configurationHelper.insertUser(user);

        // user needs 'run personal build' permissions.
        rpcClient.RemoteApi.ensureUserCanRunPersonalBuild(userName);

        assertTrue(getBrowser().login(userName, ""));
        rpcClient.login(userName, "");

        File workingCopy = createTempDirectory();
        SubversionWorkspace workspace = new SubversionWorkspace(workingCopy, "pulse", "pulse");
        workspace.doCheckout(Constants.TRIVIAL_ANT_REPOSITORY);

        // make a change to the working copy so that we can run personal builds.
        File newFile = new File(workingCopy, "file.txt");
        FileSystemUtils.createFile(newFile, "new file");
        workspace.doAdd(newFile);

        PersonalBuildRunner buildRunner = new PersonalBuildRunner(rpcClient.RemoteApi);
        buildRunner.setBase(workingCopy);
        buildRunner.createConfigFile(baseUrl, userName, "", projectName);

        doTestNoPersonalBuilds();

        buildRunner.triggerAndWaitForBuild();

        doTestSinglePersonalBuild();

        buildRunner.triggerAndWaitForBuild();
        buildRunner.triggerAndWaitForBuild();
        buildRunner.triggerAndWaitForBuild();
        buildRunner.triggerAndWaitForBuild();

        // wait for personal builds to complete.

        doTestMultiplePersonalBuilds();

        doTestPersonalPopupMenu();
    }

    private void doTestNoPersonalBuilds()
    {
        getBrowser().openAndWaitFor(MyBuildsPage.class);

        assertTrue(toolbar.isMyBuildsLinkPresent());
        assertFalse(toolbar.isBuildNavPresent());
        assertFalse(toolbar.isBuildNavMenuPresent());
    }

    private void doTestSinglePersonalBuild()
    {
        getBrowser().openAndWaitFor(PersonalBuildSummaryPage.class, 1L);

        toolbar.waitForBuildNav();

        assertFalse(toolbar.isBuildNavMenuPresent());
        assertTrue(toolbar.isBuildNavLinkPresent(1));
    }

    private void doTestMultiplePersonalBuilds()
    {
        getBrowser().openAndWaitFor(PersonalBuildSummaryPage.class, 3L);

        toolbar.waitForBuildNav();

        assertTrue(toolbar.isBuildNavMenuPresent());
        assertTrue(toolbar.isBuildNavItemPresent(1));
        assertTrue(toolbar.isBuildNavItemPresent(2));
        assertTrue(toolbar.isBuildNavLinkPresent(3));
        assertTrue(toolbar.isBuildNavItemPresent(4));
        assertTrue(toolbar.isBuildNavItemPresent(5));

        // check navigation
        toolbar.clickBuildNavItem(5);
        PersonalBuildSummaryPage page = getBrowser().createPage(PersonalBuildSummaryPage.class, 5L);
        page.waitFor();

        toolbar.waitForBuildNav();

        assertTrue(toolbar.isBuildNavLinkPresent(5));
        assertTrue(toolbar.isBuildNavItemPresent(3));
    }

    private void doTestPersonalPopupMenu()
    {
        getBrowser().openAndWaitFor(PersonalBuildSummaryPage.class, 3L);

        toolbar.waitForBuildNav();

        assertFalse(toolbar.isNextSuccessfulBuildLinkPresent());

        assertTrue(toolbar.isBuildNavMenuPresent());
        toolbar.clickOnNavMenu();

        assertTrue(toolbar.isNextSuccessfulBuildLinkPresent());
        assertTrue(toolbar.isPreviousSuccessfulBuildLinkPresent());
        assertFalse(toolbar.isNextBrokenBuildLinkPresent());
        assertFalse(toolbar.isPreviousBrokenBuildLinkPresent());

        getBrowser().openAndWaitFor(PersonalBuildSummaryPage.class, 1L);
        toolbar.waitForBuildNav();
        toolbar.clickOnNavMenu();

        assertTrue(toolbar.isNextSuccessfulBuildLinkPresent());
        assertFalse(toolbar.isPreviousSuccessfulBuildLinkPresent());

        toolbar.clickNextSuccessfulBuildLink();
        BuildSummaryPage page = getBrowser().createPage(PersonalBuildSummaryPage.class, 2L);
        page.waitFor();

        toolbar.waitForBuildNav();
        assertTrue(toolbar.isBuildNavLinkPresent(2));

        toolbar.clickOnNavMenu();
        assertTrue(toolbar.isNextSuccessfulBuildLinkPresent());
        assertTrue(toolbar.isPreviousSuccessfulBuildLinkPresent());
    }
}
