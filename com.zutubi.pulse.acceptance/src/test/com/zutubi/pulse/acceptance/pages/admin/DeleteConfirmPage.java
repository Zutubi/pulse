package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
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

    public DeleteConfirmPage(SeleniumBrowser browser, Urls urls, String path, boolean hide)
    {
        super(browser, urls, "delete:" + path);
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
        browser.click(CONFIRM_LINK);
    }

    public void clickCancel()
    {
        browser.click(CANCEL_LINK);
    }

    public ListPage confirmDeleteListItem()
    {
        clickDelete();
        return browser.waitFor(ListPage.class, PathUtils.getParentPath(path));
    }

    public CompositePage confirmDeleteSingleton()
    {
        clickDelete();
        return browser.waitFor(CompositePage.class, PathUtils.getParentPath(path));
    }
}
