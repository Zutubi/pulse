/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.LoginPage;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.ADMIN_CREDENTIALS;
import static org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY;

public class RememberMeAcceptanceTest extends AcceptanceTestBase
{
    private static final String USERNAME = ADMIN_CREDENTIALS.getUserName();
    private static final String PASSWORD = ADMIN_CREDENTIALS.getPassword();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        getBrowser().deleteAllCookies();
    }

    public void testLoginWithRememberMe()
    {
        assertFalse(isRememberMeCookieSet());
        LoginPage page = getBrowser().openAndWaitFor(LoginPage.class);
        page.login(USERNAME, PASSWORD, true);
        assertTrue(getBrowser().isLoggedIn());
        assertTrue(isRememberMeCookieSet());
    }

    public void testLoginWithoutRememberMe()
    {
        assertFalse(isRememberMeCookieSet());
        LoginPage page = getBrowser().openAndWaitFor(LoginPage.class);
        page.login(USERNAME, PASSWORD, false);
        assertTrue(getBrowser().isLoggedIn());
        assertFalse(isRememberMeCookieSet());
    }

    public void testCookieClearedAfterLogout()
    {
        assertFalse(isRememberMeCookieSet());

        LoginPage page = getBrowser().openAndWaitFor(LoginPage.class);
        page.login(USERNAME, PASSWORD, true);
        assertTrue(getBrowser().isLoggedIn());
        assertTrue(isRememberMeCookieSet());

        getBrowser().logout();
        assertFalse(isRememberMeCookieSet());
    }

    /*
     FIXME: temporarily disabled to facilitate 2.3.0 release.
    public void testThatTheRememberMeCookieWorksAsAdvertised()
    {
        assertFalse(isRememberMeCookieSet());

        // login to get ourselves a valid remember me cookie.
        LoginPage loginPage = getBrowser().openAndWaitFor(LoginPage.class);
        assertTrue(loginPage.login(USERNAME, PASSWORD, true));
        assertTrue(isRememberMeCookieSet());

        String cookie = getBrowser().getCookie(SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY);

        getBrowser().setCaptureNetworkTraffic(true);
        getBrowser().newSession();
        getBrowser().deleteAllCookies();

        // open the browser at '/' and ensure we are asked to login.
        getBrowser().open("/");
        getBrowser().waitForPageToLoad();
        assertTrue("Login page is expected.", loginPage.isPresent());
        getBrowser().resetCapturedNetworkTraffic();
        getBrowser().setCookie(SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY, cookie);
        getBrowser().open("/");
        getBrowser().waitForPageToLoad();
        assertFalse("Login page is not expected.", loginPage.isPresent());

        System.out.println(getBrowser().getCapturedNetworkTraffic());
    }
    */
    
    private boolean isRememberMeCookieSet()
    {
        return getBrowser().isCookiePresent(SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY);
    }
}
