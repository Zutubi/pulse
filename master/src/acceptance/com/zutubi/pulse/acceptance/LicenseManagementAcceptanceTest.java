package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.LicenseEditForm;

/**
 * <class-comment/>
 */
public class LicenseManagementAcceptanceTest extends BaseAcceptanceTest
{
    // we use two dummy licenses for testing, alternating between them to
    // ensure that the details are updated correctly.

    /**
     * A dummy license with no expiry date.
     */
    public static final String DUMMY_LICENSE_A =
            "AAAAFmR1bW15IGxpY2Vuc2UgYQpOZXZlcgpNhf+cJtOjEAXmhyrqT5gQdkBK\n" +
                    "utkiIk/h///Tay7Fgy6eHD1CCZsHYUT17GHpZcOwETp3969GrI+68pdkQPXD\n" +
                    "G9RXuCeb/BHE2wVTvucSvum5mfw7p7kRX1bR/2fIf2BZ4kZwe+R5mWqifvY1\n" +
                    "Xw5I78lQzVmkr224ghWuHneI+w==";

    /**
     * A dummy license with no expiry date.
     */
    public static final String DUMMY_LICENSE_B =
            "AAAAFmR1bW15IGxpY2Vuc2UgYgpOZXZlcgoXrAp3Lo7ohHeikwtatfmcq0Kq\n" +
                    "OvK4BLTwtg4odH0etCP6AZu/2nKzwfvyFAh/EwdSSXaLWltUCBKdxG+Y3J4T\n" +
                    "HTCKjNpnG4BS3fh/IfclHdpjdcedJwRw4DP5csg6eNSkUCz2jyQsWbeH6Cx6\n" +
                    "TeTrCV7hIcewejr+VG7jzC3DfQ==";

    public static final String INVALID_DUMMY_LICENSE =
            "AAAAFWR1bW15IGxpY2Vuc2UuCkldmVyCj7MZ9Rsw+pryhZkbpgHuL4Akqun\n" +
            "di/c+99kObsCxELNgkpDK1lZ5VNonwhjSN7M3o+B7AvKBabulzKnQrRHlvF\n" +
            "Lr6ox7okk99Lt/+dsMCnRrArEIFgIOoBGLxwHKi17DLW3/OKtWjGEegLWG+\n" +
            "j/FslH3cL2L9kHJyj55L0+Hq";

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

        if (textInResponse("dummy license a"))
        {
            newLicenseKey = DUMMY_LICENSE_B;
            newLicenseHolder = "dummy license b";
        }
        else
        {
            newLicenseKey = DUMMY_LICENSE_A;
            newLicenseHolder = "dummy license a";
        }
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
        form.saveFormElements(INVALID_DUMMY_LICENSE);
        form.assertFormPresent();
        form.assertFormElements(INVALID_DUMMY_LICENSE);
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
