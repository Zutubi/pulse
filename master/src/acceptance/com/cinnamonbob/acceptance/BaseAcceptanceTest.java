package com.cinnamonbob.acceptance;

import com.cinnamonbob.core.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 * <class-comment/>
 */
public abstract class BaseAcceptanceTest extends ExtendedWebTestCase
{
    //---( administrations create user form )---
    private static final String FO_USER_CREATE = "user.create";
    protected static final String USER_CREATE_LOGIN = "user.login";
    protected static final String USER_CREATE_NAME = "user.name";
    protected static final String USER_CREATE_PASSWORD = "user.password";
    protected static final String USER_CREATE_CONFIRM = "confirm";
    protected static final String USER_CREATE_ADMIN = "admin";

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

        getTestContext().setBaseUrl("http://localhost:8080/");
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
}
