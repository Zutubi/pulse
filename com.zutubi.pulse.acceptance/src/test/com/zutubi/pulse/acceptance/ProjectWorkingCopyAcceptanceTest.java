package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.acceptance.windows.PulseFileSystemBrowserWindow;
import com.zutubi.pulse.core.scm.config.api.CheckoutScheme;
import com.zutubi.pulse.core.scm.svn.config.SubversionConfiguration;
import static com.zutubi.pulse.master.agent.AgentManager.MASTER_AGENT_NAME;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard.DEFAULT_RECIPE;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard.DEFAULT_STAGE;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

/**
 * Tests for the working copy functionality.
 */
public class ProjectWorkingCopyAcceptanceTest extends SeleniumTestBase
{
    private ConfigurationHelper configurationHelper;
    private ProjectConfigurations projects;
    private UserConfigurations users;
    private BuildRunner buildRunner;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        ConfigurationHelperFactory factory = new SingletonConfigurationHelperFactory();
        configurationHelper = factory.create(xmlRpcHelper);

        buildRunner = new BuildRunner(xmlRpcHelper);
        projects = new ProjectConfigurations(configurationHelper);
        users = new UserConfigurations();

        xmlRpcHelper.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        logout();

        super.tearDown();
    }

    public void testWorkingCopyLinkEnabledByIncrementalCheckoutScheme() throws Exception
    {
        ProjectConfiguration project = createProject(random);

        loginAsAdmin();

        ProjectHomePage homePage = browser.openAndWaitFor(ProjectHomePage.class, random);
        assertFalse(homePage.isViewWorkingCopyPresent());

        updateScmCheckoutScheme(project, CheckoutScheme.INCREMENTAL_UPDATE);

        homePage.openAndWaitFor();
        assertTrue(homePage.isViewWorkingCopyPresent());
    }

    public void testWorkingCopyLinkControlledByViewSourcePermissions() throws Exception
    {
        ProjectConfiguration project = createProject(random);
        updateScmCheckoutScheme(project, CheckoutScheme.INCREMENTAL_UPDATE);

        loginAsAdmin();

        ProjectHomePage homePage = browser.openAndWaitFor(ProjectHomePage.class, random);
        assertTrue(homePage.isViewWorkingCopyPresent());

        logout();

        // create user without view source permissions for the project and log in.
        UserConfiguration user = createUser(randomName());
        login(user.getName(), "");

        homePage.openAndWaitFor();
        assertFalse(homePage.isViewWorkingCopyPresent());
    }

    public void testViewWorkingCopyOnMasterAgent() throws Exception
    {
        runTestViewWorkingCopyOnAgent(MASTER_AGENT_NAME);
    }

    public void testViewWorkingCopyOnRemoteAgent() throws Exception
    {
        xmlRpcHelper.ensureAgent(AGENT_NAME);
        runTestViewWorkingCopyOnAgent(AGENT_NAME);
    }

    private void runTestViewWorkingCopyOnAgent(String agentName) throws Exception
    {
        ProjectConfiguration project = createProject(random, agentName);
        updateScmCheckoutScheme(project, CheckoutScheme.INCREMENTAL_UPDATE);
        buildRunner.triggerSuccessfulBuild(project);

        loginAsAdmin();

        ProjectHomePage homePage = browser.openAndWaitFor(ProjectHomePage.class, random);
        assertTrue(homePage.isViewWorkingCopyPresent());

        PulseFileSystemBrowserWindow window = homePage.viewWorkingCopy();
        window.waitForLoading();

        assertEquals("browse working copy", window.getHeader());
        assertTrue(window.isNodePresent("stage :: " + DEFAULT_STAGE + " :: [" + DEFAULT_RECIPE + "]@" + agentName));

        window.clickCancel();
        assertFalse(window.isWindowPresent());
    }

    public void testViewWorkingCopyBeforeFirstBuild() throws Exception
    {
        ProjectConfiguration project = createProject(random);
        updateScmCheckoutScheme(project, CheckoutScheme.INCREMENTAL_UPDATE);

        loginAsAdmin();

        ProjectHomePage homePage = browser.openAndWaitFor(ProjectHomePage.class, random);
        assertTrue(homePage.isViewWorkingCopyPresent());

        PulseFileSystemBrowserWindow window = homePage.viewWorkingCopy();
        window.waitForLoading();

        assertEquals("browse working copy", window.getHeader());
        assertFalse(window.isNodePresent("stage :: default :: [default]@master"));
        assertEquals("An error has occured", window.getStatus());

        window.clickCancel();
        assertFalse(window.isWindowPresent());
    }

/*
    public void testViewWorkingCopyWhereAgentIsOffline()
    {
        // Awkward to test since we need control of an agent process.
        // a) we need it online so we can run the build.
        // b) we then need it to go offline so that we can run the test.
    }
*/

    private ProjectConfiguration createProject(String projectName) throws Exception
    {
        return createProject(projectName, MASTER_AGENT_NAME);
    }

    private ProjectConfiguration createProject(String projectName, String targetAgentName) throws Exception
    {
        AgentConfiguration targetAgent = configurationHelper.getAgentReference(targetAgentName);

        AntProjectHelper project = projects.createTrivialAntProject(projectName);
        project.getStage(DEFAULT_STAGE).setAgent(targetAgent);
        configurationHelper.insertProject(project.getConfig());
        return project.getConfig();
    }

    private UserConfiguration createUser(String name) throws Exception
    {
        UserConfiguration user = users.createSimpleUser(name);
        configurationHelper.insertUser(user);
        return user;
    }

    private void updateScmCheckoutScheme(ProjectConfiguration project, CheckoutScheme scheme) throws Exception
    {
        SubversionConfiguration scm = (SubversionConfiguration) project.getScm();
        scm.setCheckoutScheme(scheme);
        configurationHelper.update(scm);
    }
}
