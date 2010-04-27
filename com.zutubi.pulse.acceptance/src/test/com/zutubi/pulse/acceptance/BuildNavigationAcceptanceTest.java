package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.acceptance.pages.PulseToolbar;

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
    private String projectName;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        buildRunner = new BuildRunner(xmlRpcHelper);

        ConfigurationHelperFactory factory = new SingletonConfigurationHelperFactory();
        configurationHelper = factory.create(xmlRpcHelper);

        projects = new ProjectConfigurations(configurationHelper);

        xmlRpcHelper.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        
        super.tearDown();
    }

    public void testBuildNavigation() throws Exception
    {
        projectName = randomName();

        // create project.
        ProjectConfigurationHelper project = projects.createTrivialAntProject(projectName);
        configurationHelper.insertProject(project.getConfig());

        doTestNoBuildForProjectHomepage();

        // run single build
        buildRunner.triggerSuccessfulBuild(project.getConfig());

        doTestSingleBuild();

        // run some more builds.
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
    }
}
