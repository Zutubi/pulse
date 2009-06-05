package com.zutubi.pulse.acceptance.pages;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;

/**
 * Base for all pages, with basic functions for identifying page presence and
 * content.
 */
public abstract class SeleniumPage
{
    public static final String TITLE_PREFIX = ":: pulse :: ";

    protected Selenium selenium;
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

    public SeleniumPage(Selenium selenium, Urls urls, String id)
    {
        this(selenium, urls, id, null);
    }

    public SeleniumPage(Selenium selenium, Urls urls, String id, String title)
    {
        this.selenium = selenium;
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
        selenium.open(getUrl());
    }

    /**
     * Wait for this page to finish loading.
     *
     * @throws RuntimeException if this request times out.
     */
    public void waitFor()
    {
        SeleniumUtils.waitForElementId(selenium, id);
    }

    public boolean isPresent()
    {
        return selenium.isElementPresent("id=" + StringUtils.toValidHtmlName(id));
    }

    public String getTitle()
    {
        return title;
    }

    public abstract String getUrl();
}
