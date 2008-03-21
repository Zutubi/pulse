package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.webwork.mapping.Urls;
import junit.framework.Assert;

/**
 * The delete record confirmation page, which shows the necessary actions for
 * a record to be deleted.
 */
public class DeleteConfirmPage extends SeleniumPage
{
    public static final String CONFIRM_LINK = "confirm.delete";
    public static final String CANCEL_LINK = "cancel.delete";
    private String path;
    private boolean hide;

    public DeleteConfirmPage(Selenium selenium, Urls urls, String path, boolean hide)
    {
        super(selenium, urls, "delete:" + path);
        this.path = path;
        this.hide = hide;
    }

    public String getUrl()
    {
        // We don't support direct navigation.
        return null;
    }

    public void clickDelete()
    {
        selenium.click(CONFIRM_LINK);
    }

    public void clickCancel()
    {
        selenium.click(CANCEL_LINK);
    }

    public ListPage confirmDeleteListItem()
    {
        clickDelete();
        ListPage listPage = new ListPage(selenium, urls, PathUtils.getParentPath(path));
        listPage.waitFor();
        return listPage;
    }

    public CompositePage confirmDeleteSingleton()
    {
        clickDelete();
        CompositePage compositePage = new CompositePage(selenium, urls, PathUtils.getParentPath(path));
        compositePage.waitFor();
        return compositePage;
    }

    public ListPage cancel()
    {
        selenium.click(CANCEL_LINK);
        return new ListPage(selenium, urls, PathUtils.getParentPath(path));        
    }

    public void assertTasks(String... pathActionPairs)
    {
        if(pathActionPairs.length % 2 != 0)
        {
            Assert.fail("Tasks must be made up of (path, action) pairs");
        }

        int i;
        for(i = 0; i < pathActionPairs.length / 2; i++)
        {
            SeleniumUtils.assertCellContents(selenium, getId(), i + 1, 0, pathActionPairs[i * 2]);
            SeleniumUtils.assertCellContents(selenium, getId(), i + 1, 1, pathActionPairs[i * 2 + 1]);
        }

        String actionsCell = SeleniumUtils.getCellContents(selenium, getId(), i + 1, 0);
        actionsCell = actionsCell.replaceAll(" +", " ");
        Assert.assertEquals((hide ? "hide" : "delete") + " cancel", actionsCell);
    }
}
