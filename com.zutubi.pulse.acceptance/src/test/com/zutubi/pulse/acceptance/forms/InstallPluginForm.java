package com.zutubi.pulse.acceptance.forms;

import com.zutubi.pulse.acceptance.SeleniumBrowser;

/**
 */
public class InstallPluginForm
{
    private static final String ID_PATH_FIELD = "zfid.pluginPath";
    private static final String ID_CONTINUE   = "zfid.continue";
    private static final String ID_CANCEL     = "zfid.cancel";

    private SeleniumBrowser browser;

    public InstallPluginForm(SeleniumBrowser browser)
    {
        this.browser = browser;
    }

    public boolean isFormPresent()
    {
        return browser.isElementIdPresent(ID_PATH_FIELD);
    }

    public void waitFor()
    {
        browser.waitForElement(ID_PATH_FIELD);
    }

    public void cancel()
    {
        browser.click(ID_CANCEL);
    }
    
    public void continueFormElements(String path)
    {
        browser.type(ID_PATH_FIELD, path);
        browser.click(ID_CONTINUE);
        browser.waitForPageToLoad();
    }
}
