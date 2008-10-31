package com.zutubi.pulse.acceptance.forms;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;

/**
 */
public class InstallPluginForm
{
    private static final String PATH_FIELD_ID = "zfid.pluginPath";

    private Selenium selenium;

    public InstallPluginForm(Selenium selenium)
    {
        this.selenium = selenium;
    }

    public boolean isFormPresent()
    {
        return selenium.isElementPresent(PATH_FIELD_ID);
    }

    public void waitFor()
    {
        SeleniumUtils.waitForElementId(selenium, PATH_FIELD_ID);
    }

    public void continueFormElements(String path)
    {
        selenium.type(PATH_FIELD_ID, path);
        selenium.click("zfid.continue");
        selenium.waitForPageToLoad("30000");
    }
}
