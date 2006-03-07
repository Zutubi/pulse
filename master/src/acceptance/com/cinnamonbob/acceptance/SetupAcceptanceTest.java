package com.cinnamonbob.acceptance;

import com.cinnamonbob.core.util.FileSystemUtils;

import java.io.File;

/**
 * A setup test that covers the systems setup procedure.
 */
public class SetupAcceptanceTest extends BaseAcceptanceTest
{
    private static final String FE_LOGIN = "admin.login";
    private static final String FE_NAME = "admin.name";
    private static final String FE_PASSWORD = "admin.password";
    private static final String FE_CONFIRM = "confirm";

    private File tmpDir;
    private static final String FO_ADMIN_CREATE = "admin.create";
    private static final String FE_HOSTNAME = "hostname";
    private static final String FE_FROM = "fromAddress";
    private static final String FE_SMTP_HOST = "smtpHost";
    private static final String FE_SMTP_USER = "smtp.username";
    private static final String FE_SMTP_PASS = "smtp.password";
    private static final String FO_SERVER_SETTINGS = "server.settings";

    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = FileSystemUtils.createTempDirectory(SetupAcceptanceTest.class.getName() + "-", "");
    }

    protected void tearDown() throws Exception
    {
        // an attempt to clean up the installation will fail since the running application
        // will have open database connections, preventing the database from being deleted.
        //removeDirectory(tmpDir);

        super.tearDown();
    }

    public void testSetupProcess()
    {
        // first we deal with the bob home property configuration.
        beginAt("/");

        assertFormPresent("setup.bobHome");
        setWorkingForm("setup.bobHome");

        // ensure that we have a default value for the bobHome property.
        assertFormElementNotEmpty("bobHome");

        // record the default value for later use.
        String defaultBobHome = getFormValue("bobHome");

        // check the validation - an empty bob home.
        setFormElement("bobHome", "");
        submit("next");

        // assert that we are still on the same page.
        assertFormPresent("setup.bobHome");
        assertFormElementEmpty("bobHome");

        // check validation - an invalid bob home value.

        // enter valid bob home that does not exist.
//        File bobHome = new File(tmpDir, "home");
        setFormElement("bobHome", defaultBobHome);
        submit("next");

        // it should prompt for confirmation to create the directory.

        // create admin.
        assertFormPresent(FO_ADMIN_CREATE);
        checkValidationForCreateAdminForm();

        // now create the administrator.
        submitCreateAdminForm("admin", "A. D. Ministrator", "admin", "admin");

        // check that any attempts to bypass the setup fail.
        //beginAt("/");

        // now fill in the server essentials form.
        assertFormPresent(FO_SERVER_SETTINGS);

        //TODO: validate the form elements...

        //assertFormElementNotEmpty(FE_HOSTNAME);

        submitServerSettingsForm("localhost:8080", "from@some.host.com", "some.smtp.host.com", "", "");

        assertTextPresent(":: welcome ::");
        assertLinkPresentWithText("logout");
        assertTextPresent("A. D. Ministrator");
    }

    private void checkValidationForCreateAdminForm()
    {
        // check validation on the form.
        submitCreateAdminForm("", "A. D. Ministrator", "admin", "admin");
        assertTextPresent("required");
        assertFormPresent(FO_ADMIN_CREATE);
        assertFormElementEmpty(FE_LOGIN);
        assertFormElementEquals(FE_NAME, "A. D. Ministrator");
        assertFormElementEmpty(FE_PASSWORD);
        assertFormElementEmpty(FE_CONFIRM);

        // - no name
        submitCreateAdminForm("admin", "", "admin", "admin");
        assertTextPresent("required");
        assertFormPresent(FO_ADMIN_CREATE);
        assertFormElementEquals(FE_LOGIN, "admin");
        assertFormElementEmpty(FE_NAME);
        assertFormElementEmpty(FE_PASSWORD);
        assertFormElementEmpty(FE_CONFIRM);

        // - no password
        submitCreateAdminForm("admin", "A. D. Ministrator", "", "admin");
        assertTextPresent("required");
        assertFormPresent(FO_ADMIN_CREATE);
        assertFormElementEquals(FE_LOGIN, "admin");
        assertFormElementEquals(FE_NAME, "A. D. Ministrator");
        assertFormElementEmpty(FE_PASSWORD);
        assertFormElementEmpty(FE_CONFIRM);

        // - password and confirmation do not match
        submitCreateAdminForm("admin", "A. D. Ministrator", "admin", "something other then pass");
        assertFormPresent(FO_ADMIN_CREATE);
//        assertTextPresent("do not match");
        assertFormElementEquals(FE_LOGIN, "admin");
        assertFormElementEquals(FE_NAME, "A. D. Ministrator");
    }

    private void submitCreateAdminForm(String login, String name, String password, String confirm)
    {
        assertFormPresent(FO_ADMIN_CREATE);
        setWorkingForm(FO_ADMIN_CREATE);
        setFormElement(FE_LOGIN, login);
        setFormElement(FE_NAME, name);
        setFormElement(FE_PASSWORD, password);
        setFormElement(FE_CONFIRM, confirm);
        submit("next");
    }

    private void submitServerSettingsForm(String hostname, String smtpFrom, String smtpHost, String smtpUser, String smtpPass)
    {
        assertFormPresent(FO_SERVER_SETTINGS);
        setWorkingForm(FO_SERVER_SETTINGS);
        setFormElement(FE_HOSTNAME, hostname);
        setFormElement(FE_FROM, smtpFrom);
        setFormElement(FE_SMTP_HOST, smtpHost);
        setFormElement(FE_SMTP_USER, smtpUser);
        setFormElement(FE_SMTP_PASS, smtpPass);
        submit("next");
    }
}
