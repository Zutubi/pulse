package com.zutubi.pulse.acceptance.pages;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import junit.framework.Assert;

/**
 * Base for all pages, with basic functions for identifying page presence and
 * content.
 */
public abstract class SeleniumPage
{
    protected Selenium selenium;
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

    public SeleniumPage(Selenium selenium, String id)
    {
        this(selenium, id, null);
    }

    public SeleniumPage(Selenium selenium, String id, String title)
    {
        this.selenium = selenium;
        this.title = title;
        this.id = id;
    }

    public void goTo()
    {
        selenium.open(getUrl());
        waitFor();
    }

    public void waitFor()
    {
        SeleniumUtils.waitForElement(selenium, id);
    }

    public void assertPresent()
    {
        Assert.assertTrue(selenium.isElementPresent("id=" + id));

        if (title != null)
        {
            Assert.assertEquals(title, selenium.getTitle());
        }
    }

    public abstract String getUrl();
}
