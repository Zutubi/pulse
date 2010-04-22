package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;

import java.util.LinkedList;
import java.util.List;

/**
 * The move confirmation page, which shows the incompatible paths that need to
 * be deleted.
 */
public class MoveConfirmPage extends ConfigurationPanePage
{
    public static final String CONFIRM_LINK = "confirm.move";
    public static final String CANCEL_LINK = "cancel.move";

    public MoveConfirmPage(SeleniumBrowser browser, Urls urls, String path)
    {
        super(browser, urls, "move-" + path);
    }

    public String getUrl()
    {
        // We don't support direct navigation.
        return null;
    }

    public void clickMove()
    {
        browser.click(CONFIRM_LINK);
    }

    public void clickCancel()
    {
        browser.click(CANCEL_LINK);
    }

    public List<String> getDeletedPaths()
    {
        int count = 0;
        while (browser.isElementIdPresent("deleted-path-" + (count + 1)))
        {
            count++;
        }
        
        List<String> result = new LinkedList<String>();
        for (int i = 0; i < count; i++)
        {
            result.add(browser.getCellContents("deleted-paths", i + 1, 0));
        }
        
        return result;
    }
}