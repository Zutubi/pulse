package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.IDs;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.admin.CloneForm;
import com.zutubi.pulse.acceptance.forms.admin.PullUpForm;
import com.zutubi.pulse.acceptance.forms.admin.PushDownForm;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.WebUtils;

import static com.zutubi.util.WebUtils.uriPathEncode;

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
    public static final String ANNOTATION_NONE       = "noan";

    public static final String ADD_LINK = "map:add";
    
    private String path;

    public ListPage(SeleniumBrowser browser, Urls urls, String path)
    {
        super(browser, urls, "map:path:" + path);
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    public String getUrl()
    {
        return urls.admin() + uriPathEncode(path) + "/";
    }

    public boolean isItemPresent(String baseName)
    {
        return browser.isElementIdPresent(getItemId(baseName));
    }

    public boolean isAnnotationPresent(String baseName, String annotation)
    {
        return browser.isElementIdPresent(WebUtils.toValidHtmlName(annotation + ":" + baseName));
    }

    public boolean isActionLinkPresent(String baseName, String action)
    {
        return browser.isLinkPresent(getActionId(action, baseName));
    }

    public String getCellContent(int itemIndex, int columnIndex)
    {
        return browser.getCellContents(IDs.COLLECTION_TABLE, itemIndex + 2, columnIndex);
    }

    private String getItemId(String baseName)
    {
        return WebUtils.toValidHtmlName("item:" + baseName);
    }

    public String getActionId(String action, String baseName)
    {
        return WebUtils.toValidHtmlName(action + ":" + baseName);
    }

    public void clickAdd()
    {
        browser.click(ADD_LINK);
    }

    public void clickAction(String baseName, String action)
    {
        browser.click(getActionId(action, baseName));
    }

    public void clickView(String baseName)
    {
        clickAction(baseName, ACTION_VIEW);
    }

    public CloneForm clickClone(String baseName)
    {
        clickAction(baseName, ACTION_CLONE);
        return browser.createForm(CloneForm.class, false);
    }

    public PullUpForm clickPullUp(String baseName)
    {
        clickAction(baseName, ConfigurationRefactoringManager.ACTION_PULL_UP);
        return browser.createForm(PullUpForm.class);
    }

    public PushDownForm clickPushDown(String baseName)
    {
        clickAction(baseName, ConfigurationRefactoringManager.ACTION_PUSH_DOWN);
        return browser.createForm(PushDownForm.class);
    }

    public DeleteConfirmPage clickDelete(String baseName)
    {
        String actionId = getActionId("delete", baseName);
        boolean isHide = "hide".equals(browser.getText(actionId));
        browser.click(actionId);
        return browser.createPage(DeleteConfirmPage.class, PathUtils.getPath(path, baseName), isHide);
    }

    public void clickRestore(String baseName)
    {
        clickRefreshingAction(baseName, "restore");
    }

    public boolean isOrderColumnPresent(int summaryColumnCount)
    {
        // The order column comes directly after the summary columns if
        // present, so its index is the same as the summary column count.
        return HEADER_ORDER.equals(browser.getCellContents(IDs.COLLECTION_TABLE, 1, summaryColumnCount));
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
        browser.click(getActionId(action, baseName));
        browser.waitForVariable("actionInProgress", true);
    }

    public boolean isOrderInheritedPresent()
    {
        return browser.isElementIdPresent("order-inherited");
    }

    public boolean isOrderOverriddenPresent()
    {
        return browser.isElementIdPresent("order-overridden");
    }

    public boolean isItemPresent(String baseName, String annotation, String... actions)
    {
        if (!isItemPresent(baseName))
        {
            return false;
        }

        if(annotation == null)
        {
            annotation = ListPage.ANNOTATION_NONE;
        }
        if (!isAnnotationPresent(baseName, annotation))
        {
            return false;
        }

        for(String action: actions)
        {
            if (!isActionLinkPresent(baseName, action))
            {
                return false;
            }
        }
        return true;
    }

}
