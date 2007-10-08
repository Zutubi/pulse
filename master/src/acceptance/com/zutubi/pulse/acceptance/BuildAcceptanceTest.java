package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.BuildStageForm;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildDetailedViewPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectsPage;

/**
 * An acceptance test that adds a very simple project and runs a build as a
 * sanity test.
 */
public class BuildAcceptanceTest extends SeleniumTestBase
{
    private static final String AGENT_NAME = "localhost";

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void testSimpleBuild() throws Exception
    {
        loginAsAdmin();
        goTo(urls.adminProjects());
        addProject(random);

        triggerSuccessfulBuild(random);
    }

    public void testAgentBuild() throws Exception
    {
        loginAsAdmin();

        xmlRpcHelper.loginAsAdmin();
        String agentHandle;
        try
        {
            ensureAgent(AGENT_NAME);
            agentHandle = xmlRpcHelper.getConfigHandle("agents/" + AGENT_NAME);
        }
        finally
        {
            xmlRpcHelper.logout();
        }

        goTo(urls.adminProjects());
        addProject(random);
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, random, false);
        hierarchyPage.assertPresent();
        ProjectConfigPage configPage = hierarchyPage.clickConfigure();
        ListPage stagesPage = configPage.selectCollection(ProjectConfigPage.BUILD_STAGES_BASE, ProjectConfigPage.BUILD_STAGES_DISPLAY);
        stagesPage.clickView("default");

        BuildStageForm stageForm = new BuildStageForm(selenium, true);
        stageForm.waitFor();

        stageForm.applyFormElements("", agentHandle);

        triggerSuccessfulBuild(random);
    }

    public void testDetailedView() throws Exception
    {
        ensureProject(random);
        loginAsAdmin();
        triggerSuccessfulBuild(random);

        BuildDetailedViewPage detailedViewPage = new BuildDetailedViewPage(selenium, urls, random, 1);
        detailedViewPage.goTo();
        SeleniumUtils.assertNotVisible(selenium, "link=env.txt");
        detailedViewPage.clickCommand("default", "build");
        SeleniumUtils.assertVisible(selenium, "link=env.txt");
        selenium.click("link=env.txt");
        selenium.waitForPageToLoad("10000");
        assertTextPresent("Process Environment");

        detailedViewPage.goTo();
        detailedViewPage.clickCommand("default", "build");
        selenium.click("link=decorated");
        selenium.waitForPageToLoad("10000");
        assertElementPresent("decorated");
    }

    private void triggerSuccessfulBuild(String projectName)
    {
        ProjectsPage projectsPage = new ProjectsPage(selenium, urls);
        projectsPage.goTo();
        projectsPage.assertProjectPresent(projectName);
        projectsPage.triggerProject(projectName);
        projectsPage.waitFor();

        ProjectHomePage home = new ProjectHomePage(selenium, urls, projectName);
        home.goTo();
        String statusId = IDs.buildNumberCell(projectName, 1);
        SeleniumUtils.refreshUntilElement(selenium, statusId);
        SeleniumUtils.refreshUntilText(selenium, statusId, "success");
    }
}
