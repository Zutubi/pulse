package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.IDs;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.acceptance.forms.admin.CloneForm;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.StringUtils;

/**
 * A page in the admin UI that displays a list of composites.  The list is
 * shown in a table with annotations and actions.  This page applies to both
 * lists and maps in the configuration model.
 */
public class ListPage extends ConfigPage
{
    private static final String HEADER_ORDER = "order";

    public static final String ACTION_CLONE = "clone";
    public static final String ACTION_VIEW  = "view";

    public static final String ANNOTATION_INHERITED  = "inherited";
    public static final String ANNOTATION_OVERRIDDEN = "overridden";
    public static final String ANNOTATION_HIDDEN     = "hidden";

    protected static final String ADD_LINK = "map:add";
    
    private String path;

    public ListPage(Selenium selenium, Urls urls, String path)
    {
        super(selenium, urls, "map:path:" + path);
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    public String getUrl()
    {
        return urls.admin() + path + "/";
    }

    public boolean isItemPresent(String baseName)
    {
        return selenium.isElementPresent(StringUtils.toValidHtmlName(getItemId(baseName)));
    }

    public boolean isAnnotationPresent(String baseName, String annotation)
    {
        return selenium.isElementPresent(StringUtils.toValidHtmlName(annotation + ":" + baseName));
    }

    public boolean isActionLinkPresent(String baseName, String action)
    {
        return SeleniumUtils.isLinkPresent(selenium, getActionId(action, baseName));
    }

    public String getCellContent(int itemIndex, int columnIndex)
    {
        return SeleniumUtils.getCellContents(selenium, IDs.COLLECTION_TABLE, itemIndex + 2, columnIndex);
    }

    private String getItemId(String baseName)
    {
        return "item:" + baseName;
    }

    public String getActionId(String action, String baseName)
    {
        return action + ":" + baseName;
    }

    public void clickAdd()
    {
        selenium.click(ADD_LINK);
    }

    public void clickAction(String baseName, String action)
    {
        selenium.click(getActionId(action, baseName));
    }

    public void clickView(String baseName)
    {
        clickAction(baseName, ACTION_VIEW);
    }

    public CloneForm clickClone(String baseName)
    {
        clickAction(baseName, ACTION_CLONE);
        return new CloneForm(selenium, false);
    }

    public DeleteConfirmPage clickDelete(String baseName)
    {
        String actionId = getActionId("delete", baseName);
        boolean isHide = "hide".equals(selenium.getText(actionId));
        selenium.click(actionId);
        return new DeleteConfirmPage(selenium, urls, PathUtils.getPath(path, baseName), isHide);
    }

    public void clickRestore(String baseName)
    {
        clickRefreshingAction(baseName, "restore");
    }

    public boolean isOrderColumnPresent(int summaryColumnCount)
    {
        // The order column comes directly after the summary columns if
        // present, so its index is the same as the summary column count.
        return HEADER_ORDER.equals(SeleniumUtils.getCellContents(selenium, IDs.COLLECTION_TABLE, 1, summaryColumnCount));
    }

    public void clickUp(String baseName)
    {
        clickRefreshingAction(baseName, "up");
    }

    public void clickDown(String baseName)
    {
        clickRefreshingAction(baseName, "down");
    }

    private void clickRefreshingAction(String baseName, String action)
    {
        selenium.click(getActionId(action, baseName));
        SeleniumUtils.waitForVariable(selenium, "actionInProgress", SeleniumUtils.DEFAULT_TIMEOUT, true);
    }

    public boolean isOrderInheritedPresent()
    {
        return selenium.isElementPresent("order-inherited");
    }

    public boolean isOrderOverriddenPresent()
    {
        return selenium.isElementPresent("order-overridden");
    }
}
