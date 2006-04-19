/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.setup.CreateAdminForm;
import com.zutubi.pulse.acceptance.forms.setup.ServerSettingsForm;
import com.zutubi.pulse.acceptance.forms.setup.SetPulseHomeForm;
import com.zutubi.pulse.acceptance.forms.setup.PulseLicenseForm;
import com.zutubi.pulse.test.LicenseHelper;

/**
 * A setup test that covers the systems setup procedure.
 * <p/>
 * This setup test is a little awkward since we can only run it once. Once done, the
 * server is setup and will not take kindly to us trying to set it up again. So, rather than
 * having multiple test methods, there is one testSetupProcess method that is breaks up the setup
 * process and handles all of the validation testing as it goes.
 */
public class SetupAcceptanceTest extends BaseAcceptanceTest
{
    public String licenseKey;
    public String expiredLicenseKey;

    public static final String INVALID_LICENSE_KEY =
            "AAAAaXNvbUgY29tcGFueSBhbmQgc29tZSBvdGhlciBkYXRhIHRoYXQgd2ls\n" +
                    "bBjb21lIGZyb20gb3RoZXIgZmllbGRzIgluIHRoZSBmdXR1cmUuCjIwMDYt\n" +
                    "MQtMTggMTA6Mjc6MTUgRVNUCk+U6cAyaORQLoB7r+IDKasLJjamRU7YMYjm\n" +
                    "sT2VU3Mz5ZY67+fAc5o35/TjNnrWEqkhdha36DdMx5+0ADEAMh/UvE8E2hk\n" +
                    "iA0MLr3lR3etbPQKn3PYFLhSM6C3CSXU5V9a9uclezgQIkxP+/eoJdOYeAy\n" +
                    "Q/Zs7NGNWH+TB79";

    protected void setUp() throws Exception
    {
        super.setUp();

        licenseKey = LicenseHelper.newLicenseKey("dummy", "S. O. MeBody");
        expiredLicenseKey = LicenseHelper.newExpiredLicenseKey("dummy", "S. O. MeBody");
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testSetupProcess()
    {
        // first we deal with the pulse home property configuration.
        beginAt("/");

        // step one. setting the pulse home variable.
        checkSetPulseHome();

        // step two. setting the license details.
        checkLicenseDetails();

        // step three. creating the administration user.
        checkCreateAdmin();

        // check that any attempts to bypass the setup fail.
        //beginAt("/");

        // step four. configuring the server essentials.
        checkServerSettings();

        // one complete, we should see the home page, and it should contain the following:
        assertTextPresent(":: welcome ::");
        assertTextPresent("A. D. Ministrator");
        assertLinkPresentWithText("logout");

        // lets also take this opportunity to verify that what was set during the setup wizard
        // was actually persisted.
    }

    private void checkServerSettings()
    {
        ServerSettingsForm settingsForm = new ServerSettingsForm(tester);
        settingsForm.assertFormPresent();

        // local host is required, and while we are at it, verify that all of the other form fields
        // are correctly returned to the user when there is a validation failure.
        settingsForm.nextFormElements("", "some.smtp.host.com", "from@some.host.com", "username", "password", "prefix");
        settingsForm.assertFormElements("", "some.smtp.host.com", "from@some.host.com", "username", "password", "prefix");
        assertTextPresent("required");

        // if smtp host is set, then smtp from is also required.
        settingsForm.nextFormElements("localhost:8080", "some.smtp.host.com", "", "", "", "");
        settingsForm.assertFormElements("localhost:8080", "some.smtp.host.com", "", "", "", "");
        assertTextPresent("required");

        // smtp from must be valid ??.
//        settingsForm.nextFormElements("localhost:8080", "some.smtp.host.com", "invalid from address", "", "");
//        settingsForm.assertFormElements("localhost:8080", "some.smtp.host.com", "invalid from address", "", "");
//        assertTextPresent("required");

        settingsForm.nextFormElements("localhost:8080", "some.smtp.host.com", "from@some.host.com", "username", "password", "prefix");
    }

    private void checkLicenseDetails()
    {
        PulseLicenseForm licenseForm = new PulseLicenseForm(tester);

        licenseForm.assertFormPresent();

        // check that license is required.
        licenseForm.nextFormElements("");
        licenseForm.assertFormPresent();
        licenseForm.assertFormElements("");
        assertTextPresent("required");

        // check that license validation works.
        licenseForm.nextFormElements(INVALID_LICENSE_KEY);
        licenseForm.assertFormPresent();
        licenseForm.assertFormElements(INVALID_LICENSE_KEY);
        assertTextPresent("invalid");

        // check that an expired license is not accepted.
        licenseForm.nextFormElements(expiredLicenseKey);
        licenseForm.assertFormPresent();
        licenseForm.assertFormElements(expiredLicenseKey);
        assertTextPresent("expired");

        // enter a valid license.
        licenseForm.nextFormElements(licenseKey);
        licenseForm.assertFormNotPresent();
    }

    private void checkCreateAdmin()
    {
        CreateAdminForm createAdminForm = new CreateAdminForm(tester);

        // create admin.
        createAdminForm.assertFormPresent();

        // check validation on the form.
        createAdminForm.nextFormElements("", "A. D. Ministrator", "admin", "admin");
        createAdminForm.assertFormElements("", "A. D. Ministrator", "", "");
        assertTextPresent("required");

        // - no name
        createAdminForm.nextFormElements("admin", "", "admin", "admin");
        createAdminForm.assertFormElements("admin", "", "", "");
        assertTextPresent("required");

        // - no password
        createAdminForm.nextFormElements("admin", "A. D. Ministrator", "", "admin");
        createAdminForm.assertFormElements("admin", "A. D. Ministrator", "", "");
        assertTextPresent("required");

        // - password and confirmation do not match
        createAdminForm.nextFormElements("admin", "A. D. Ministrator", "admin", "something other then pass");
        createAdminForm.assertFormElements("admin", "A. D. Ministrator", "", "");

        // now create the administrator.
        createAdminForm.nextFormElements("admin", "A. D. Ministrator", "admin", "admin");
    }

    private void checkSetPulseHome()
    {
        SetPulseHomeForm homeForm = new SetPulseHomeForm(tester);

        homeForm.assertFormPresent();

        // ensure that we have a default value for the pulseHome property.
        assertFormElementNotEmpty("home");
        // record the default value for later use.
        String defaultHome = getFormValue("home");

        // check the validation - an empty pulse home.
        homeForm.nextFormElements("");
        // assert that we are still on the same page.
        homeForm.assertFormElements("");

        // check validation - an invalid pulse home value.

        // enter valid pulse home that does not exist.
        homeForm.nextFormElements(defaultHome);

        // it should prompt for confirmation to create the directory....
    }
}
