package com.cinnamonbob.acceptance;

import com.cinnamonbob.acceptance.forms.setup.CreateAdminForm;
import com.cinnamonbob.acceptance.forms.setup.ServerSettingsForm;
import com.cinnamonbob.acceptance.forms.setup.SetBobHomeForm;

/**
 * A setup test that covers the systems setup procedure.
 *
 * This setup test is a little awkward since we can only run it once. Once done, the
 * server is setup and will not take kindly to us trying to set it up again. So, rather than
 * having multiple test methods, there is one testSetupProcess method that is breaks up the setup
 * process and handles all of the validation testing as it goes.
 */
public class SetupAcceptanceTest extends BaseAcceptanceTest
{
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testSetupProcess()
    {
        // first we deal with the bob home property configuration.
        beginAt("/");

        // step one. setting the bob home variable.
        checkSetBobHome();

        // step two. creating the administration user.
        checkCreateAdmin();

        // check that any attempts to bypass the setup fail.
        //beginAt("/");

        // step three. configuring the server essentials.
        checkServerSettings();

        // one complete, we should see the home page, and it should contain the following:
        assertTextPresent(":: welcome ::");
        assertTextPresent("A. D. Ministrator");
        assertLinkPresentWithText("logout");

        // lets also take this opportunity to verify that what was set during the setup wizard
        // was actually persisted.
    }

    private void checkServerSettings()
    {
        ServerSettingsForm settingsForm = new ServerSettingsForm(tester);
        settingsForm.assertFormPresent();

        // local host is required, and while we are at it, verify that all of the other form fields
        // are correctly returned to the user when there is a validation failure.
        settingsForm.nextFormElements("", "some.smtp.host.com", "from@some.host.com", "username", "password", "prefix");
        settingsForm.assertFormElements("", "some.smtp.host.com", "from@some.host.com", "username", "password", "prefix");
        assertTextPresent("required");

        // if smtp host is set, then smtp from is also required.
        settingsForm.nextFormElements("localhost:8080", "some.smtp.host.com", "", "", "", "");
        settingsForm.assertFormElements("localhost:8080", "some.smtp.host.com", "", "", "", "");
        assertTextPresent("required");

        // smtp from must be valid ??.
//        settingsForm.nextFormElements("localhost:8080", "some.smtp.host.com", "invalid from address", "", "");
//        settingsForm.assertFormElements("localhost:8080", "some.smtp.host.com", "invalid from address", "", "");
//        assertTextPresent("required");

        settingsForm.nextFormElements("localhost:8080", "some.smtp.host.com", "from@some.host.com", "username", "password", "prefix");
    }

    private void checkCreateAdmin()
    {
        CreateAdminForm createAdminForm = new CreateAdminForm(tester);

        // create admin.
        createAdminForm.assertFormPresent();

        // check validation on the form.
        createAdminForm.nextFormElements("", "A. D. Ministrator", "admin", "admin");
        createAdminForm.assertFormElements("", "A. D. Ministrator", "", "");
        assertTextPresent("required");

        // - no name
        createAdminForm.nextFormElements("admin", "", "admin", "admin");
        createAdminForm.assertFormElements("admin", "", "", "");
        assertTextPresent("required");

        // - no password
        createAdminForm.nextFormElements("admin", "A. D. Ministrator", "", "admin");
        createAdminForm.assertFormElements("admin", "A. D. Ministrator", "", "");
        assertTextPresent("required");

        // - password and confirmation do not match
        createAdminForm.nextFormElements("admin", "A. D. Ministrator", "admin", "something other then pass");
        createAdminForm.assertFormElements("admin", "A. D. Ministrator", "", "");

        // now create the administrator.
        createAdminForm.nextFormElements("admin", "A. D. Ministrator", "admin", "admin");
    }

    private void checkSetBobHome()
    {
        SetBobHomeForm bobHomeForm = new SetBobHomeForm(tester);

        bobHomeForm.assertFormPresent();

        // ensure that we have a default value for the bobHome property.
        assertFormElementNotEmpty("bobHome");
        // record the default value for later use.
        String defaultBobHome = getFormValue("bobHome");

        // check the validation - an empty bob home.
        bobHomeForm.nextFormElements("");
        // assert that we are still on the same page.
        bobHomeForm.assertFormElements("");

        // check validation - an invalid bob home value.

        // enter valid bob home that does not exist.
        bobHomeForm.nextFormElements(defaultBobHome);

        // it should prompt for confirmation to create the directory....
    }
}
