package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * base log page.
 */
public abstract class AbstractLogPage extends SeleniumPage 
{
    protected AbstractLogPage(SeleniumBrowser browser, Urls urls, String id)
    {
        super(browser, urls, id);
    }
    public boolean isDownloadLinkAvailable()
    {
        return browser.isElementIdPresent("link=full log");
    }

    public void clickDownloadLink()
    {
        browser.click("link=full log");
    }

    /**
     * Retrieve the visible log text
     *
     * @return log text.
     */
    public String getLog()
    {
        return browser.getText(getId());
    }

    public boolean logContains(String text)
    {
        return getLog().contains(text);
    }

    public boolean isLogAvailable()
    {
        return browser.isElementIdPresent(getId()) && !browser.isTextPresent("log file does not exist");
    }

    public boolean isLogNotAvailable()
    {
        return browser.isElementIdPresent(getId()) && browser.isTextPresent("log file does not exist");
    }
}
