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
public class SetupAcceptanceTest extends SeleniumTestBase
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
        selenium.open("http://localhost:" + port + "/setup/setupData!input.action");

        // step one. setting the pulse home variable.
        checkSetPulseData();

        // step two. setting the license details.
        checkLicenseDetails();

        // step three. creating the administration user.
        checkCreateAdmin();

        // step four. configuring the server essentials.
        checkServerSettings();

        assertTextPresent("system setup");
        selenium.waitForCondition("selenium.browserbot.getCurrentWindow().document.getElementById('welcome.heading') != null", "60000");

        // one complete, we should see the home page, and it should contain the following:
        assertTextPresent(":: welcome ::");
        assertTextPresent("A. D. Ministrator");
        assertLinkPresent(Navigation.LINK_LOGOUT);

        // lets also take this opportunity to verify that what was set during the setup wizard
        // was actually persisted.
    }

    private void checkSetPulseData()
    {
        SetPulseDataForm form = new SetPulseDataForm(selenium);
        form.assertFormPresent();
        assertFormFieldNotEmpty("zfid.data");

        form.nextFormElements("");
        assertTextPresent("data requires a value");
        form.assertFormPresent();

        form.nextFormElements("data");
    }

    private void checkLicenseDetails()
    {
        PulseLicenseForm licenseForm = new PulseLicenseForm(selenium);

        licenseForm.assertFormPresent();

        // check that license is required.
        licenseForm.nextFormElements("");
        licenseForm.assertFormPresent();
        licenseForm.assertFormElements("");
        assertTextPresent("license requires a value");

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
        CreateAdminForm createAdminForm = new CreateAdminForm(selenium);

        // create admin.
        createAdminForm.assertFormPresent();
        createAdminForm.nextFormElements("admin", "A. D. Ministrator", "admin", "admin");
    }

    private void checkServerSettings()
    {
        ServerSettingsForm settingsForm = new ServerSettingsForm(selenium);
        settingsForm.assertFormPresent();
        settingsForm.finishFormElements("http://localhost:8080", "some.smtp.host.com", "true", "Setup <from@localhost.com>", "username", "password", "prefix", "true", "123");
    }

}
