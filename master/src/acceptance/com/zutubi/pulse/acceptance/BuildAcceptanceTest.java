package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.BuildStageForm;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildDetailedViewPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectsPage;
import com.zutubi.pulse.agent.AgentManager;

import java.util.Hashtable;

/**
 * An acceptance test that adds a very simple project and runs a build as a
 * sanity test.
 */
public class BuildAcceptanceTest extends SeleniumTestBase
{
    private static final String AGENT_NAME   = "localhost";
    private static final String PROJECT_NAME = "BuildAcceptanceTest-Project";

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void testSimpleBuild() throws Exception
    {
        loginAsAdmin();
        goTo(urls.adminProjects());
        addProject(random);

        triggerSuccessfulBuild(random, AgentManager.MASTER_AGENT_NAME);
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

        triggerSuccessfulBuild(random, AGENT_NAME);
    }

    public void testDetailedView() throws Exception
    {
        loginAsAdmin();
        ensureBuild();

        BuildDetailedViewPage detailedViewPage = new BuildDetailedViewPage(selenium, urls, PROJECT_NAME, 1);
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

    public void testPulseEnvironmentVariables() throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        try
        {
            loginAsAdmin();
            ensureProject(random);

            Hashtable<String, Object> property = new Hashtable<String, Object>();
            property.put(BaseXmlRpcAcceptanceTest.SYMBOLIC_NAME_KEY, "zutubi.resourceProperty");
            property.put("name", "pname");
            property.put("value", "pvalue");
            property.put("addToEnvironment", true);
            xmlRpcHelper.insertConfig("projects/" + random + "/properties", property);
        }
        finally
        {
            xmlRpcHelper.logout();
        }

        triggerSuccessfulBuild(random, AgentManager.MASTER_AGENT_NAME);

        BuildDetailedViewPage detailedViewPage = new BuildDetailedViewPage(selenium, urls, random, 1);
        detailedViewPage.goTo();
        detailedViewPage.clickCommand("default", "build");
        selenium.click("link=env.txt");
        selenium.waitForPageToLoad("10000");
        assertTextPresent("pname=pvalue");
        assertTextPresent("PULSE_PNAME=pvalue");
        assertTextPresent("PULSE_BUILD_NUMBER=1");
    }

    private void ensureBuild() throws Exception
    {
        if(ensureProject(PROJECT_NAME))
        {
            triggerSuccessfulBuild(PROJECT_NAME, AgentManager.MASTER_AGENT_NAME);
        }
    }

    private void triggerSuccessfulBuild(String projectName, String agent)
    {
        ProjectsPage projectsPage = new ProjectsPage(selenium, urls);
        projectsPage.goTo();
        projectsPage.assertProjectPresent(projectName);
        projectsPage.triggerProject(projectName);
        projectsPage.waitFor();

        ProjectHomePage home = new ProjectHomePage(selenium, urls, projectName);
        home.goTo();
        String statusId = IDs.buildStatusCell(projectName, 1);
        SeleniumUtils.refreshUntilElement(selenium, statusId);
        SeleniumUtils.refreshUntilText(selenium, statusId, "success");
        SeleniumUtils.assertText(selenium, IDs.stageAgentCell(projectName, 1, "default"), agent);
    }
}
