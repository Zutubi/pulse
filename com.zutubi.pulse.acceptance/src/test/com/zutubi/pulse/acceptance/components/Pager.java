package com.zutubi.pulse.acceptance.components;

import com.zutubi.pulse.acceptance.SeleniumBrowser;

/**
 * Corresponds to the Zutubi.Pager JS component.
 */
public class Pager extends Component
{
    private static final String ID_SUFFIX_TOTAL = "-total";
    private static final String ID_SUFFIX_PAGING = "-paging";
    private static final String ID_SUFFIX_FIRST = "-first";
    private static final String ID_SUFFIX_PREVIOUS = "-previous";
    private static final String ID_SUFFIX_NEXT = "-next";
    private static final String ID_SUFFIX_LAST = "-last";

    public Pager(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }

    /**
     * Returns the total items count displayed by this pager.
     * 
     * @return the total items number displayed by this pager
     */
    public int getTotalItems()
    {
        String text = browser.getText(id + ID_SUFFIX_TOTAL);
        String[] pieces = text.trim().split("\\s+");
        return Integer.parseInt(pieces[0]);
    }

    /**
     * Returns the current page index (note that indexes are zero-based).
     * 
     * @return the current page index
     */
    public int getCurrentPage()
    {
        return Integer.parseInt(browser.evalExpression(getComponentJS() + ".data.currentPage"));
    }

    /**
     * Indicates if the paging (navigation) row is present.
     * 
     * @return true if the navigation row is present
     */
    public boolean hasPagingRow()
    {
        return browser.isElementIdPresent(id + ID_SUFFIX_PAGING);
    }
    
    /**
     * Indicates if there is a link to the first page.
     * 
     * @return true if there is a link to the first page
     */
    public boolean hasFirstLink()
    {
        return browser.isElementIdPresent(id + ID_SUFFIX_FIRST);
    }

    /**
     * Clicks the link to navigate to the first page.
     */
    public void clickFirst()
    {
        browser.click(id + ID_SUFFIX_FIRST);
    }
    
    /**
     * Indicates if there is a link to the previous page.
     * 
     * @return true if there is a link to the previous page
     */
    public boolean hasPreviousLink()
    {
        return browser.isElementIdPresent(id + ID_SUFFIX_PREVIOUS);
    }

    /**
     * Clicks the link to navigate to the previous page.
     */
    public void clickPrevious()
    {
        browser.click(id + ID_SUFFIX_PREVIOUS);
    }
    
    /**
     * Indicates if there is a link to the next page.
     * 
     * @return true if there is a link to the next page
     */
    public boolean hasNextLink()
    {
        return browser.isElementIdPresent(id + ID_SUFFIX_NEXT);
    }
    
    /**
     * Clicks the link to navigate to the next page.
     */
    public void clickNext()
    {
        browser.click(id + ID_SUFFIX_NEXT);
    }

    /**
     * Indicates if there is a link to the last page.
     * 
     * @return true if there is a link to the lat page
     */
    public boolean hasLastLink()
    {
        return browser.isElementIdPresent(id + ID_SUFFIX_LAST);
    }

    /**
     * Clicks the link to navigate to the last page.
     */
    public void clickLast()
    {
        browser.click(id + ID_SUFFIX_LAST);
    }
}
