package com.zutubi.pulse.acceptance.pages;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.LoginForm;
import com.zutubi.pulse.master.webwork.Urls;
import org.openqa.selenium.By;

import static com.zutubi.util.CollectionUtils.asPair;

public class LoginPage extends SeleniumPage
{
    public static final String FIELD_USERNAME = "j_username";
    public static final String FIELD_PASSWORD = "j_password";
    public static final String FIELD_REMEMBERME = "_spring_security_remember_me";

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

    public boolean login(String username, String password)
    {
        return login(username, password, false);
    }

    public boolean login(String username, String password, boolean rememberMe)
    {
        LoginForm form = browser.createForm(LoginForm.class);
        form.waitFor();
        form.submitNamedFormElements(TITLE,
                asPair(FIELD_USERNAME, username),
                asPair(FIELD_PASSWORD, password),
                asPair(FIELD_REMEMBERME, Boolean.toString(rememberMe))
        );

        return !form.isFormPresent();
    }

    public boolean isSignupLinkPresent()
    {
        return browser.isElementIdPresent(SIGNUP_ID);
    }

    public void waitForSignup()
    {
        browser.waitForElement(SIGNUP_ID);
    }

    public SignupPage clickSignup()
    {
        browser.click(By.id(SIGNUP_ID));
        return browser.createPage(SignupPage.class);
    }
}
