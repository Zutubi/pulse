package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.*;
import com.zutubi.pulse.acceptance.pages.WelcomePage;
import com.zutubi.pulse.acceptance.pages.admin.AgentHierarchyPage;
import com.zutubi.pulse.acceptance.pages.admin.LicensePage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.admin.UsersPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.license.LicenseHelper;
import com.zutubi.pulse.master.license.LicenseType;
import com.zutubi.pulse.master.license.config.LicenseConfiguration;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.util.Constants;

import java.util.Date;
import java.util.Hashtable;

import static com.zutubi.util.CollectionUtils.asPair;

/**
 * Test for managing the server license and ensuring the licenses are
 * enforced.
 */
public class LicenseAcceptanceTest extends AcceptanceTestBase
{
    private static final String LICENSE_PATH = "settings/license";
    private static final int BUILD_TIMEOUT = 90000;

    protected void setUp() throws Exception
    {
        super.setUp();
        
        rpcClient.loginAsAdmin();
        getBrowser().loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        // ensure that we return back to an unrestricted license.
        try
        {
            setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.EVALUATION, "S. O. MeBody"));
        }
        finally
        {
            rpcClient.logout();
        }

        super.tearDown();
    }

    public void testChangeLicenseViaWebUI()
    {
        LicensePage licensePage = goToLicensePage();
        setLicenseViaUI(LicenseHelper.newLicenseKey(LicenseType.EVALUATION, random, tomorrow()));

        // wait for the panel to reload.
        licensePage.waitFor();
        assertEquals(random, licensePage.getName());
    }

    public void testChangeLicenseViaRemoteApi() throws Exception
    {
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.EVALUATION, random, tomorrow()));

        LicensePage licensePage = goToLicensePage();
        assertEquals(random, licensePage.getName());
    }

    public void testExpiresStateField()
    {
        // Field should be 'expiry' for eval and 'supportExpiry' for all others.
        LicensePage licensePage = goToLicensePage();
        setLicenseViaUI(LicenseHelper.newLicenseKey(LicenseType.EVALUATION, random, tomorrow()));

        licensePage.waitFor();
        assertTrue(licensePage.isExpiryPresent());
        assertFalse(licensePage.isSupportExpiryPresent());

        setLicenseViaUI(LicenseHelper.newLicenseKey(LicenseType.ENTERPRISE, random, tomorrow()));

        licensePage.waitFor();
        assertTrue(licensePage.isSupportExpiryPresent());
        assertFalse(licensePage.isExpiryPresent());
    }

    public void testExpiredEvaluationLicense() throws Exception
    {
        goToLicensePage();
        setLicenseViaUI(LicenseHelper.newLicenseKey(LicenseType.EVALUATION, random, twoDaysAgo()));

        getBrowser().open(urls.base());
        getBrowser().waitForElement("license-expired");
        getBrowser().waitForTextPresent("Your license has expired.");
        assertFalse(getBrowser().isElementIdPresent("support-expired"));

        // Build triggers should be ignored
        assertTriggersIgnored();
    }

    public void testExpiredCommercialLicense() throws Exception
    {
        goToLicensePage();
        setLicenseViaUI(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, random, twoDaysAgo()));

        getBrowser().openAndWaitFor(WelcomePage.class);
        getBrowser().refreshUntilElement("support-expired", BUILD_TIMEOUT);

        getBrowser().waitForTextPresent("support/upgrades have expired");
        assertFalse(getBrowser().isElementIdPresent("license-expired"));

        // Build triggers should behave normally
        assertTriggersHandled();
    }

    public void testExpiredCommercialLicenseInvalidUpgrade() throws Exception
    {
        goToLicensePage();
        setLicenseViaUI(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, random, manyYearsAgo()));

        getBrowser().open(urls.base());
        getBrowser().waitForElement("license-cannot-run");
        getBrowser().waitForTextPresent("Your license cannot run this version of Pulse, as it was released after the license expiry date.");
        assertFalse(getBrowser().isElementIdPresent("license-expired"));
        assertFalse(getBrowser().isElementIdPresent("support-expired"));

        // Build triggers should behave normally
        assertTriggersIgnored();
    }

    public void testProjectsExceeded() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random, false);

        int projectCount = rpcClient.RemoteApi.getProjectCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), projectCount - 1, -1, -1));

        assertExceeded();

        assertCanNotAddProjectViaApi(random + "B");
        assertCanNotAddProjectViaWeb(random + "C");
    }

    public void testCanNotAddProjectsPastLicensedLimit() throws Exception
    {
        int projectCount = rpcClient.RemoteApi.getProjectCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), projectCount + 1, -1, -1));

        rpcClient.RemoteApi.insertSimpleProject(random + "A", false);

        assertCanNotAddProjectViaApi(random + "B");
        assertCanNotAddProjectViaWeb(random + "B");
    }

    public void testDeletingProjectsOnExceededLicenseRefreshesAuthorisations() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random, false);
        rpcClient.RemoteApi.insertSimpleProject(random + "B", false);

        int projectCount = rpcClient.RemoteApi.getProjectCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), projectCount -1, -1, -1));

        assertExceeded();

        rpcClient.RemoteApi.deleteConfig("projects/" + random + "B");
        rpcClient.RemoteApi.deleteConfig("projects/" + random);

        assertCanAddProjectViaApi(random + "C");
    }

    public void testAgentsExceeded() throws Exception
    {
        int agentCount = rpcClient.RemoteApi.getAgentCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), -1, agentCount - 1, -1));

        assertExceeded();

        assertCanNotAddAgentViaApi(random + "A");
        assertCanNotAddAgentViaWeb(random + "B");
    }

    public void testCanNotAddAgentsPastLicensedLimit() throws Exception
    {
        int agentCount = rpcClient.RemoteApi.getAgentCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), -1, agentCount + 1, -1));

        rpcClient.RemoteApi.insertSimpleAgent(random + "A");

        assertCanNotAddAgentViaApi(random + "B");
        assertCanNotAddAgentViaWeb(random + "C");
    }

    public void testDeletingAgentsOnExceededLicenseRefreshesAuthorisations() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleAgent(random + "A");
        rpcClient.RemoteApi.insertSimpleAgent(random + "B");

        int agentCount = rpcClient.RemoteApi.getAgentCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), -1, agentCount - 1, -1));

        assertExceeded();

        rpcClient.RemoteApi.deleteConfig("agents/" + random + "B");
        rpcClient.RemoteApi.deleteConfig("agents/" + random + "A");

        assertCanAddAgentViaApi(random + "C");
    }

    public void testUsersExceeded() throws Exception
    {
        int userCount = rpcClient.RemoteApi.getUserCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), -1, -1, userCount - 1));

        assertExceeded();

        assertCanNotAddUserViaApi(random + "C");
        assertCanNotAddUserViaWeb(random + "C");
    }

    public void testCanNotAddUsersPastLicensedLimit() throws Exception
    {
        int userCount = rpcClient.RemoteApi.getUserCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), -1, -1, userCount + 1));

        rpcClient.RemoteApi.insertTrivialUser(random + "A");

        assertCanNotAddUserViaApi(random + "B");
        assertCanNotAddUserViaWeb(random + "C");
    }

    public void testDeletingUsersOnExceededLicenseRefreshesAuthorisations() throws Exception
    {
        rpcClient.RemoteApi.insertTrivialUser(random + "A");
        rpcClient.RemoteApi.insertTrivialUser(random + "B");

        int userCount = rpcClient.RemoteApi.getUserCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), -1, -1, userCount - 1));

        assertExceeded();

        rpcClient.RemoteApi.deleteConfig("users/" + random + "B");
        rpcClient.RemoteApi.deleteConfig("users/" + random + "A");

        assertCanAddUserViaApi(random + "C");
    }

    public void testEnforcedViaRemoteApi() throws Exception
    {
        int projectCount = rpcClient.RemoteApi.getProjectCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), projectCount - 1, -1, -1));
        try
        {
            rpcClient.RemoteApi.insertTrivialProject(random, false);
            fail();
        }
        catch(Exception e)
        {
            assertTrue(e.getMessage().contains("Unable to add project: license limit exceeded"));
        }
    }

    public void testEnforcedOnClone() throws Exception
    {
        rpcClient.RemoteApi.insertTrivialProject(random, false);
        int projectCount = rpcClient.RemoteApi.getProjectCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), projectCount - 1, -1, -1));

        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, random, false);
        hierarchyPage.clickClone();

        CloneForm cloneForm = getBrowser().createForm(CloneForm.class, false);
        cloneForm.waitFor();
        cloneForm.cloneFormElements(random + "clone");
        cloneForm.waitFor();
        getBrowser().waitForTextPresent("Unable to add project: license limit exceeded");
    }

    private Date tomorrow()
    {
        return new Date(System.currentTimeMillis() + Constants.DAY);
    }

    private Date twoDaysAgo()
    {
        return new Date(System.currentTimeMillis() - 2 * Constants.DAY);
    }

    private Date manyYearsAgo()
    {
        return new Date(System.currentTimeMillis() - 100 * Constants.YEAR);
    }

    private LicensePage goToLicensePage()
    {
        return getBrowser().openAndWaitFor(LicensePage.class);
    }

    private void setLicenseViaApi(String key) throws Exception
    {
        Hashtable<String, Object> license = rpcClient.RemoteApi.createDefaultConfig(LicenseConfiguration.class);
        license.put("key", key);
        rpcClient.RemoteApi.saveConfig(LICENSE_PATH, license, false);
    }

    private LicenseForm setLicenseViaUI(String license)
    {
        LicenseForm form = getBrowser().createForm(LicenseForm.class);
        form.waitFor();
        form.applyFormElements(license);
        return form;
    }

    private void assertCanNotAddAgentViaApi(String name)
    {
        try
        {
            rpcClient.RemoteApi.insertSimpleAgent(name);
            fail("Expected exception: Unable to add agent: license limit exceeded");
        }
        catch (Exception e)
        {
            assertTrue(e.getMessage().contains("Unable to add agent: license limit exceeded"));
        }
    }

    private void assertCanAddAgentViaApi(String name)
    {
        try
        {
            rpcClient.RemoteApi.insertSimpleAgent(name);
        }
        catch (Exception e)
        {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    private void assertCanNotAddAgentViaWeb(String name)
    {
        AgentHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(AgentHierarchyPage.class, AgentManager.GLOBAL_AGENT_NAME, true);
        hierarchyPage.clickAdd();
        AgentForm form = getBrowser().createForm(AgentForm.class, true);
        form.waitFor();
        form.finishNamedFormElements(asPair("name", name), asPair("host", "localhost"));
        form.waitFor();
        getBrowser().waitForTextPresent("Unable to add agent: license limit exceeded");
    }

    private void assertCanNotAddUserViaWeb(String name)
    {
        UsersPage usersPage = getBrowser().openAndWaitFor(UsersPage.class);
        usersPage.clickAdd();
        AddUserForm form = getBrowser().createForm(AddUserForm.class);
        form.waitFor();
        form.finishNamedFormElements(asPair("login", name), asPair("name", name), asPair("emailAddress", name + "@example.com"));
        form.waitFor();
        getBrowser().waitForTextPresent("Unable to add user: license limit exceeded");
    }

    private void assertCanAddUserViaApi(String name)
    {
        try
        {
            rpcClient.RemoteApi.insertTrivialUser(name);
        }
        catch (Exception e)
        {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    private void assertCanNotAddUserViaApi(String name)
    {
        try
        {
            rpcClient.RemoteApi.insertTrivialUser(name);
            fail("Expected exception: Unable to add user: license limit exceeded");
        }
        catch (Exception e)
        {
            assertTrue(e.getMessage().contains("Unable to add user: license limit exceeded"));
        }
    }

    private void assertCanNotAddProjectViaWeb(String name)
    {
        AddProjectWizard wizard = new AddProjectWizard(getBrowser(), rpcClient.RemoteApi);
        AddProjectWizard.CommandState state = wizard.runAddProjectWizard(new AddProjectWizard.DefaultProjectWizardDriver(ProjectManager.GLOBAL_PROJECT_NAME, name, false));
        state.waitFor();
        getBrowser().waitForTextPresent("Unable to add project: license limit exceeded");
    }

    private void assertCanAddProjectViaApi(String name)
    {
        try
        {
            rpcClient.RemoteApi.insertTrivialProject(name, false);
        }
        catch (Exception e)
        {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    private void assertCanNotAddProjectViaApi(String name)
    {
        try
        {
            rpcClient.RemoteApi.insertTrivialProject(name, false);
            fail("Expected exception: Unable to add project: license limit exceeded");
        }
        catch (Exception e)
        {
            assertTrue(e.getMessage().contains("Unable to add project: license limit exceeded"));
        }
    }

    private void assertExceeded() throws Exception
    {
        getBrowser().open(urls.base());
        getBrowser().refreshUntilElement("license-exceeded");
        getBrowser().waitForTextPresent("Your license limits have been exceeded.");

        // No builds for you!
        assertTriggersIgnored();
    }

    private void assertTriggersHandled() throws Exception
    {
        assertTriggers(false);
    }

    private void assertTriggersIgnored() throws Exception
    {
        assertTriggers(true);
    }

    private void assertTriggers(boolean ignored) throws Exception
    {
        rpcClient.RemoteApi.ensureProject(random);

        ProjectHomePage home = getBrowser().openAndWaitFor(ProjectHomePage.class, random);
        home.triggerBuild();
        home.waitFor();
        if (ignored)
        {
            Thread.sleep(Constants.SECOND);
            assertFalse(home.hasBuildActivity());
            assertFalse(home.hasCompletedBuild());
        }
        else
        {
            assertEquals(ResultState.SUCCESS, home.waitForLatestCompletedBuild(1, BUILD_TIMEOUT));
        }
    }
}
