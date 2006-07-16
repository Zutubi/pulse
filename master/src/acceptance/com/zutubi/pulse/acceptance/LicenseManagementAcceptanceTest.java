package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.LicenseEditForm;
import com.zutubi.pulse.license.LicenseType;
import com.zutubi.pulse.test.LicenseHelper;
import com.zutubi.pulse.util.RandomUtils;

/**
 * <class-comment/>
 */
public class LicenseManagementAcceptanceTest extends BaseAcceptanceTest
{
    // we use two dummy licenses for testing, alternating between them to
    // ensure that the details are updated correctly.

    private String newLicenseKey;
    private String newLicenseHolder;
    private String expiredLicenseKey;
    private String invalidLicenseKey;

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
        loginAsAdmin();

        // start on the system information page.
        clickLinkWithText("administration");

        newLicenseHolder = RandomUtils.randomString(10);
        newLicenseKey = LicenseHelper.newLicenseKey(LicenseType.EVALUATION, newLicenseHolder, null);
        expiredLicenseKey = LicenseHelper.newExpiredLicenseKey(LicenseType.EVALUATION, "S. O. MeBody");
        invalidLicenseKey = LicenseHelper.newInvalidLicenseKey(LicenseType.EVALUATION, "S. O. MeBody");
    }

    protected void tearDown() throws Exception
    {
        newLicenseKey = null;

        super.tearDown();
    }

    public void testLicenseDetails()
    {
        assertTablePresent("license.info");
        // this is a little difficult since we dont know exactly what the
        // details of the installed license are...
    }

    public void testUpdateLicense()
    {
        clickLink("license.edit");

        LicenseEditForm form = new LicenseEditForm(tester);
        form.assertFormPresent();

        // the form should be blank initially.
        form.assertFormElements("");

        // a valid license is accepted
        form.saveFormElements(newLicenseKey);
        form.assertFormNotPresent();

        assertTablePresent("license.info");
        assertTextPresent(newLicenseHolder);
    }

    public void testUpdateLicenseValidation()
    {
        clickLink("license.edit");

        LicenseEditForm form = new LicenseEditForm(tester);
        form.assertFormPresent();

        // the form should be blank initially.
        form.assertFormElements("");

        // a) a blank form.
        form.saveFormElements("");
        form.assertFormPresent();
        assertTextPresent("required");

        // b) an invalid license.
        form.saveFormElements(invalidLicenseKey);
        form.assertFormPresent();
        form.assertFormElements(invalidLicenseKey);
        assertTextPresent("invalid");
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

    public void testExpiredLicense()
    {
        clickLink("license.edit");

        LicenseEditForm form = new LicenseEditForm(tester);
        form.assertFormPresent();

        // set a new license string.
        form.saveFormElements(expiredLicenseKey);
        form.assertFormPresent();

        assertTextPresent("expired");
    }

    public void testEditLicenseLinkOnlyAvailableToAdmin()
    {

    }
}
