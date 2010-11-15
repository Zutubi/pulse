package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.setup.*;
import com.zutubi.pulse.acceptance.pages.PulseToolbar;
import com.zutubi.pulse.master.license.LicenseHelper;
import com.zutubi.pulse.master.license.LicenseType;
import org.xml.sax.SAXException;

import java.io.IOException;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.ADMIN_CREDENTIALS;

/**
 * A setup test that covers the systems setup procedure.
 * <p/>
 * This setup test is a little awkward since we can only run it once. Once done, the
 * server is setup and will not take kindly to us trying to set it up again. So, rather than
 * having multiple test methods, there is one testSetupProcess method that is breaks up the setup
 * process and handles all of the validation testing as it goes.
 */
public class SetupAcceptanceTest extends AcceptanceTestBase
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

    public void testSetupProcess() throws InterruptedException, IOException, SAXException
    {
        // first we deal with the pulse home property configuration.
        getBrowser().open(urls.base() + "setup/setupData!input.action");
        getBrowser().waitForPageToLoad();

        // step one. setting the pulse home variable.
        checkSetPulseData();

        _checkPostPulseData();

        // lets also take this opportunity to verify that what was set during the setup wizard
        // was actually persisted.
    }

    protected void checkPostPulseData()
    {
        getBrowser().open(urls.base());
        _checkPostPulseData();
    }

    protected void _checkPostPulseData()
    {
        // step two. setting up the database
        checkSetupDatabase();

        // step three. setting the license details.
        checkLicenseDetails();

        // step four. creating the administration user.
        checkCreateAdmin();

        // step five. configuring the server essentials.
        checkServerSettings();

        assertTrue(getBrowser().isTextPresent("system setup"));
        getBrowser().waitForElement("welcome.heading", 60000);

        // one complete, we should see the home page, and it should contain the following:
        assertTrue(getBrowser().isTextPresent(":: welcome ::"));

        // wait for the toolbar to be rendered before continuing with checking.
        PulseToolbar toolbar = new PulseToolbar(getBrowser());
        toolbar.waitFor();

        assertTrue(getBrowser().isTextPresent("A. D. Ministrator"));
        assertTrue(getBrowser().isElementIdPresent("logout"));
    }

    private void checkSetPulseData()
    {
        assertPulseTabsNotVisible();

        SetPulseDataForm form = getBrowser().createForm(SetPulseDataForm.class);
        assertTrue(form.isFormPresent());
        assertTrue(form.isBrowseDataLinkPresent());
        assertTrue(form.isFieldNotEmpty("zfid.data"));

        form.nextFormElements("");
        form.waitFor();
        assertTrue(getBrowser().isTextPresent("pulse data directory requires a value"));

        form.nextFormElements("data");
    }

    private void assertPulseTabsNotVisible()
    {
        assertFalse(getBrowser().isElementIdPresent("tab.projects"));
        assertFalse(getBrowser().isElementIdPresent("tab.projects"));
        assertFalse(getBrowser().isElementIdPresent("tab.queues"));
        assertFalse(getBrowser().isElementIdPresent("tab.agents"));
        assertFalse(getBrowser().isElementIdPresent("tab.administration"));
    }

    private void checkSetupDatabase()
    {
        assertPulseTabsNotVisible();

        SetupDatabaseTypeForm form = getBrowser().createForm(SetupDatabaseTypeForm.class);
        form.waitFor();
        assertFalse("Detail fields should be disabled for embedded database", form.isEditable("host"));
        form.nextFormElements("EMBEDDED", null, null, null, null, null, null);
    }

    private void checkLicenseDetails()
    {
        assertPulseTabsNotVisible();

        PulseLicenseForm licenseForm = getBrowser().createForm(PulseLicenseForm.class);

        licenseForm.waitFor();

        // check that license is required.
        licenseForm.nextFormElements("");
        assertTrue(licenseForm.isFormPresent());
        assertTrue(licenseForm.checkFormValues(""));
        assertTrue(getBrowser().isTextPresent("license key requires a value"));

        // check that license validation works.
        licenseForm.nextFormElements(invalidLicenseKey);
        assertTrue(licenseForm.isFormPresent());
        assertTrue(licenseForm.checkFormValues(invalidLicenseKey));
        assertTrue(getBrowser().isTextPresent("invalid"));

        // check that an expired license is not accepted.
        licenseForm.nextFormElements(expiredLicenseKey);
        assertTrue(licenseForm.isFormPresent());
        assertTrue(licenseForm.checkFormValues(expiredLicenseKey));
        assertTrue(getBrowser().isTextPresent("expired"));

        // enter a valid license.
        licenseForm.nextFormElements(licenseKey);
        assertFalse(licenseForm.isFormPresent());
    }

    private void checkCreateAdmin()
    {
        assertPulseTabsNotVisible();

        CreateAdminForm createAdminForm = getBrowser().createForm(CreateAdminForm.class);

        // create admin.
        createAdminForm.waitFor();
        createAdminForm.nextFormElements(
                ADMIN_CREDENTIALS.getUserName(),
                "A. D. Ministrator",
                "admin@example.com",
                ADMIN_CREDENTIALS.getPassword(),
                ADMIN_CREDENTIALS.getPassword()
        );
    }

    private void checkServerSettings()
    {
        assertPulseTabsNotVisible();

        ServerSettingsForm settingsForm = getBrowser().createForm(ServerSettingsForm.class);
        settingsForm.waitFor();
        settingsForm.finishFormElements("http://localhost:8080", "some.smtp.host.com", "true", "Setup <from@localhost.com>", "username", "password", "prefix", "true", "123");
    }

}
