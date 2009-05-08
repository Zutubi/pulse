package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;

/**
 * Represents the popup prompt show when taking responsibilty for a build.
 */
public class TakeResponsibilityDialog
{
    private static final String LOCATOR_COMMENT = "css=input.ext-mb-input";
    private static final String LOCATOR_OK = "css=button:contains('OK')";

    private Selenium selenium;

    public TakeResponsibilityDialog(Selenium selenium)
    {
        this.selenium = selenium;
    }

    public void waitFor()
    {
        SeleniumUtils.waitForLocator(selenium, LOCATOR_COMMENT);
    }

    public void typeComment(String comment)
    {
        selenium.type(LOCATOR_COMMENT, comment);
    }

    public void clickOk()
    {
        selenium.click(LOCATOR_OK);
    }
}
