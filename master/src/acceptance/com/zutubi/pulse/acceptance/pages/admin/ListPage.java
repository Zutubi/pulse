package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.webwork.mapping.Urls;

/**
 * A page in the admin UI that displays a list of composites.  The list is
 * shown in a table with edit and delete links, and there are no child nodes.
 */
public class ListPage extends SeleniumPage
{
    private static final String ADD_LINK = "map:add";

    private String path;

    public ListPage(Selenium selenium, Urls urls, String path)
    {
        super(selenium, urls, "map:path:" + path);
        this.path = path;
    }

    public String getUrl()
    {
        return urls.admin() + path + "/";
    }

    public void assertItemPresent(String baseName, String... actions)
    {
        SeleniumUtils.assertElementPresent(selenium, getItemId(baseName));
        for(String action: actions)
        {
            SeleniumUtils.assertLinkPresent(selenium, getActionId(action, baseName));
        }
    }

    public void assertItemNotPresent(String baseName)
    {
        SeleniumUtils.assertElementNotPresent(selenium, getItemId(baseName));
    }

    private String getItemId(String baseName)
    {
        return "item:" + baseName;
    }

    public void assertActionsNotPresent(String baseName, String... actions)
    {
        for(String action: actions)
        {
            SeleniumUtils.assertLinkNotPresent(selenium, getActionId(action, baseName));
        }
    }

    private String getActionId(String action, String baseName)
    {
        return action + ":" + baseName;
    }

    public void addItem()
    {
        selenium.click(ADD_LINK);
    }

    public DeleteConfirmPage deleteItem(String baseName)
    {
        selenium.click(getActionId("delete", baseName));
        return new DeleteConfirmPage(selenium, urls, PathUtils.getPath(path, baseName));
    }
}
