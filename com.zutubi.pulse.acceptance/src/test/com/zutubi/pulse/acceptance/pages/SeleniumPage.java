package com.zutubi.pulse.acceptance.pages;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;
import junit.framework.Assert;

/**
 * Base for all pages, with basic functions for identifying page presence and
 * content.
 */
public abstract class SeleniumPage
{
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
    private static final String TITLE_PREFIX = ":: pulse :: ";

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

    public void goTo()
    {
        selenium.open(getUrl());
        waitFor();
    }

    public void waitFor()
    {
        SeleniumUtils.waitForElementId(selenium, id);
    }

    public void assertPresent()
    {
        Assert.assertTrue(selenium.isElementPresent("id=" + StringUtils.toValidHtmlName(id)));

        if (title != null)
        {
            String gotTitle = selenium.getTitle();
            if(gotTitle.startsWith(TITLE_PREFIX))
            {
                gotTitle = gotTitle.substring(TITLE_PREFIX.length());
            }
            Assert.assertEquals(title, gotTitle);
        }
    }

    public abstract String getUrl();
}
