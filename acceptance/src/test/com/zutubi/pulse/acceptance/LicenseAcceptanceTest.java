package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.LicenseForm;
import com.zutubi.pulse.acceptance.pages.admin.CompositePage;
import com.zutubi.pulse.license.LicenseType;
import com.zutubi.pulse.license.config.LicenseConfiguration;
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
        loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        try
        {
            Hashtable<String, Object> license = xmlRpcHelper.createDefaultConfig(LicenseConfiguration.class);
            license.put("key", LicenseHelper.newLicenseKey(LicenseType.EVALUATION, "S. O. MeBody"));
            xmlRpcHelper.saveConfig(LICENSE_PATH, license, false);
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
        LicenseForm form = new LicenseForm(selenium);
        form.waitFor();
        form.applyFormElements(LicenseHelper.newLicenseKey(LicenseType.EVALUATION, random, new Date(System.currentTimeMillis() + Constants.DAY)));

        waitForElement(licensePage.getStateFieldId("name"));
        assertEquals(random, licensePage.getStateField("name"));
    }

    public void testExpiresStateField()
    {
        // Field should be 'expiry' for eval and 'supportExpiry' for all
        // others.
        CompositePage licensePage = goToLicensePage();
        LicenseForm form = new LicenseForm(selenium);
        form.waitFor();
        form.applyFormElements(LicenseHelper.newLicenseKey(LicenseType.EVALUATION, random, new Date(System.currentTimeMillis() + Constants.DAY)));

        waitForElement(licensePage.getStateFieldId("expiry"));
        assertFalse(licensePage.isStateFieldPresent("supportExpiry"));

        form.waitFor();
        form.applyFormElements(LicenseHelper.newLicenseKey(LicenseType.ENTERPRISE, random, new Date(System.currentTimeMillis() + Constants.DAY)));

        waitForElement(licensePage.getStateFieldId("supportExpiry"));
        assertFalse(licensePage.isStateFieldPresent("expiry"));
    }

    private CompositePage goToLicensePage()
    {
        CompositePage licensePage = new CompositePage(selenium, urls, LICENSE_PATH);
        licensePage.goTo();
        return licensePage;
    }
}
