package com.zutubi.pulse.acceptance.pages;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;

import static com.zutubi.util.WebUtils.toValidHtmlName;

/**
 * Base for all pages, with basic functions for identifying page presence and
 * content.
 */
public abstract class SeleniumPage
{
    public static final String TITLE_PREFIX = ":: pulse [alpha] :: ";

    protected SeleniumBrowser browser;
    /**
     * Used to determine urls for the page.
     */
    protected Urls urls;
    /**
     * Identifier of an element that is always on the page and can therefore
     * by used to determine the page's presence.
     */
    private String id;
    /**
     * The page title, or null if not title is expected (or it is too hard to
     * test).
     */
    private String title;

    public SeleniumPage(SeleniumBrowser browser, Urls urls, String id)
    {
        this(browser, urls, id, null);
    }

    public SeleniumPage(SeleniumBrowser browser, Urls urls, String id, String title)
    {
        this.browser = browser;
        this.urls = urls;
        this.title = title;
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    /**
     * A convenience method that opens and then waits for this page.
     *
     * @see #open()
     * @see #waitFor() 
     */
    public void openAndWaitFor()
    {
        open();
        waitFor();
    }

    /**
     * Open selenium at the page represented by this page instance
     */
    public void open()
    {
        browser.open(getUrl());
    }

    /**
     * Wait for this page to finish loading.
     *
     * @throws RuntimeException if this request times out.
     */
    public void waitFor()
    {
        browser.waitForElement(toValidHtmlName(id));
    }

    public void waitForPageToLoad()
    {
        browser.waitForPageToLoad();
    }

    /**
     * Wait for the browser to reload, and for this page to be available.
     */
    public void waitForReload()
    {
        browser.waitForPageToLoad();
        waitFor();
    }
    
    public boolean isPresent()
    {
        return browser.isElementPresent("id=" + toValidHtmlName(id));
    }

    public String getTitle()
    {
        return TITLE_PREFIX + title;
    }

    public boolean isElementIdPresent(String id)
    {
        return browser.isElementIdPresent(id);
    }

    public abstract String getUrl();
}
