package com.zutubi.pulse.acceptance.components.pulse.project;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.Component;
import org.openqa.selenium.By;

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
    protected String getPresentScript()
    {
        return "var data = " + getComponentJS() + ".data; return data && data.length > 0;";
    }

    /**
     * Indicates if a comment with the given id is present.
     *
     * @param commentId unique id of the comment
     * @return true if the comment is present, false otherwise
     */
    public boolean isCommentPresent(long commentId)
    {
        return browser.isElementIdPresent("comment-" + commentId);
    }

    /**
     * Indicates if a delete link is shown for the comment of the given id.
     * 
     * @param commentId unique id of the comment
     * @return true if the given comment has a delete link
     */
    public boolean isDeleteLinkPresent(long commentId)
    {
        return browser.isElementIdPresent(getDeleteLinkId(commentId));
    }

    /**
     * Clicks the delete link for the comment of the given id.
     * 
     * @param commentId unique id of the comment
     */
    public void clickDeleteLink(long commentId)
    {
        browser.click(By.id(getDeleteLinkId(commentId)));
    }

    private String getDeleteLinkId(long commentId)
    {
        return "delete-comment-" + commentId;
    }
}
