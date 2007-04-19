package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.LicenseEditForm;
import com.zutubi.pulse.license.LicenseType;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.test.LicenseHelper;
import com.zutubi.util.RandomUtils;

/**
 * <class-comment/>
 */
public class LicenseManagementAcceptanceTest extends BaseAcceptanceTestCase
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
        clickLink(Navigation.TAB_ADMINISTRATION);

        newLicenseHolder = RandomUtils.randomString(10);
        newLicenseKey = LicenseHelper.newLicenseKey(LicenseType.EVALUATION, newLicenseHolder, null);
        expiredLicenseKey = LicenseHelper.newExpiredLicenseKey(LicenseType.EVALUATION, "S. O. MeBody");
        invalidLicenseKey = LicenseHelper.newInvalidLicenseKey(LicenseType.EVALUATION, "S. O. MeBody");
    }

    protected void tearDown() throws Exception
    {
        newLicenseKey = null;
        expiredLicenseKey = null;
        invalidLicenseKey = null;
        newLicenseHolder = null;

        super.tearDown();
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

    //---( Now lets check the license information display )---

    public void testLicenseDetails() throws LicenseException
    {
        License newLicense = new License(LicenseType.CUSTOM, "local tester").setSupportedUsers(200);

        // set the new license
        clickLink("license.edit");

        LicenseEditForm form = new LicenseEditForm(tester);
        form.assertFormPresent();
        form.saveFormElements(LicenseHelper.newLicenseKey(newLicense));
        form.assertFormNotPresent();

        // validate the details of the license.
        assertTablePresent("license.info");
        assertTableRowsEqual("license.info", 1, new String[][]{
                {"license type", "Custom"},
                {"licensee", "local tester"},
                {"expiry date", "Never"},
                {"agent restriction", "unrestricted"},
                {"project restriction", "unrestricted"},
                {"user restriction", "3 of 200"}
        });
        // assuming that we only have a single user...
    }



}
