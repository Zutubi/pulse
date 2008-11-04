package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.setup.*;
import com.zutubi.pulse.master.license.LicenseHelper;
import com.zutubi.pulse.master.license.LicenseType;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
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
@Test(groups = "init.setup")
public class SetupAcceptanceTest extends SeleniumTestBase
{
    public String licenseKey;
    public String expiredLicenseKey;
    public String invalidLicenseKey;

    @BeforeMethod
    protected void setUp() throws Exception
    {
        super.setUp();

        licenseKey = LicenseHelper.newLicenseKey(LicenseType.EVALUATION, "S. O. MeBody");
        expiredLicenseKey = LicenseHelper.newExpiredLicenseKey(LicenseType.EVALUATION, "S. O. MeBody");
        invalidLicenseKey = LicenseHelper.newInvalidLicenseKey(LicenseType.EVALUATION, "S. O. MeBody");
    }

    @AfterMethod
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testSetupProcess() throws InterruptedException, IOException, SAXException
    {
        // first we deal with the pulse home property configuration.
        goTo("setup/setupData!input.action");

        // step one. setting the pulse home variable.
        checkSetPulseData();

        _checkPostPulseData();

        // lets also take this opportunity to verify that what was set during the setup wizard
        // was actually persisted.
    }


    protected void checkPostPulseData()
    {
        goTo("/");
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

        assertTextPresent("system setup");
        waitForElement("welcome.heading", 60000);

        // one complete, we should see the home page, and it should contain the following:
        assertTextPresent(":: welcome ::");
        assertTextPresent("A. D. Ministrator");
        assertLinkPresent("logout");
    }

    private void checkSetPulseData()
    {
        assertPulseTabsNotVisible();

        SetPulseDataForm form = new SetPulseDataForm(selenium);
        form.assertFormPresent();
        assertFormFieldNotEmpty("zfid.data");

        String defaultDataDir = "data";

        form.nextFormElements("");
        assertTextPresent("pulse data directory requires a value");
        form.assertFormPresent();

        form.nextFormElements(defaultDataDir);
    }

    private void assertPulseTabsNotVisible()
    {
        assertElementNotPresent("tab.projects");
        assertElementNotPresent("tab.queues");
        assertElementNotPresent("tab.agents");
        assertElementNotPresent("tab.administration");
    }

    private void checkSetupDatabase()
    {
        assertPulseTabsNotVisible();

        SetupDatabaseTypeForm form = new SetupDatabaseTypeForm(selenium);
        form.assertFormPresent();
        form.nextFormElements("EMBEDDED", null, null, null, null, null, null);
    }

    private void checkLicenseDetails()
    {
        assertPulseTabsNotVisible();

        PulseLicenseForm licenseForm = new PulseLicenseForm(selenium);

        licenseForm.assertFormPresent();

        // check that license is required.
        licenseForm.nextFormElements("");
        licenseForm.assertFormPresent();
        licenseForm.assertFormElements("");
        assertTextPresent("license key requires a value");

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
        assertPulseTabsNotVisible();

        CreateAdminForm createAdminForm = new CreateAdminForm(selenium);

        // create admin.
        createAdminForm.assertFormPresent();
        createAdminForm.nextFormElements("admin", "A. D. Ministrator", "admin", "admin");
    }

    private void checkServerSettings()
    {
        assertPulseTabsNotVisible();

        ServerSettingsForm settingsForm = new ServerSettingsForm(selenium);
        settingsForm.assertFormPresent();
        settingsForm.finishFormElements("http://localhost:8080", "some.smtp.host.com", "true", "Setup <from@localhost.com>", "username", "password", "prefix", "true", "123");
    }

}
