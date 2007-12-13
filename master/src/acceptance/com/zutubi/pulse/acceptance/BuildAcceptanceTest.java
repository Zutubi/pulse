package com.zutubi.pulse.acceptance;

import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.acceptance.forms.admin.BuildStageForm;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildDetailedViewPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectsPage;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.model.ResourceRequirement;

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

        xmlRpcHelper.insertConfig("projects/" + random + "/properties", createProperty("pname", "pvalue", false, true, false));

        triggerSuccessfulBuild(random, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(random, 1, "pname=pvalue", "PULSE_PNAME=pvalue", "PULSE_BUILD_NUMBER=1");
    }

    public void testImportedResources() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(AgentManager.MASTER_AGENT_NAME, resourceName);
        xmlRpcHelper.insertConfig(PathUtils.getPath(resourcePath, "properties"), createProperty("test-property", "test-value", false, false, false));

        String projectName = random + "-project";
        ensureProject(projectName);
        xmlRpcHelper.insertConfig(PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "PULSE_TEST-PROPERTY=test-value");
    }

    public void testProjectPropertyReferencesResourceProperty() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(AgentManager.MASTER_AGENT_NAME, resourceName);
        xmlRpcHelper.insertConfig(PathUtils.getPath(resourcePath, "properties"), createProperty("rp", "rv", false, false, false));

        String projectName = random + "-project";
        String projectPath = PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName);
        ensureProject(projectName);
        xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, "requirements"), createRequiredResource(resourceName, null));
        xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, "properties"), createProperty("pp", "ref ${rp}", true, true, false));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "pp=ref rv");
    }

    public void testResourcePropertyReferencesEarlierProperty() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(AgentManager.MASTER_AGENT_NAME, resourceName);
        String propertiesPath = PathUtils.getPath(resourcePath, "properties");
        xmlRpcHelper.insertConfig(propertiesPath, createProperty("referee", "ee", false, false, false));
        xmlRpcHelper.insertConfig(propertiesPath, createProperty("referer", "ref ${referee}", true, true, false));

        String projectName = random + "-project";
        ensureProject(projectName);
        xmlRpcHelper.insertConfig(PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "referer=ref ee");
    }

    public void testProjectPropertyReferencesAgentName() throws Exception
    {
        String projectName = random + "-project";
        ensureProject(projectName);
        String stagePath = PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "stages", "default");
        Hashtable<String, Object> defaultStage = xmlRpcHelper.getConfig(stagePath);
        defaultStage.put("agent", PathUtils.getPath(ConfigurationRegistry.AGENTS_SCOPE, AgentManager.MASTER_AGENT_NAME));
        xmlRpcHelper.saveConfig(stagePath, defaultStage, false);
        xmlRpcHelper.insertConfig(PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "properties"), createProperty("pp", "ref ${agent}", true, true, false));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "pp=ref " + AgentManager.MASTER_AGENT_NAME);
    }

    public void testResourcePropertyReferencesAgentName() throws Exception
    {
        String resourceName = random + "-resource";
        String resourcePath = addResource(AgentManager.MASTER_AGENT_NAME, resourceName);
        xmlRpcHelper.insertConfig(PathUtils.getPath(resourcePath, "properties"), createProperty("rp", "ref ${agent}", true, true, false));

        String projectName = random + "-project";
        ensureProject(projectName);
        xmlRpcHelper.insertConfig(PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "requirements"), createRequiredResource(resourceName, null));

        loginAsAdmin();
        triggerSuccessfulBuild(projectName, AgentManager.MASTER_AGENT_NAME);
        assertEnvironment(projectName, 1, "rp=ref " + AgentManager.MASTER_AGENT_NAME);
    }

    public void testSuppressedProperty() throws Exception
    {
        String projectName = random + "-project";
        ensureProject(projectName);
        String stagePath = PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "stages", "default");
        Hashtable<String, Object> defaultStage = xmlRpcHelper.getConfig(stagePath);
        defaultStage.put("agent", PathUtils.getPath(ConfigurationRegistry.AGENTS_SCOPE, AgentManager.MASTER_AGENT_NAME));
        xmlRpcHelper.saveConfig(stagePath, defaultStage, false);
        String suppressedName = "PULSE_TEST_SUPPRESSED";
        String suppressedValue = random + "-suppress";
        xmlRpcHelper.insertConfig(PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName, "properties"), createProperty(suppressedName, suppressedValue, false, true, false));

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
        return xmlRpcHelper.insertConfig(PathUtils.getPath(ConfigurationRegistry.AGENTS_SCOPE, agent, "resources"), resource);
    }

    private Hashtable<String, Object> createProperty(String propertyName, String propertyValue, boolean resolveVariables, boolean addToEnvironment, boolean addToPath) throws Exception
    {
        Hashtable<String, Object> property = xmlRpcHelper.createDefaultConfig(ResourceProperty.class);
        property.put("name", propertyName);
        property.put("value", propertyValue);
        property.put("resolveVariables", resolveVariables);
        property.put("addToEnvironment", addToEnvironment);
        property.put("addToPath", addToPath);
        return property;
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
