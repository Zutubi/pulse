package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.type.record.PathUtils;
import org.openqa.selenium.By;

/**
 * The page shown when looking at the hierarchy view of a templated scope.
 */
public class HierarchyPage extends ConfigurationPanePage
{
    public static final String LINK_ADD = "add.new";
    public static final String LINK_ADD_TEMPLATE = "add.template";
    public static final String LINK_CLONE = "clone";
    public static final String LINK_CONFIGURE = "configure";
    public static final String LINK_DELETE = "delete";
    public static final String LINK_INTRODUCE_PARENT = "introduceparent";
    public static final String LINK_SMART_CLONE = "smartclone";
    public static final String LINK_MOVE = "move";

    protected String scope;
    protected String baseName;
    protected boolean template;

    public HierarchyPage(SeleniumBrowser browser, Urls urls, String scope, String baseName, boolean template)
    {
        super(browser, urls, PathUtils.getPath(scope, baseName));
        this.scope = scope;
        this.baseName = baseName;
        this.template = template;
    }

    public boolean isAddLinkPresent()
    {
        return isLinkPresent(LINK_ADD);
    }

    public boolean isAddTemplateLinkPresent()
    {
        return isLinkPresent(LINK_ADD_TEMPLATE);
    }

    public boolean isConfigureLinkPresent()
    {
        return isLinkPresent(LINK_CONFIGURE);
    }

    public boolean isTemplate()
    {
        return template;
    }
    
    protected boolean isLinkPresent(String link)
    {
        return browser.isLinkPresent(link);
    }

    public String getUrl()
    {
        return urls.admin() + scope + "/";
    }

    public void open()
    {
        browser.open(getUrl());
    }

    public void waitFor()
    {
        // Wait for the default right panel to load.
        waitForActionToComplete();

        // Choose our panel and wait for it.
        By panelLink = By.linkText(baseName);
        browser.waitForElement(panelLink);
        browser.click(panelLink);
        super.waitFor();
    }

    public boolean isTreeItemPresent(String baseName)
    {
        return browser.isElementPresent(By.linkText(baseName));
    }

    public boolean isTreeItemVisible(String baseName)
    {
        return browser.isVisible(By.linkText(baseName));
    }

    public void expandTreeItem(String baseName)
    {
        browser.doubleClick(By.linkText(baseName));
    }

    public void selectTreeItem(String baseName)
    {
        browser.waitAndClick(By.linkText(baseName));
    }

    public boolean isAddPresent()
    {
        return browser.isElementIdPresent(LINK_ADD);
    }

    public void clickAdd()
    {
        browser.waitAndClick(By.id(LINK_ADD));
    }

    public void clickAddTemplate()
    {
        browser.waitAndClick(By.id(LINK_ADD_TEMPLATE));
    }

    public boolean isClonePresent()
    {
        return browser.isElementIdPresent(LINK_CLONE);
    }

    public void clickClone()
    {
        browser.waitAndClick(By.id(LINK_CLONE));
    }

    public void clickSmartClone()
    {
        browser.waitAndClick(By.id(LINK_SMART_CLONE));
    }

    public boolean isIntroduceParentPresent()
    {
        return browser.isElementIdPresent(LINK_INTRODUCE_PARENT);
    }

    public void clickIntroduceParent()
    {
        browser.waitAndClick(By.id(LINK_INTRODUCE_PARENT));
    }
    
    public boolean isMovePresent()
    {
        return browser.isElementIdPresent(LINK_MOVE);
    }
    
    public void clickMove()
    {
        browser.waitAndClick(By.id(LINK_MOVE));
    }

    public void setTemplate(boolean template)
    {
        this.template = template;
    }

    public DeleteConfirmPage clickDelete()
    {
        browser.waitAndClick(By.id(LINK_DELETE));
        return browser.createPage(DeleteConfirmPage.class, getId(), false);
    }
}
