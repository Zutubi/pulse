package com.zutubi.pulse.acceptance;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.ADMIN_CREDENTIALS;
import com.zutubi.pulse.acceptance.forms.setup.*;
import com.zutubi.pulse.acceptance.pages.PulseToolbar;
import com.zutubi.pulse.master.license.LicenseHelper;
import com.zutubi.pulse.master.license.LicenseType;
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

    public void testSetupProcess() throws InterruptedException, IOException, SAXException
    {
        // first we deal with the pulse home property configuration.
        browser.open(urls.base() + "setup/setupData!input.action");
        browser.waitForPageToLoad();

        // step one. setting the pulse home variable.
        checkSetPulseData();

        _checkPostPulseData();

        // lets also take this opportunity to verify that what was set during the setup wizard
        // was actually persisted.
    }

    protected void checkPostPulseData()
    {
        browser.open(urls.base());
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

        assertTrue(browser.isTextPresent("system setup"));
        browser.waitForElement("welcome.heading", 60000);

        // one complete, we should see the home page, and it should contain the following:
        assertTrue(browser.isTextPresent(":: welcome ::"));

        // wait for the toolbar to be rendered before continuing with checking.
        PulseToolbar toolbar = new PulseToolbar(browser);
        toolbar.waitFor();

        assertTrue(browser.isTextPresent("A. D. Ministrator"));
        assertTrue(browser.isElementIdPresent("logout"));
    }

    private void checkSetPulseData()
    {
        assertPulseTabsNotVisible();

        SetPulseDataForm form = browser.createForm(SetPulseDataForm.class);
        assertTrue(form.isFormPresent());
        assertTrue(form.isBrowseDataLinkPresent());
        assertTrue(form.isFieldNotEmpty("zfid.data"));

        form.nextFormElements("");
        form.waitFor();
        assertTrue(browser.isTextPresent("pulse data directory requires a value"));

        form.nextFormElements("data");
    }

    private void assertPulseTabsNotVisible()
    {
        assertFalse(browser.isElementIdPresent("tab.projects"));
        assertFalse(browser.isElementIdPresent("tab.projects"));
        assertFalse(browser.isElementIdPresent("tab.queues"));
        assertFalse(browser.isElementIdPresent("tab.agents"));
        assertFalse(browser.isElementIdPresent("tab.administration"));
    }

    private void checkSetupDatabase()
    {
        assertPulseTabsNotVisible();

        SetupDatabaseTypeForm form = browser.createForm(SetupDatabaseTypeForm.class);
        form.waitFor();
        assertFalse("Detail fields should be disabled for embedded database", form.isEditable("host"));
        form.nextFormElements("EMBEDDED", null, null, null, null, null, null);
    }

    private void checkLicenseDetails()
    {
        assertPulseTabsNotVisible();

        PulseLicenseForm licenseForm = browser.createForm(PulseLicenseForm.class);

        licenseForm.waitFor();

        // check that license is required.
        licenseForm.nextFormElements("");
        assertTrue(licenseForm.isFormPresent());
        assertFormElements(licenseForm, "");
        assertTrue(browser.isTextPresent("license key requires a value"));

        // check that license validation works.
        licenseForm.nextFormElements(invalidLicenseKey);
        assertTrue(licenseForm.isFormPresent());
        assertFormElements(licenseForm, invalidLicenseKey);
        assertTrue(browser.isTextPresent("invalid"));

        // check that an expired license is not accepted.
        licenseForm.nextFormElements(expiredLicenseKey);
        assertTrue(licenseForm.isFormPresent());
        assertFormElements(licenseForm, expiredLicenseKey);
        assertTrue(browser.isTextPresent("expired"));

        // enter a valid license.
        licenseForm.nextFormElements(licenseKey);
        assertFalse(licenseForm.isFormPresent());
    }

    private void checkCreateAdmin()
    {
        assertPulseTabsNotVisible();

        CreateAdminForm createAdminForm = browser.createForm(CreateAdminForm.class);

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

        ServerSettingsForm settingsForm = browser.createForm(ServerSettingsForm.class);
        settingsForm.waitFor();
        settingsForm.finishFormElements("http://localhost:8080", "some.smtp.host.com", "true", "Setup <from@localhost.com>", "username", "password", "prefix", "true", "123");
    }

}
