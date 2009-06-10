package com.zutubi.pulse.acceptance.pages;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.LoginForm;
import com.zutubi.pulse.acceptance.forms.SignupForm;
import com.zutubi.pulse.master.webwork.Urls;
import static com.zutubi.util.CollectionUtils.asPair;

/**
 */
public class LoginPage extends SeleniumPage
{
    private static final String SIGNUP_ID = "signup";

    public LoginPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "login-logo", "login");
    }

    public String getUrl()
    {
        return urls.login();
    }

    public void login(String username, String password)
    {
        LoginForm form = browser.createForm(LoginForm.class);
        form.submitNamedFormElements("login", asPair("j_username", username), asPair("j_password", password));
        browser.waitForPageToLoad();
    }

    public boolean isSignupPresent()
    {
        return browser.isElementIdPresent(SIGNUP_ID);
    }

    public void waitForSignup()
    {
        browser.waitForElement(SIGNUP_ID);
    }

    public SignupForm clickSignup()
    {
        browser.click(SIGNUP_ID);
        browser.waitForPageToLoad();
        return browser.createForm(SignupForm.class);
    }
}
