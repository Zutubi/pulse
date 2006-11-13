package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.setup.CreateAdminForm;
import com.zutubi.pulse.acceptance.forms.setup.PulseLicenseForm;
import com.zutubi.pulse.acceptance.forms.setup.ServerSettingsForm;
import com.zutubi.pulse.acceptance.forms.setup.SetPulseDataForm;
import com.zutubi.pulse.license.LicenseType;
import com.zutubi.pulse.test.LicenseHelper;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * A setup test that covers the systems setup procedure.
 * <p/>
 * This setup test is a little awkward since we can only run it once. Once done, the
 * server is setup and will not take kindly to us trying to set it up again. So, rather than
 * having multiple test methods, there is one testSetupProcess method that is breaks up the setup
 * process and handles all of the validation testing as it goes.
 */
public class SetupAcceptanceTest extends BaseAcceptanceTestCase
{
    public String licenseKey;
    public String expiredLicenseKey;
    public String invalidLicenseKey;

    protected void setUp() throws Exception
    {
        super.setUp();

        licenseKey = LicenseHelper.newLicenseKey(LicenseType.EVALUATION, "S. O. MeBody");
        expiredLicenseKey = LicenseHelper.newExpiredLicenseKey(LicenseType.EVALUATION, "S. O. MeBody");
        invalidLicenseKey = LicenseHelper.newInvalidLicenseKey(LicenseType.EVALUATION, "S. O. MeBody");
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testSetupProcess() throws InterruptedException, IOException, SAXException
    {
        // first we deal with the pulse home property configuration.
        beginAt("/");

        // step one. setting the pulse home variable.
        checkSetPulseData();

        // step two. setting the license details.
        checkLicenseDetails();

        // step three. creating the administration user.
        checkCreateAdmin();

        // check that any attempts to bypass the setup fail.
        //beginAt("/");

        // step four. configuring the server essentials.
        checkServerSettings();

        // step five. setup in progress - simulate the auto refresh of the browser.
        assertTextPresent("system setup");
        pauseWhileMetaRefreshActive();

        // one complete, we should see the home page, and it should contain the following:
        assertTextPresent(":: welcome ::");
        assertTextPresent("A. D. Ministrator");
        assertLinkPresent(Navigation.LINK_LOGOUT);

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

        // ensure that the base url setting is a valid url.
        settingsForm.nextFormElements("localhost:8080", "", "", "", "", "");
        settingsForm.assertFormElements("localhost:8080", "", "", "", "", "");
        assertTextPresent("valid");

        // check that the from address is correctly validated.
        settingsForm.nextFormElements("http://localhost:8080", "", "invalid at email dot com", "", "", "");
        assertTextPresent("whitespace");

        settingsForm.nextFormElements("http://localhost:8080", "some.smtp.host.com", "Setup <from@localhost.com>", "username", "password", "prefix");
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
        licenseForm.nextFormElements(invalidLicenseKey);
        licenseForm.assertFormPresent();
        licenseForm.assertFormElements(invalidLicenseKey);
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

    private void checkSetPulseData()
    {
        SetPulseDataForm dataForm = new SetPulseDataForm(tester);

        dataForm.assertFormPresent();

        // ensure that we have a default value for the pulseData property.
        assertFormElementNotEmpty("data");
        // record the default value for later use.
        String defaultData = getFormValue("data");

        // check the validation - an empty pulse data.
        dataForm.nextFormElements("");
        // assert that we are still on the same page.
        dataForm.assertFormElements("");

        // check validation - an invalid pulse data value.

        // enter valid pulse data that does not exist.
        dataForm.nextFormElements("data");

        // it should prompt for confirmation to create the directory....
    }
}
