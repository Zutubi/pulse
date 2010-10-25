package com.zutubi.pulse.acceptance.components;

import com.zutubi.pulse.acceptance.SeleniumBrowser;

/**
 * Corresponds to the Zutubi.table.LinkTable JS component.
 */
public class LinkTable extends Component
{
    public LinkTable(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }

    /**
     * Indicates if a link with the given name is present in the table.
     *
     * @param linkName name of the link to check for
     * @return true if the given link is present, false otherwise
     */
    public boolean isLinkPresent(String linkName)
    {
        return browser.isElementPresent(getLinkId(linkName));
    }

    /**
     * Clicks on the link with the given name.  The link must be present.  This
     * method returns immediately on clicking the link (i.e. it oes not wait
     * for any effect).
     *
     * @param linkName name of the link to click on
     */
    public void clickLink(String linkName)
    {
        browser.click(getLinkId(linkName));
    }

    /**
     * Returns the DOM id that will be used for a link with the given name.
     *
     * @param linkName name of the link to get the id for
     * @return the DOM is that will be applied to the given link
     */
    public String getLinkId(String linkName)
    {
        return id + "-" + linkName;
    }
}
