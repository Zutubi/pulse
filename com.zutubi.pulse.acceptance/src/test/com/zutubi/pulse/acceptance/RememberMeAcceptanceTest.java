package com.zutubi.pulse.acceptance;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.ADMIN_CREDENTIALS;
import com.zutubi.pulse.acceptance.pages.LoginPage;
import static org.springframework.security.ui.rememberme.AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY;

public class RememberMeAcceptanceTest extends SeleniumTestBase
{
    private static final String USERNAME = ADMIN_CREDENTIALS.getUserName();
    private static final String PASSWORD = ADMIN_CREDENTIALS.getPassword();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        browser.deleteAllCookies();
    }

    public void testLoginWithRememberMe()
    {
        assertFalse(isRememberMeCookieSet());
        LoginPage page = browser.openAndWaitFor(LoginPage.class);
        assertTrue(page.login(USERNAME, PASSWORD, true));
        assertTrue(isRememberMeCookieSet());
    }

    public void testLoginWithoutRememberMe()
    {
        assertFalse(isRememberMeCookieSet());
        LoginPage page = browser.openAndWaitFor(LoginPage.class);
        assertTrue(page.login(USERNAME, PASSWORD, false));
        assertFalse(isRememberMeCookieSet());
    }

    public void testCookieClearedAfterLogout()
    {
        assertFalse(isRememberMeCookieSet());

        LoginPage page = browser.openAndWaitFor(LoginPage.class);
        assertTrue(page.login(USERNAME, PASSWORD, true));
        assertTrue(isRememberMeCookieSet());

        browser.logout();
        assertFalse(isRememberMeCookieSet());
    }

    public void testThatTheRememberMeCookieWorksAsAdvertised()
    {
        assertFalse(isRememberMeCookieSet());

        // login to get outselves a valid remember me cookie.
        LoginPage loginPage = browser.openAndWaitFor(LoginPage.class);
        assertTrue(loginPage.login(USERNAME, PASSWORD, true));
        assertTrue(isRememberMeCookieSet());

        String cookie = browser.getCookie(SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY);

        browser.newSession();
        browser.deleteAllCookies();

        // open the browser at '/' and ensure we are asked to login.
        browser.open("/");
        browser.waitForPageToLoad();
        assertTrue(loginPage.isPresent());
        
        browser.setCookie(SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY, cookie);
        browser.open("/");
        browser.waitForPageToLoad();
        assertFalse(loginPage.isPresent());
    }

    private boolean isRememberMeCookieSet()
    {
        return browser.isCookiePresent(SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY);
    }
}
