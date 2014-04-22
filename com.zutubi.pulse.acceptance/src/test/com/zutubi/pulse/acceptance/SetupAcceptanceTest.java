package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.setup.*;
import com.zutubi.pulse.acceptance.pages.PulseToolbar;
import com.zutubi.pulse.master.license.LicenseHelper;
import com.zutubi.pulse.master.license.LicenseType;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Hashtable;

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

    public void testSetupProcess() throws Exception
    {
        getBrowser().open(urls.base() + "setup/setupData!input.action");

        checkSetPulseData();
        checkPostPulseData();

        setAdminPreferences();
    }

    private void checkSetPulseData()
    {
        SetPulseDataForm form = getBrowser().createForm(SetPulseDataForm.class);
        form.waitFor();
        assertTrue(form.isBrowseDataLinkPresent());
        assertTrue(form.isFieldNotEmpty("zfid.data"));

        form.nextFormElements("");
        form.waitFor();
        getBrowser().waitForTextPresent("pulse data directory requires a value");

        form.nextFormElements("data");
    }

    protected void checkPostPulseData()
    {
        checkSetupDatabase();
        checkLicenseDetails();
        checkCreateAdmin();
        checkServerSettings();

        getBrowser().waitForTextPresent(":: welcome ::");

        PulseToolbar toolbar = new PulseToolbar(getBrowser());
        toolbar.waitFor();

        getBrowser().waitForTextPresent("A. D. Ministrator");
        assertTrue(getBrowser().isElementIdPresent("logout"));
    }

    private void checkSetupDatabase()
    {
        SetupDatabaseTypeForm form = getBrowser().createForm(SetupDatabaseTypeForm.class);
        form.waitFor();
        assertFalse("Detail fields should be disabled for embedded database", form.isEditable("host"));
        form.nextFormElements("EMBEDDED", null, null, null, null, null, null);
    }

    private void checkLicenseDetails()
    {
        PulseLicenseForm licenseForm = getBrowser().createForm(PulseLicenseForm.class);
        licenseForm.waitFor();

        // check that license is required.
        licenseForm.nextFormElements("");
        licenseForm.waitFor();
        assertTrue(licenseForm.checkFormValues(""));
        getBrowser().waitForTextPresent("license key requires a value");

        // check that license validation works.
        licenseForm.nextFormElements(invalidLicenseKey);
        licenseForm.waitFor();
        assertTrue(licenseForm.checkFormValues(invalidLicenseKey));
        getBrowser().waitForTextPresent("invalid");

        // check that an expired license is not accepted.
        licenseForm.nextFormElements(expiredLicenseKey);
        licenseForm.waitFor();
        assertTrue(licenseForm.checkFormValues(expiredLicenseKey));
        getBrowser().waitForTextPresent("expired");

        // enter a valid license.
        licenseForm.nextFormElements(licenseKey);
    }

    private void checkCreateAdmin()
    {
        CreateAdminForm createAdminForm = getBrowser().createForm(CreateAdminForm.class);
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
        ServerSettingsForm settingsForm = getBrowser().createForm(ServerSettingsForm.class);
        settingsForm.waitFor();
        settingsForm.finishFormElements("http://localhost:8080", "some.smtp.host.com", "true", "Setup <from@localhost.com>", "username", "password", "prefix", "true", "123");
    }

    private void setAdminPreferences() throws Exception
    {
        rpcClient.loginAsAdmin();
        try
        {
            String preferencesPath = PathUtils.getPath(MasterConfigurationRegistry.USERS_SCOPE, ADMIN_CREDENTIALS.getUserName(), "preferences");
            Hashtable<String, Object> preferences = rpcClient.RemoteApi.getConfig(preferencesPath);
            preferences.put("refreshInterval", 600);
            rpcClient.RemoteApi.saveConfig(preferencesPath, preferences, false);
        }
        finally
        {
            rpcClient.logout();
        }
    }
}
