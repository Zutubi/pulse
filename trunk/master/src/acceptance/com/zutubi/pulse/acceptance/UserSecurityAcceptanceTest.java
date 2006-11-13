package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.LoginForm;
import org.acegisecurity.ui.rememberme.TokenBasedRememberMeServices;

/**
 * <class-comment/>
 */
public class UserSecurityAcceptanceTest extends BaseAcceptanceTestCase
{
    public UserSecurityAcceptanceTest()
    {
    }

    public UserSecurityAcceptanceTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testLoginLogout()
    {
        // test that we can log in to a secured resource.
        beginAt("/");
        LoginForm loginForm = new LoginForm(tester);

        // we know that the user exists with credentials admin:admin
        loginForm.assertFormPresent();
        loginForm.loginFormElements("admin", "admin", "false");
        loginForm.assertFormNotPresent();

        assertAndClick(Navigation.LINK_LOGOUT);

        // assert that we are back at the login page.
        loginForm.assertFormPresent();
    }

    public void testLoginValidation()
    {
        // test that we can log in to a secured resource.
        beginAt("/");
        LoginForm loginForm = new LoginForm(tester);

        // we know that the user exists with credentials admin:admin

        loginForm.assertFormPresent();
        loginForm.loginFormElements("admin", "", "false");
        assertTextPresent("name or password incorrect");

        loginForm.loginFormElements("", "admin", "false");
        assertTextPresent("name or password incorrect");

        // credentials are case sensitive.
        loginForm.loginFormElements("admin", "ADMIN", "false");
        assertTextPresent("name or password incorrect");
    }

    public void testRememberMeCookieSet()
    {
        // test that we can log in to a secured resource.
        beginAt("/");
        LoginForm loginForm = new LoginForm(tester);
        loginForm.loginFormElements("admin", "admin", "true");

        assertCookieSet(TokenBasedRememberMeServices.ACEGI_SECURITY_HASHED_REMEMBER_ME_COOKIE_KEY);
    }

    public void testRememberMeCookieNotSet()
    {
        beginAt("/");
        LoginForm loginForm = new LoginForm(tester);
        loginForm.loginFormElements("admin", "admin", "false");

        assertCookieNotSet(TokenBasedRememberMeServices.ACEGI_SECURITY_HASHED_REMEMBER_ME_COOKIE_KEY);
    }

    public void testExplicitLogoutExpiresCookie()
    {
        // test that we can log in to a secured resource.
        beginAt("/");
        LoginForm loginForm = new LoginForm(tester);
        loginForm.loginFormElements("admin", "admin", "true");

        String cookieName = TokenBasedRememberMeServices.ACEGI_SECURITY_HASHED_REMEMBER_ME_COOKIE_KEY;

        // we have the login cookie
        assertCookieSet(cookieName);

        // logging out should set a nother cookie, this time one that expires now.
        assertAndClick(Navigation.LINK_LOGOUT);

        // check that the cookie has expired.
        assertCookieValue(cookieName, "");
    }
}
