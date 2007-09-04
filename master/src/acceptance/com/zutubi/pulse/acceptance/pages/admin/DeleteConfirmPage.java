package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.webwork.mapping.Urls;

/**
 * The delete record confirmation page, which shows the necessary actions for
 * a record to be deleted.
 */
public class DeleteConfirmPage extends SeleniumPage
{
    public static final String CONFIRM_LINK = "confirm.delete";
    public static final String CANCEL_LINK = "cancel.delete";
    private String path;

    public DeleteConfirmPage(Selenium selenium, Urls urls, String path)
    {
        super(selenium, urls, "delete:" + path);
        this.path = path;
    }

    public String getUrl()
    {
        // We don't support direct navigation.
        return null;
    }

    public ListPage confirm()
    {
        selenium.click(CONFIRM_LINK);
        return new ListPage(selenium, urls, PathUtils.getParentPath(path));
    }

    public ListPage cancel()
    {
        selenium.click(CANCEL_LINK);
        return new ListPage(selenium, urls, PathUtils.getParentPath(path));        
    }
}
