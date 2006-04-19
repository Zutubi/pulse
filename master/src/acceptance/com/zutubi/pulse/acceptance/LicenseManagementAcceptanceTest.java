package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.LicenseEditForm;
import com.zutubi.pulse.util.RandomUtils;
import com.zutubi.pulse.test.LicenseHelper;

/**
 * <class-comment/>
 */
public class LicenseManagementAcceptanceTest extends BaseAcceptanceTest
{
    // we use two dummy licenses for testing, alternating between them to
    // ensure that the details are updated correctly.

    public static final String INVALID_LICENSE =
            "AAAFWR1bW15IGxpY2Vuc2UuCkldmVyCj7MZ9Rsw+pryhZkbpgHuL4Akqun\n" +
            "i/c+99kObsCxELNgkpDK1lZ5VNonwhjSN7M3o+B7AvKBabulzKnQrRHlvF\n" +
            "r6ox7okk99Lt/+dsMCnRrArEIFgIOoBGLxwHKi17DLW3/OKtWjGEegLWG+\n" +
            "/FslH3cL2L9kHJyj55L0+Hq";

    private String newLicenseKey;
    private String newLicenseHolder;

    public LicenseManagementAcceptanceTest()
    {
    }

    public LicenseManagementAcceptanceTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        login("admin", "admin");

        // start on the system information page.
        gotoPage("/viewSystemInfo.action");

        newLicenseHolder = RandomUtils.randomString(10);
        newLicenseKey = LicenseHelper.newLicenseKey("dummy", newLicenseHolder, null);
    }

    protected void tearDown() throws Exception
    {
        newLicenseKey = null;

        super.tearDown();
    }

    public void testLicenseDetails()
    {
        assertTablePresent("license.info");

        // need some more validation here... need to ensure that it
        // handles that the installed license could be one of multiple
        // licenses used in testing.

    }

    public void testUpdateLicense()
    {
        clickLink("license.edit");

        LicenseEditForm form = new LicenseEditForm(tester);
        form.assertFormPresent();

        // the form should be blank initially.
        form.assertFormElements("");

        // ensure that correct validation is carried out.
        // a) a blank form.
        form.saveFormElements("");
        form.assertFormPresent();
        assertTextPresent("required");

        // b) an invalid license.
        form.saveFormElements(INVALID_LICENSE);
        form.assertFormPresent();
        form.assertFormElements(INVALID_LICENSE);
        assertTextPresent("invalid");

        // c) a valid license is accepted
        form.saveFormElements(newLicenseKey);
        form.assertFormNotPresent();

        assertTablePresent("license.info");
        assertTextPresent(newLicenseHolder);
    }

    public void testCancelLicenseEdit()
    {
        clickLink("license.edit");

        LicenseEditForm form = new LicenseEditForm(tester);
        form.assertFormPresent();

        // set a new license string.
        form.cancelFormElements(newLicenseKey);

        // we should be back on the server info page without the license
        // having been updated.
        form.assertFormNotPresent();

        assertTablePresent("license.info");
        assertTextNotPresent(newLicenseHolder);
    }
}
