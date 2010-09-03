package com.zutubi.pulse.acceptance;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.ADMIN_CREDENTIALS;
import com.zutubi.pulse.acceptance.pages.LoginPage;
import com.thoughtworks.selenium.SeleniumException;
import static org.springframework.security.ui.rememberme.AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY;

public class RememberMeAcceptanceTest extends SeleniumTestBase
{
    private static final String USERNAME = ADMIN_CREDENTIALS.getUserName();
    private static final String PASSWORD = ADMIN_CREDENTIALS.getPassword();

    public void testLoginWithRememberMe()
    {
        assertFalse(isRememberMeCookieSet());
        LoginPage page = browser.openAndWaitFor(LoginPage.class);
        page.login(USERNAME, PASSWORD, true);
        assertTrue(isRememberMeCookieSet());
    }

    public void testLoginWithoutRememberMe()
    {
        assertFalse(isRememberMeCookieSet());
        LoginPage page = browser.openAndWaitFor(LoginPage.class);
        page.login(USERNAME, PASSWORD, false);
        assertFalse(isRememberMeCookieSet());
    }

    public void testCookieClearedAfterLogout()
    {
        assertFalse(isRememberMeCookieSet());

        LoginPage page = browser.openAndWaitFor(LoginPage.class);
        page.login(USERNAME, PASSWORD, true);
        assertTrue(isRememberMeCookieSet());

        browser.logout();
        assertFalse(isRememberMeCookieSet());
    }

    private boolean isRememberMeCookieSet()
    {
        try
        {
            browser.getCookie(SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY);
            return true;
        }
        catch (SeleniumException e)
        {
            return false;
        }
    }
}
