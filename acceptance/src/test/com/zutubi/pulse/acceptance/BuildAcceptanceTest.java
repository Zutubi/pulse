package com.zutubi.pulse.acceptance;

import com.zutubi.prototype.config.ConfigurationRegistry;
import static com.zutubi.prototype.type.record.PathUtils.getPath;
import com.zutubi.pulse.acceptance.forms.admin.BuildStageForm;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildDetailedViewPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.model.ResourceRequirement;

import java.util.Hashtable;

/**
 * An acceptance test that adds a very simple project and runs a build as a
 * sanity test.
 */
public class BuildAcceptanceTest extends SeleniumTestBase
{
    private static final String PROJECT_NAME = "BuildAcceptanceTest-Project";

    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
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

        String agentHandle;
        ensureAgent(AGENT_NAME);
        agentHandle = xmlRpcHelper.getConfigHandle("agents/" + AGENT_NAME);

        goTo(urls.adminProjects());
        addProject(random);
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, random, false);
        hierarchyPage.assertPresent();
        ProjectConfigPage configPage = hierarchyPage.clickConfigure();
        configPage.waitFor();
        ListPage stagesPage = configPage.clickCollection(ProjectConfigPage.BUILD_STAGES_BASE, ProjectConfigPage.BUILD_STAGES_DISPLAY);
        stagesPage.waitFor();
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
        loginAsAdmin();
        ensureProject(random);

        xmlRpcHelper.insertProjectProperty(random, "pname", "pvalue", false, true, false);

        triggerSuccessfulBuild(random, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(random, 1, "pname=pvalue", "PULSE_PNAME=pvalue", "PULSE_BUILD_NUMBER=1");
    }

    public void testImportedResources() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(AgentManager.MASTER_AGENT_NAME, resourceName);
        xmlRpcHelper.insertConfig(getPath(resourcePath, "properties"), xmlRpcHelper.createProperty("test-property", "test-value", false, false, false));

        String projectName = random + "-project";
        ensureProject(projectName);
        xmlRpcHelper.insertConfig(getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "PULSE_TEST-PROPERTY=test-value");
    }

    public void testProjectPropertyReferencesResourceProperty() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(AgentManager.MASTER_AGENT_NAME, resourceName);
        xmlRpcHelper.insertConfig(getPath(resourcePath, "properties"), xmlRpcHelper.createProperty("rp", "rv", false, false, false));

        String projectName = random + "-project";
        String projectPath = getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName);
        ensureProject(projectName);
        xmlRpcHelper.insertConfig(getPath(projectPath, "requirements"), createRequiredResource(resourceName, null));
        xmlRpcHelper.insertConfig(getPath(projectPath, "properties"), xmlRpcHelper.createProperty("pp", "ref ${rp}", true, true, false));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "pp=ref rv");
    }

    public void testResourcePropertyReferencesEarlierProperty() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(AgentManager.MASTER_AGENT_NAME, resourceName);
        String propertiesPath = getPath(resourcePath, "properties");
        xmlRpcHelper.insertConfig(propertiesPath, xmlRpcHelper.createProperty("referee", "ee", false, false, false));
        xmlRpcHelper.insertConfig(propertiesPath, xmlRpcHelper.createProperty("referer", "ref ${referee}", true, true, false));

        String projectName = random + "-project";
        ensureProject(projectName);
        xmlRpcHelper.insertConfig(getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "referer=ref ee");
    }

    public void testProjectPropertyReferencesAgentName() throws Exception
    {
        String projectName = random + "-project";
        ensureProject(projectName);
        String stagePath = getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "stages", "default");
        Hashtable<String, Object> defaultStage = xmlRpcHelper.getConfig(stagePath);
        defaultStage.put("agent", getPath(ConfigurationRegistry.AGENTS_SCOPE, AgentManager.MASTER_AGENT_NAME));
        xmlRpcHelper.saveConfig(stagePath, defaultStage, false);
        xmlRpcHelper.insertProjectProperty(projectName, "pp", "ref ${agent}", true, true, false);

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "pp=ref " + AgentManager.MASTER_AGENT_NAME);
    }

    public void testResourcePropertyReferencesAgentName() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(AgentManager.MASTER_AGENT_NAME, resourceName);
        xmlRpcHelper.insertConfig(getPath(resourcePath, "properties"), xmlRpcHelper.createProperty("rp", "ref ${agent}", true, true, false));

        String projectName = random + "-project";
        ensureProject(projectName);
        xmlRpcHelper.insertConfig(getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "rp=ref " + AgentManager.MASTER_AGENT_NAME);
    }

    public void testSuppressedProperty() throws Exception
    {
        String projectName = random + "-project";
        ensureProject(projectName);
        String stagePath = getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "stages", "default");
        Hashtable<String, Object> defaultStage = xmlRpcHelper.getConfig(stagePath);
        defaultStage.put("agent", getPath(ConfigurationRegistry.AGENTS_SCOPE, AgentManager.MASTER_AGENT_NAME));
        xmlRpcHelper.saveConfig(stagePath, defaultStage, false);
        String suppressedName = "PULSE_TEST_SUPPRESSED";
        String suppressedValue = random + "-suppress";
        xmlRpcHelper.insertProjectProperty(projectName, suppressedName, suppressedValue, false, true, false);

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, AgentManager.MASTER_AGENT_NAME);
        goToEnv(projectName, 1);
        assertTextPresent(suppressedName);
        assertTextNotPresent(suppressedValue);
    }

    private void assertEnvironment(String projectName, int buildId, String... envs)
    {
        goToEnv(projectName, buildId);
        for(String env: envs)
        {
            assertTextPresent(env);
        }
    }

    private void goToEnv(String projectName, int buildId)
    {
        BuildDetailedViewPage detailedViewPage = new BuildDetailedViewPage(selenium, urls, projectName, buildId);
        detailedViewPage.goTo();
        detailedViewPage.clickCommand("default", "build");
        selenium.click("link=env.txt");
        selenium.waitForPageToLoad("10000");
    }

    private String addResource(String agent, String name) throws Exception
    {
        Hashtable<String, Object> resource = xmlRpcHelper.createDefaultConfig(Resource.class);
        resource.put("name", name);
        return xmlRpcHelper.insertConfig(getPath(ConfigurationRegistry.AGENTS_SCOPE, agent, "resources"), resource);
    }

    private Hashtable<String, Object> createRequiredResource(String resource, String version) throws Exception
    {
        Hashtable<String, Object> requirement = xmlRpcHelper.createDefaultConfig(ResourceRequirement.class);
        requirement.put("resource", resource);
        if (version != null)
        {
            requirement.put("version", version);
        }

        return requirement;
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
        ProjectHomePage home = new ProjectHomePage(selenium, urls, projectName);
        home.goTo();
        home.triggerBuild();
        home.waitFor();
        String statusId = IDs.buildStatusCell(projectName, 1);
        SeleniumUtils.refreshUntilElement(selenium, statusId, 30000);
        SeleniumUtils.refreshUntilText(selenium, statusId, "success", 30000);
        SeleniumUtils.assertText(selenium, IDs.stageAgentCell(projectName, 1, "default"), agent);
    }
}
