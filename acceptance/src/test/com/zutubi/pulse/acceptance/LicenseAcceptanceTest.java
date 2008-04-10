package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.*;
import com.zutubi.pulse.acceptance.pages.admin.AgentHierarchyPage;
import com.zutubi.pulse.acceptance.pages.admin.CompositePage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.admin.UsersPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.license.LicenseType;
import com.zutubi.pulse.license.config.LicenseConfiguration;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.test.LicenseHelper;
import com.zutubi.util.Constants;

import java.util.Date;
import java.util.Hashtable;

/**
 * Test for managing the server license and ensuring the licenses are
 * enforced.
 */
public class LicenseAcceptanceTest extends SeleniumTestBase
{
    private static final String LICENSE_PATH = "settings/licenseConfig";

    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();
        loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        try
        {
            setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.EVALUATION, "S. O. MeBody"));
        }
        finally
        {
            xmlRpcHelper.logout();
        }

        super.tearDown();
    }

    public void testChangeLicense()
    {
        CompositePage licensePage = goToLicensePage();
        setLicenseViaUI(LicenseHelper.newLicenseKey(LicenseType.EVALUATION, random, tomorrow()));

        waitForElement(licensePage.getStateFieldId("name"));
        assertEquals(random, licensePage.getStateField("name"));
    }

    public void testExpiresStateField()
    {
        // Field should be 'expiry' for eval and 'supportExpiry' for all
        // others.
        CompositePage licensePage = goToLicensePage();
        LicenseForm form = setLicenseViaUI(LicenseHelper.newLicenseKey(LicenseType.EVALUATION, random, tomorrow()));

        waitForElement(licensePage.getStateFieldId("expiry"));
        assertFalse(licensePage.isStateFieldPresent("supportExpiry"));

        form.waitFor();
        form.applyFormElements(LicenseHelper.newLicenseKey(LicenseType.ENTERPRISE, random, tomorrow()));

        waitForElement(licensePage.getStateFieldId("supportExpiry"));
        assertFalse(licensePage.isStateFieldPresent("expiry"));
    }

    public void testExpiredEvaluationLicense() throws Exception
    {
        goToLicensePage();
        setLicenseViaUI(LicenseHelper.newLicenseKey(LicenseType.EVALUATION, random, twoDaysAgo()));

        goTo("/");
        assertElementNotPresent("support-expired");
        assertElementPresent("license-expired");
        assertTextPresent("Your license has expired.");

        // Build triggers should be ignored
        xmlRpcHelper.insertSimpleProject(random, false);
        assertTriggersIgnored();
    }

    public void testExpiredCommercialLicense() throws Exception
    {
        goToLicensePage();
        setLicenseViaUI(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, random, twoDaysAgo()));

        goTo("/");
        assertElementNotPresent("license-expired");
        assertElementPresent("support-expired");
        assertTextPresent("support/upgrades have expired");

        // Build triggers should behave normally
        xmlRpcHelper.insertSimpleProject(random, false);
        assertTriggersHandled();
    }

    public void testExpiredCommercialLicenseInvalidUpgrade() throws Exception
    {
        goToLicensePage();
        setLicenseViaUI(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, random, new Date(System.currentTimeMillis() - 999999 * Constants.DAY)));

        goTo("/");
        assertElementNotPresent("license-expired");
        assertElementNotPresent("support-expired");
        assertElementPresent("license-cannot-run");
        assertTextPresent("Your license cannot run this version of Pulse, as it was released after the license expiry date.");

        // Build triggers should behave normally
        xmlRpcHelper.insertSimpleProject(random, false);
        assertTriggersIgnored();
    }

    public void testProjectsExceeded() throws Exception
    {
        xmlRpcHelper.insertSimpleProject(random, false);

        int projectCount = xmlRpcHelper.getProjectCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), projectCount - 1, -1, -1));

        assertExceeded();

        // Adding a project should fail
        AddProjectWizard.AntState state = runProjectWizard(random + "-2", false, ProjectManager.GLOBAL_PROJECT_NAME);
        state.waitFor();
        waitForStatus("Unable to add project: license limit exceeded");
    }

    public void testAgentsExceeded() throws Exception
    {
        int agentCount = xmlRpcHelper.getAgentCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), -1, agentCount - 1, -1));

        xmlRpcHelper.insertSimpleProject(random, false);
        assertExceeded();

        // Adding an agent should fail
        AgentHierarchyPage hierarchyPage = new AgentHierarchyPage(selenium, urls, AgentManager.GLOBAL_AGENT_NAME, true);
        hierarchyPage.goTo();
        hierarchyPage.clickAdd();
        AgentForm form = new AgentForm(selenium);
        form.waitFor();
        form.finishFormElements(random, "true", "localhost", "8090");
        form.waitFor();
        waitForStatus("Unable to add agent: license limit exceeded");
    }

    public void testUsersExceeded() throws Exception
    {
        int userCount = xmlRpcHelper.getUserCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), -1, -1, userCount - 1));

        xmlRpcHelper.insertSimpleProject(random, false);
        assertExceeded();

        // Adding a user should fail
        UsersPage usersPage = new UsersPage(selenium, urls);
        usersPage.goTo();
        usersPage.clickAdd();
        AddUserForm form = new AddUserForm(selenium);
        form.waitFor();
        form.finishFormElements(random, random, "false", "", "");
        form.waitFor();
        waitForStatus("Unable to add user: license limit exceeded");
    }

    public void testEnforcedViaRemoteApi() throws Exception
    {
        int projectCount = xmlRpcHelper.getProjectCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), projectCount - 1, -1, -1));
        try
        {
            xmlRpcHelper.insertTrivialProject(random, false);
            fail();
        }
        catch(Exception e)
        {
            assertTrue(e.getMessage().contains("Unable to add project: license limit exceeded"));
        }
    }

    public void testEnforcedOnClone() throws Exception
    {
        xmlRpcHelper.insertTrivialProject(random, false);
        int projectCount = xmlRpcHelper.getProjectCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), projectCount - 1, -1, -1));

        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, random, false);
        hierarchyPage.goTo();
        hierarchyPage.clickClone();

        CloneForm cloneForm = new CloneForm(selenium);
        cloneForm.waitFor();
        cloneForm.cloneFormElements(random + "clone");
        cloneForm.waitFor();
        waitForStatus("Unable to add project: license limit exceeded");
    }

    private Date tomorrow()
    {
        return new Date(System.currentTimeMillis() + Constants.DAY);
    }

    private Date twoDaysAgo()
    {
        return new Date(System.currentTimeMillis() - 2 * Constants.DAY);
    }

    private void setLicenseViaApi(String key) throws Exception
    {
        Hashtable<String, Object> license = xmlRpcHelper.createDefaultConfig(LicenseConfiguration.class);
        license.put("key", key);
        xmlRpcHelper.saveConfig(LICENSE_PATH, license, false);
    }

    private CompositePage goToLicensePage()
    {
        CompositePage licensePage = new CompositePage(selenium, urls, LICENSE_PATH);
        licensePage.goTo();
        return licensePage;
    }

    private LicenseForm setLicenseViaUI(String license)
    {
        LicenseForm form = new LicenseForm(selenium);
        form.waitFor();
        form.applyFormElements(license);
        return form;
    }

    private void assertExceeded() throws Exception
    {
        goTo("/");
        assertElementPresent("license-exceeded");
        assertTextPresent("Your license limits have been exceeded.");

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
        ProjectHomePage home = new ProjectHomePage(selenium, urls, random);
        home.goTo();
        home.triggerBuild();
        home.waitFor();
        String statusId = IDs.buildStatusCell(random, 1);
        if(ignored)
        {
            Thread.sleep(1000);
            assertElementNotPresent(statusId);
        }
        else
        {
            SeleniumUtils.refreshUntilElement(selenium, statusId, 30000);
            SeleniumUtils.refreshUntilText(selenium, statusId, "success", 30000);
        }
    }
}
