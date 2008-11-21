package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.type.record.PathUtils;

/**
 * The delete record confirmation page, which shows the necessary actions for
 * a record to be deleted.
 */
public class DeleteConfirmPage extends ConfigurationPanePage
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

    public boolean isHide()
    {
        return hide;
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
}
