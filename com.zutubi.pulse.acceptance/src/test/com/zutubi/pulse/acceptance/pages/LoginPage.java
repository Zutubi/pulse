package com.zutubi.pulse.acceptance.pages;

import com.thoughtworks.selenium.SeleniumException;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.LoginForm;
import com.zutubi.pulse.acceptance.forms.SignupForm;
import com.zutubi.pulse.master.webwork.Urls;

import java.io.File;

import static com.zutubi.util.CollectionUtils.asPair;

public class LoginPage extends SeleniumPage
{
    public static final String FIELD_USERNAME = "j_username";
    public static final String FIELD_PASSWORD = "j_password";

    private static final String SIGNUP_ID = "signup";
    private static final String TITLE = "login";

    public LoginPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "login-logo", TITLE);
    }

    public String getUrl()
    {
        return urls.login();
    }

    public WelcomePage login(String username, String password)
    {
        LoginForm form = browser.createForm(LoginForm.class);
        form.waitFor();
        form.submitNamedFormElements(TITLE, asPair(FIELD_USERNAME, username), asPair(FIELD_PASSWORD, password));
        try
        {
            WelcomePage welcomePage = browser.createPage(WelcomePage.class);
            welcomePage.waitFor();
            return welcomePage;
        }
        catch (Exception e)
        {
            File failureFile = browser.captureFailure();
            throw new SeleniumException("Failure while waiting to login (see: " + failureFile.getName() + "): " + e.getMessage(), e);
        }
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
