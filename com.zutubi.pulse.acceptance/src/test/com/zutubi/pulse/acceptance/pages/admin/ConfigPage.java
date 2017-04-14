/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.IDs;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.type.record.PathUtils;
import org.openqa.selenium.By;

/**
 * Base for config pages that appear when the "configuration" tab is selected
 * in the tree pane.
 */
public abstract class ConfigPage extends ConfigurationPanePage
{
    private static final String ID_COLLAPSE_ALL = "toolbar.collapse.all";

    public ConfigPage(SeleniumBrowser browser, Urls urls, String id)
    {
        super(browser, urls, id);
    }

    public ConfigPage(SeleniumBrowser browser, Urls urls, String id, String title)
    {
        super(browser, urls, id, title);
    }

    public abstract String getPath();

    public CompositePage clickComposite(String relativePath, String displayName)
    {
        browser.click(By.xpath(getTreeLinkXPath(displayName)));
        return browser.createPage(CompositePage.class, PathUtils.getPath(getPath(), relativePath));
    }

    public ListPage clickCollection(String baseName, String displayName)
    {
        browser.click(By.xpath(getTreeLinkXPath(displayName)));
        return browser.createPage(ListPage.class, PathUtils.getPath(getId(), baseName));
    }

    public void expandTreeNode(String path)
    {
        String nodeExpression = "configTree.getNodeByConfigPath('" + path + "')";
        browser.evaluateScript(nodeExpression + ".expand(false, false);");
        browser.waitForCondition("return " + nodeExpression + ".isExpanded();");
    }

    public String getTreeLinkXPath(String displayName)
    {
        return "//div[@id='config-tree']//a[span='" + displayName + "']";
    }

    public boolean isTreeLinkPresent(String displayName)
    {
        return browser.isElementPresent(By.xpath(getTreeLinkXPath(displayName)));
    }

    public boolean isTreeLinkVisible(String displayName)
    {
        return browser.isVisible(By.xpath(getTreeLinkXPath(displayName)));
    }
    
    public void clickCollapseAll()
    {
        browser.click(By.id(ID_COLLAPSE_ALL));
    }

    protected String getHierarchyXPath()
    {
        return "//span[text()='hierarchy']";
    }

    public boolean isCollapsedCollectionPresent()
    {
        return browser.isElementIdPresent(IDs.COLLECTION_TABLE);
    }

    public boolean isLinksBoxPresent()
    {
        return browser.isElementIdPresent(IDs.LINKS_BOX);
    }

    public String getStateId()
    {
        return "config-state";
    }

    public boolean isStatePresent()
    {
        return browser.isElementIdPresent(getStateId());
    }

    public String getStateFieldId(String name)
    {
        return "state." + name;
    }

    public boolean isStateFieldPresent(String field)
    {
        return browser.isElementIdPresent(getStateFieldId(field));
    }

    public String getStateField(String name)
    {
        return browser.getText(By.id(getStateFieldId(name)));
    }

    public boolean isStateFieldExpandable(String field)
    {
        return browser.isElementIdPresent(getStateFieldExpandId(field));
    }

    public boolean isStateFieldExpandVisible(String field)
    {
        return browser.isVisible(By.id(getStateFieldExpandId(field)));
    }

    public void expandStateField(String field)
    {
        browser.click(By.id(getStateFieldExpandId(field)));
        browser.waitForVisible(getStateFieldCollapseId(field));
    }

    public boolean isStateFieldCollapseVisible(String field)
    {
        return browser.isVisible(By.id(getStateFieldCollapseId(field)));
    }

    public void collapseStateField(String field)
    {
        browser.click(By.id(getStateFieldCollapseId(field)));
        browser.waitForVisible(getStateFieldExpandId(field));
    }

    private String getStateFieldExpandId(String field)
    {
        return getStateFieldId(field) + ".expand";
    }

    private String getStateFieldCollapseId(String field)
    {
        return getStateFieldId(field) + ".collapse";
    }
}
