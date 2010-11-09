package com.zutubi.pulse.acceptance.components;

import com.zutubi.pulse.acceptance.SeleniumBrowser;

/**
 * Corresponds to the Zutubi.pulse.project.CommentList JS component.
 */
public class CommentList extends Component
{
    public CommentList(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }

    @Override
    protected String getPresentExpression()
    {
        return "var data = " + getComponentJS() + ".data; data && data.length > 0";
    }

    /**
     * Indicates if a delete link is shown for the comment of the given number.
     * 
     * @param commentNumber one-based position of the comment in the list
     * @return true if the given comment has a delete link
     */
    public boolean isDeleteLinkPresent(int commentNumber)
    {
        return browser.isElementIdPresent(getDeleteLinkId(commentNumber));
    }

    /**
     * Clicks the delete link for the comment of the given number.
     * 
     * @param commentNumber one-based position of the comment in the list
     */
    public void clickDeleteLink(int commentNumber)
    {
        browser.click(getDeleteLinkId(commentNumber));
    }

    private String getDeleteLinkId(int commentNumber)
    {
        return "delete-comment-" + commentNumber;
    }
}
