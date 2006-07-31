package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.CreateUserForm;
import com.zutubi.pulse.acceptance.forms.LicenseEditForm;
import com.zutubi.pulse.acceptance.forms.LoginForm;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseType;
import com.zutubi.pulse.test.LicenseHelper;
import junit.framework.Test;
import junit.framework.TestSuite;
import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class LicenseAuthorisationAcceptanceTest extends BaseAcceptanceTestCase
{
    public static Test suite()
    {
        TestSuite testSuite = new TestSuite(LicenseAuthorisationAcceptanceTest.class);
        return new LicenseAuthorisationSetup(testSuite);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        beginAt("/");
        loginAsAdmin();
    }

    public void testAddProjectLinkOnlyAvailableWhenLicensed() throws Exception
    {
        // verify that the link is initially available.
        clickLinkWithText("projects");
        assertLinkPresentWithText("add new project");

        // configure a license that supports 0 projects.
        installLicense(tester, new License(LicenseType.CUSTOM, "holder").setSupportedProjects(0));

        // verify that add project wizard link is not available.
        clickLinkWithText("projects");
        assertLinkNotPresentWithText("add new project");

        // verify that add project wizard action can not be triggered directly.
        goTo("/addProject!input.action");
        assertTextPresent("Not licensed");
    }

    public void testCreateUserFormOnlyAvailableWhenLicensed() throws Exception
    {
        // verify that the form is available.
        CreateUserForm form = new CreateUserForm(tester);

        clickLinkWithText("administration");
        clickLinkWithText("users");
        form.assertFormPresent();

        // configure a license that supports 0 users.
        installLicense(tester, new License(LicenseType.CUSTOM, "holder").setSupportedUsers(0));

        // verify create user form is not available.
        clickLinkWithText("administration");
        clickLinkWithText("users");
        form.assertFormNotPresent();

        // verify that we can not post directly to the create user action.
        goTo("/admin/createUser.action");
        assertTextPresent("Not licensed");
    }

    public void testAddAgentLinkOnlyAvailableWhenLicensed() throws Exception
    {
        // firstly, verify that the add agent link exists.
        clickLinkWithText("agents");
        assertLinkPresentWithText("add new agent");

        // configure a license that supports 0 users.
        installLicense(tester, new License(LicenseType.CUSTOM, "holder").setSupportedAgents(0));

        // verify that the link is no longer available.
        clickLinkWithText("agents");
        assertLinkNotPresentWithText("add new agent");

        // verify that we can not post directly to the add agent action.
        goTo("/admin/addAgent.action");
        assertTextPresent("Not licensed");
    }

    private static void installLicense(WebTester tester, License l) throws Exception
    {
        String licenseKey = LicenseHelper.newLicenseKey(l);

        // navigate to admin license update.
        tester.beginAt("/");
        tester.clickLinkWithText("administration");
        tester.clickLink("license.edit");

        LicenseEditForm form = new LicenseEditForm(tester);
        form.assertFormPresent();
        form.saveFormElements(licenseKey);
        form.assertFormNotPresent();
    }

    /**
     *
     *
     */
    public static class LicenseAuthorisationSetup extends WebTestSetup
    {
        public LicenseAuthorisationSetup(Test test)
        {
            super(test);
        }

        protected void setUp() throws Exception
        {
            License l = new License(LicenseType.CUSTOM, "tester");
            loginAsAdmin();
            installLicense(tester, l);
            logout();
        }

        protected void tearDown() throws Exception
        {
            License l = new License(LicenseType.CUSTOM, "tester");
            loginAsAdmin();
            installLicense(tester, l);
            logout();
        }

        protected void loginAsAdmin()
        {
            beginAt("/login.action");
            LoginForm loginForm = new LoginForm(tester);
            loginForm.loginFormElements("admin", "admin", "false");
        }

        protected void logout()
        {
            beginAt("/");
            clickLink("logout");
        }

    }
}
