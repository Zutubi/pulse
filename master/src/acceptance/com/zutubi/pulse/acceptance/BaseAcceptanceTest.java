package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.CvsForm;
import com.zutubi.pulse.core.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 * <class-comment/>
 */
public abstract class BaseAcceptanceTest extends ExtendedWebTestCase
{
    //---( administrations create user form )---
    private static final String FO_USER_CREATE = "newUser.create";
    protected static final String USER_CREATE_LOGIN = "newUser.login";
    protected static final String USER_CREATE_NAME = "newUser.name";
    protected static final String USER_CREATE_PASSWORD = "newUser.password";
    protected static final String USER_CREATE_CONFIRM = "confirm";
    protected static final String USER_CREATE_ADMIN = "admin";

    //---( add project wizard forms )---
    protected static final String FO_PROJECT_BASICS = "project.basics";
    protected static final String PROJECT_BASICS_NAME = "name";
    protected static final String PROJECT_BASICS_DESCRIPTION = "description";
    protected static final String PROJECT_BASICS_URL = "url";
    protected static final String PROJECT_BASICS_SCM = "scm";
    protected static final String PROJECT_BASICS_TYPE = "type";
    protected static final String FO_CUSTOM_SETUP = "custom.setup";
    protected static final String CUSTOM_SETUP_FILE = "details.pulseFileName";

    protected String port;

    public BaseAcceptanceTest()
    {
    }

    public BaseAcceptanceTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();


        port = System.getProperty("pulse.port");
        if(port == null)
        {
            port = "8080";
        }

        getTestContext().setBaseUrl("http://localhost:" + port + "/");
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }


    protected void removeDirectory(File dir) throws IOException
    {
        if (!FileSystemUtils.removeDirectory(dir))
        {
            throw new IOException("Failed to remove " + dir);
        }
    }

    protected void login(String user, String password)
    {
        beginAt("/login.action");
        setWorkingForm("j_acegi_security_check");
        setFormElement("j_username", user);
        setFormElement("j_password", password);
        submit("login");
    }

    protected void submitCreateUserForm(String login, String name, String password, String confirm, boolean admin)
    {
        setWorkingForm(FO_USER_CREATE);
        setFormElement(USER_CREATE_LOGIN, login);
        setFormElement(USER_CREATE_NAME, name);
        setFormElement(USER_CREATE_PASSWORD, password);
        setFormElement(USER_CREATE_CONFIRM, confirm);
        if (admin)
        {
            checkCheckbox(USER_CREATE_ADMIN, "true");
        }
        else
        {
            uncheckCheckbox(USER_CREATE_ADMIN);
        }
        submit("save");
    }

    protected void navigateToUserAdministration()
    {
        gotoPage("/");
        clickLinkWithText("administration");
        clickLinkWithText("users");
    }

    protected void submitCustomSetupForm(String file)
    {
        assertFormPresent(FO_CUSTOM_SETUP);
        setWorkingForm(FO_CUSTOM_SETUP);
        setFormElement(CUSTOM_SETUP_FILE, file);
        submit("next");
    }

    protected void submitCvsSetupForm(String root, String module, String password, String path)
    {
        CvsForm.Create form = new CvsForm.Create(tester);
        form.assertFormPresent();
        form.nextFormElements(root, module, password, "", "", "");
    }

    protected void submitProjectBasicsForm(String projectName, String description, String url, String scm, String type)
    {
        assertFormPresent(FO_PROJECT_BASICS);
        setWorkingForm(FO_PROJECT_BASICS);
        setFormElement(PROJECT_BASICS_NAME, projectName);
        setFormElement(PROJECT_BASICS_DESCRIPTION, description);
        setFormElement(PROJECT_BASICS_URL, url);
        setFormElement(PROJECT_BASICS_SCM, scm);
        setFormElement(PROJECT_BASICS_TYPE, type);
        submit("next");
    }

    public void assertAndClick(String name)
    {
        assertLinkPresent(name);
        clickLink(name);
    }

    public String getEditId(String name)
    {
        return "edit_" + name;
    }
}
