package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.acceptance.utils.workspace.SubversionWorkspace;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.acceptance.pages.PulseToolbar;
import com.zutubi.pulse.acceptance.pages.dashboard.MyBuildsPage;
import com.zutubi.pulse.acceptance.pages.dashboard.PersonalBuildSummaryPage;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Acceptance tests for the build navigation portion of the breadcrumbs.
 */
public class BuildNavigationAcceptanceTest extends SeleniumTestBase
{
    //NOTE: This acceptance test is structured slightly differently to avoid the
    //      creation of a heap of projects and builds.  It follows the workflow
    //      and testing of a single project.

    private BuildRunner buildRunner;
    private ConfigurationHelper configurationHelper;
    private ProjectConfigurations projects;
    private UserConfigurations users;
    private String projectName;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        buildRunner = new BuildRunner(xmlRpcHelper);

        ConfigurationHelperFactory factory = new SingletonConfigurationHelperFactory();
        configurationHelper = factory.create(xmlRpcHelper);

        projects = new ProjectConfigurations(configurationHelper);
        users = new UserConfigurations();

        xmlRpcHelper.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        logout();
        xmlRpcHelper.logout();
        
        super.tearDown();
    }

    public void testBuildNavigation() throws Exception
    {
        projectName = randomName() + "\"' ;?&";

        // create project.
        ProjectConfigurationHelper project = projects.createTrivialAntProject(projectName);
        configurationHelper.insertProject(project.getConfig());

        loginAsAdmin();

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
        browser.openAndWaitFor(ProjectHomePage.class, projectName);

        PulseToolbar toolbar = new PulseToolbar(browser, urls);
        assertTrue(toolbar.isProjectLinkPresent(projectName));
        assertFalse(toolbar.isBuildNavPresent());
        assertFalse(toolbar.isBuildNavMenuPresent());
    }

    private void doTestSingleBuild()
    {
        browser.openAndWaitFor(BuildSummaryPage.class, projectName, 1L);

        PulseToolbar toolbar = new PulseToolbar(browser, urls);
        toolbar.waitForBuildNav();

        assertFalse(toolbar.isBuildNavMenuPresent());
        assertTrue(toolbar.isBuildNavLinkPresent(projectName, 1));
    }

    private void doTestMultipleBuilds()
    {
        browser.openAndWaitFor(BuildSummaryPage.class, projectName, 3L);

        PulseToolbar toolbar = new PulseToolbar(browser, urls);
        toolbar.waitForBuildNav();

        assertTrue(toolbar.isBuildNavMenuPresent());
        assertTrue(toolbar.isBuildNavItemPresent(1));
        assertTrue(toolbar.isBuildNavItemPresent(2));
        assertTrue(toolbar.isBuildNavLinkPresent(projectName, 3));
        assertTrue(toolbar.isBuildNavItemPresent(4));
        assertTrue(toolbar.isBuildNavItemPresent(5));
        assertFalse(toolbar.isBuildNavItemPresent(6));

        // check navigation
        toolbar.clickBuildNavItem(5);
        BuildSummaryPage page = browser.createPage(BuildSummaryPage.class, projectName, 5L);
        page.waitFor();

        // wait for reload?
        toolbar.waitForBuildNav();

        assertTrue(toolbar.isBuildNavLinkPresent(projectName, 5));
        assertFalse(toolbar.isBuildNavItemPresent(1));
        assertTrue(toolbar.isBuildNavItemPresent(6));
    }

    private void doTestPopupMenu()
    {
        browser.openAndWaitFor(BuildSummaryPage.class, projectName, 3L);

        PulseToolbar toolbar = new PulseToolbar(browser, urls);
        toolbar.waitForBuildNav();

        assertFalse(toolbar.isNextSuccessfulBuildLinkPresent());

        assertTrue(toolbar.isBuildNavMenuPresent());
        toolbar.clickOnNavMenu();

        assertTrue(toolbar.isNextSuccessfulBuildLinkPresent());
        assertTrue(toolbar.isPreviousSuccessfulBuildLinkPresent());
        assertFalse(toolbar.isNextBrokenBuildLinkPresent());
        assertFalse(toolbar.isPreviousBrokenBuildLinkPresent());

        browser.openAndWaitFor(BuildSummaryPage.class, projectName, 1L);

        toolbar.waitForBuildNav();
        toolbar.clickOnNavMenu();
        assertTrue(toolbar.isNextSuccessfulBuildLinkPresent());
        assertFalse(toolbar.isPreviousSuccessfulBuildLinkPresent());

        toolbar.clickNextSuccessfulBuildLink();
        BuildSummaryPage page = browser.createPage(BuildSummaryPage.class, projectName, 2L);
        page.waitFor();

        toolbar.waitForBuildNav();
        assertTrue(toolbar.isBuildNavLinkPresent(projectName, 2));

        toolbar.clickOnNavMenu();
        assertTrue(toolbar.isNextSuccessfulBuildLinkPresent());
        assertTrue(toolbar.isPreviousSuccessfulBuildLinkPresent());
    }

    public void testPersonalBuildNavigation() throws Exception
    {
        // create project and user.
        projectName = randomName();

        ProjectConfigurationHelper project = projects.createTrivialAntProject(projectName);
        configurationHelper.insertProject(project.getConfig());

        // ensure the 'all users group' can run 'personal builds'
        ensureAllUsersCanRunPersonalBuilds();

        // user needs 'run personal build' permissions.
        String userName = randomName();
        UserConfiguration user = users.createSimpleUser(userName);
        configurationHelper.insertUser(user);

        login(userName, "");
        xmlRpcHelper.login(userName, "");

        File workingCopy = FileSystemUtils.createTempDir();
        SubversionWorkspace workspace = new SubversionWorkspace(workingCopy, "pulse", "pulse");
        workspace.doCheckout(Constants.TRIVIAL_ANT_REPOSITORY);

        // make a change to the working copy so that we can run personal builds.
        File newFile = new File(workingCopy, "file.txt");
        FileSystemUtils.createFile(newFile, "new file");
        workspace.doAdd(newFile);

        PersonalBuildRunner buildRunner = new PersonalBuildRunner(xmlRpcHelper);
        buildRunner.setBase(workingCopy);
        buildRunner.createConfigFile(browser.getBaseUrl(), userName, "", projectName);

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
        browser.openAndWaitFor(MyBuildsPage.class);

        PulseToolbar toolbar = new PulseToolbar(browser, urls);
        assertTrue(toolbar.isMyBuildsLinkPresent());
        assertFalse(toolbar.isBuildNavPresent());
        assertFalse(toolbar.isBuildNavMenuPresent());
    }

    private void doTestSinglePersonalBuild()
    {
        browser.openAndWaitFor(PersonalBuildSummaryPage.class, 1L);

        PulseToolbar toolbar = new PulseToolbar(browser, urls);
        toolbar.waitForBuildNav();

        assertFalse(toolbar.isBuildNavMenuPresent());
        assertTrue(toolbar.isPersonalBuildNavLinkPresent(1));
    }

    private void doTestMultiplePersonalBuilds()
    {
        browser.openAndWaitFor(PersonalBuildSummaryPage.class, 3L);

        PulseToolbar toolbar = new PulseToolbar(browser, urls);
        toolbar.waitForBuildNav();

        assertTrue(toolbar.isBuildNavMenuPresent());
        assertTrue(toolbar.isBuildNavItemPresent(1));
        assertTrue(toolbar.isBuildNavItemPresent(2));
        assertTrue(toolbar.isPersonalBuildNavLinkPresent(3));
        assertTrue(toolbar.isBuildNavItemPresent(4));
        assertTrue(toolbar.isBuildNavItemPresent(5));

        // check navigation
        toolbar.clickBuildNavItem(5);
        PersonalBuildSummaryPage page = browser.createPage(PersonalBuildSummaryPage.class, 5L);
        page.waitFor();

        toolbar.waitForBuildNav();

        assertTrue(toolbar.isPersonalBuildNavLinkPresent(5));
        assertTrue(toolbar.isBuildNavItemPresent(3));
    }

    private void doTestPersonalPopupMenu()
    {
        browser.openAndWaitFor(PersonalBuildSummaryPage.class, 3L);

        PulseToolbar toolbar = new PulseToolbar(browser, urls);
        toolbar.waitForBuildNav();

        assertFalse(toolbar.isNextSuccessfulBuildLinkPresent());

        assertTrue(toolbar.isBuildNavMenuPresent());
        toolbar.clickOnNavMenu();

        assertTrue(toolbar.isNextSuccessfulBuildLinkPresent());
        assertTrue(toolbar.isPreviousSuccessfulBuildLinkPresent());
        assertFalse(toolbar.isNextBrokenBuildLinkPresent());
        assertFalse(toolbar.isPreviousBrokenBuildLinkPresent());

        browser.openAndWaitFor(PersonalBuildSummaryPage.class, 1L);
        toolbar.waitForBuildNav();
        toolbar.clickOnNavMenu();

        assertTrue(toolbar.isNextSuccessfulBuildLinkPresent());
        assertFalse(toolbar.isPreviousSuccessfulBuildLinkPresent());

        toolbar.clickNextSuccessfulBuildLink();
        BuildSummaryPage page = browser.createPage(PersonalBuildSummaryPage.class, 2L);
        page.waitFor();

        toolbar.waitForBuildNav();
        assertTrue(toolbar.isPersonalBuildNavLinkPresent(2));

        toolbar.clickOnNavMenu();
        assertTrue(toolbar.isNextSuccessfulBuildLinkPresent());
        assertTrue(toolbar.isPreviousSuccessfulBuildLinkPresent());
    }

    private void ensureAllUsersCanRunPersonalBuilds() throws Exception
    {
        ensureGroupHasPermission("groups/all users", ServerPermission.PERSONAL_BUILD.toString());
    }

    private void ensureGroupHasPermission(String groupPath, String permission) throws Exception
    {
        Hashtable group = xmlRpcHelper.getConfig(groupPath);
        Vector perms = (Vector) group.get("serverPermissions");

        if (!perms.contains(permission))
        {
            perms.add(permission);
            xmlRpcHelper.saveConfig(groupPath, group, false);
        }
    }
}
