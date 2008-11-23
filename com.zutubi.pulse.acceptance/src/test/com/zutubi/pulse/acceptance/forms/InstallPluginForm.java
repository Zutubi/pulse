package com.zutubi.pulse.acceptance.forms;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;

/**
 */
public class InstallPluginForm
{
    private static final String ID_PATH_FIELD = "zfid.pluginPath";
    private static final String ID_CONTINUE   = "zfid.continue";
    private static final String ID_CANCEL     = "zfid.cancel";

    private Selenium selenium;

    public InstallPluginForm(Selenium selenium)
    {
        this.selenium = selenium;
    }

    public boolean isFormPresent()
    {
        return selenium.isElementPresent(ID_PATH_FIELD);
    }

    public void waitFor()
    {
        SeleniumUtils.waitForElementId(selenium, ID_PATH_FIELD);
    }

    public void cancel()
    {
        selenium.click(ID_CANCEL);
    }
    
    public void continueFormElements(String path)
    {
        selenium.type(ID_PATH_FIELD, path);
        selenium.click(ID_CONTINUE);
        selenium.waitForPageToLoad("30000");
    }
}
