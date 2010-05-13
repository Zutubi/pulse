package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.*;
import com.zutubi.pulse.acceptance.pages.admin.AgentHierarchyPage;
import com.zutubi.pulse.acceptance.pages.admin.CompositePage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.admin.UsersPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
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
// the xmlrpc tests create the project/user/agent data we need for these tests to work, so lets depend on them.
public class LicenseAcceptanceTest extends SeleniumTestBase
{
    private static final String LICENSE_PATH = "settings/license";
    private static final int BUILD_TIMEOUT = 90000;

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

        browser.waitForElement(licensePage.getStateFieldId("name"));
        assertEquals(random, licensePage.getStateField("name"));
    }

    public void testExpiresStateField()
    {
        // Field should be 'expiry' for eval and 'supportExpiry' for all
        // others.
        CompositePage licensePage = goToLicensePage();
        LicenseForm form = setLicenseViaUI(LicenseHelper.newLicenseKey(LicenseType.EVALUATION, random, tomorrow()));

        browser.waitForElement(licensePage.getStateFieldId("expiry"));
        assertFalse(licensePage.isStateFieldPresent("supportExpiry"));

        form.waitFor();
        form.applyFormElements(LicenseHelper.newLicenseKey(LicenseType.ENTERPRISE, random, tomorrow()));

        browser.waitForElement(licensePage.getStateFieldId("supportExpiry"));
        assertFalse(licensePage.isStateFieldPresent("expiry"));
    }

    public void testExpiredEvaluationLicense() throws Exception
    {
        goToLicensePage();
        setLicenseViaUI(LicenseHelper.newLicenseKey(LicenseType.EVALUATION, random, twoDaysAgo()));

        browser.open(urls.base());
        browser.waitForElement("license-expired");
        assertTextPresent("Your license has expired.");
        assertElementNotPresent("support-expired");

        // Build triggers should be ignored
        xmlRpcHelper.insertSimpleProject(random, false);
        assertTriggersIgnored();
    }

    public void testExpiredCommercialLicense() throws Exception
    {
        goToLicensePage();
        setLicenseViaUI(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, random, twoDaysAgo()));

        browser.open(urls.base());
        browser.waitForElement("support-expired");
        assertTextPresent("support/upgrades have expired");
        assertElementNotPresent("license-expired");

        // Build triggers should behave normally
        xmlRpcHelper.insertSimpleProject(random, false);
        assertTriggersHandled();
    }

    public void testExpiredCommercialLicenseInvalidUpgrade() throws Exception
    {
        goToLicensePage();
        setLicenseViaUI(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, random, new Date(System.currentTimeMillis() - 999999 * Constants.DAY)));

        browser.open(urls.base());
        browser.waitForElement("license-cannot-run");
        assertTextPresent("Your license cannot run this version of Pulse, as it was released after the license expiry date.");
        assertElementNotPresent("license-expired");
        assertElementNotPresent("support-expired");

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
        AddProjectWizard.CommandState state = runAddProjectWizard(new DefaultProjectWizardDriver(ProjectManager.GLOBAL_PROJECT_NAME, random+"-2", false));
        state.waitFor();
        assertTextPresent("Unable to add project: license limit exceeded");
    }

    public void testAgentsExceeded() throws Exception
    {
        int agentCount = xmlRpcHelper.getAgentCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), -1, agentCount - 1, -1));

        xmlRpcHelper.insertSimpleProject(random, false);
        assertExceeded();

        // Adding an agent should fail
        AgentHierarchyPage hierarchyPage = browser.openAndWaitFor(AgentHierarchyPage.class, AgentManager.GLOBAL_AGENT_NAME, true);
        hierarchyPage.clickAdd();
        AgentForm form = browser.createForm(AgentForm.class, true);
        form.waitFor();
        form.finishNamedFormElements(asPair("name", random), asPair("host", "localhost"));
        form.waitFor();
        assertTextPresent("Unable to add agent: license limit exceeded");
    }

    public void testUsersExceeded() throws Exception
    {
        int userCount = xmlRpcHelper.getUserCount();
        setLicenseViaApi(LicenseHelper.newLicenseKey(LicenseType.CUSTOM, "me", tomorrow(), -1, -1, userCount - 1));

        xmlRpcHelper.insertSimpleProject(random, false);
        assertExceeded();

        // Adding a user should fail
        UsersPage usersPage = browser.openAndWaitFor(UsersPage.class);
        usersPage.clickAdd();
        AddUserForm form = browser.createForm(AddUserForm.class);
        form.waitFor();
        form.finishNamedFormElements(asPair("login", random), asPair("name", random), asPair("emailAddress", random + "@example.com"));
        form.waitFor();
        assertTextPresent("Unable to add user: license limit exceeded");
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

        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, random, false);
        hierarchyPage.clickClone();

        CloneForm cloneForm = browser.createForm(CloneForm.class, false);
        cloneForm.waitFor();
        cloneForm.cloneFormElements(random + "clone");
        cloneForm.waitFor();
        assertTextPresent("Unable to add project: license limit exceeded");
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
        return browser.openAndWaitFor(CompositePage.class, LICENSE_PATH);
    }

    private LicenseForm setLicenseViaUI(String license)
    {
        LicenseForm form = browser.createForm(LicenseForm.class);
        form.waitFor();
        form.applyFormElements(license);
        return form;
    }

    private void assertExceeded() throws Exception
    {
        browser.open(urls.base());
        browser.refreshUntilElement("license-exceeded");
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
        ProjectHomePage home = browser.openAndWaitFor(ProjectHomePage.class, random);
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
            browser.refreshUntilElement(statusId, BUILD_TIMEOUT);
            browser.refreshUntilText(statusId, BUILD_TIMEOUT, "success");
        }
    }
}
