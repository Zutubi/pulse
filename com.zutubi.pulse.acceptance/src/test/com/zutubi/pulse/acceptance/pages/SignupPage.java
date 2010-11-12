package com.zutubi.pulse.acceptance.pages;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.SignupForm;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The anonymous user signup page.
 */
public class SignupPage extends SeleniumPage
{
    public SignupPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "signup");
    }

    @Override
    public String getUrl()
    {
        return urls.base() + "signup!input.action";
    }

    public SignupForm getForm()
    {
        return browser.createForm(SignupForm.class);
    }

}
