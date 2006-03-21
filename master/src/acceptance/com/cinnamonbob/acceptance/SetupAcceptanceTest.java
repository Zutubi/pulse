package com.cinnamonbob.acceptance;

import com.cinnamonbob.acceptance.forms.setup.SetBobHomeForm;
import com.cinnamonbob.acceptance.forms.setup.CreateAdminForm;
import com.cinnamonbob.acceptance.forms.setup.ServerSettingsForm;

/**
 * A setup test that covers the systems setup procedure.
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

        CreateAdminForm createAdminForm = new CreateAdminForm(tester);

        // create admin.
        createAdminForm.assertFormPresent();
        checkValidationForCreateAdminForm(createAdminForm);

        // now create the administrator.
        createAdminForm.nextFormElements("admin", "A. D. Ministrator", "admin", "admin");

        // check that any attempts to bypass the setup fail.
        //beginAt("/");

        // now fill in the server essentials form.
        ServerSettingsForm settingsForm = new ServerSettingsForm(tester);
        settingsForm.assertFormPresent();

        //TODO: validate the form elements...

        settingsForm.nextFormElements("localhost:8080", "from@some.host.com", "some.smtp.host.com", "", "");

        assertTextPresent(":: welcome ::");
        assertLinkPresentWithText("logout");
        assertTextPresent("A. D. Ministrator");
    }

    private void checkValidationForCreateAdminForm(CreateAdminForm form)
    {
        // check validation on the form.
        form.nextFormElements("", "A. D. Ministrator", "admin", "admin");
        form.assertFormElements("", "A. D. Ministrator", "", "");
        assertTextPresent("required");

        // - no name
        form.nextFormElements("admin", "", "admin", "admin");
        form.assertFormElements("admin", "", "", "");
        assertTextPresent("required");

        // - no password
        form.nextFormElements("admin", "A. D. Ministrator", "", "admin");
        form.assertFormElements("admin", "A. D. Ministrator", "", "");
        assertTextPresent("required");

        // - password and confirmation do not match
        form.nextFormElements("admin", "A. D. Ministrator", "admin", "something other then pass");
        form.assertFormElements("admin", "A. D. Ministrator", "", "");
    }
}
