package com.zutubi.pulse.acceptance.forms;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;

/**
 */
public class InstallPluginForm
{
    private static final String FORM_ID = "plugin.local";

    private Selenium selenium;

    public InstallPluginForm(Selenium selenium)
    {
        this.selenium = selenium;
    }

    public boolean isFormPresent()
    {
        return selenium.isElementPresent(FORM_ID);
    }

    public void waitFor()
    {
        SeleniumUtils.waitForElementId(selenium, FORM_ID);
    }

    public void continueFormElements(String path)
    {
        selenium.type("local.path", path);
        selenium.click("continue");
        selenium.waitForPageToLoad("30000");
    }
}
