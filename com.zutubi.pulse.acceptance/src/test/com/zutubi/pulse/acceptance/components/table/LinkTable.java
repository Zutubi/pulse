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

package com.zutubi.pulse.acceptance.components.table;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.util.WebUtils;
import org.openqa.selenium.By;

import java.util.LinkedList;
import java.util.List;

/**
 * Corresponds to the Zutubi.table.LinkTable JS component.
 */
public class LinkTable extends ContentTable
{
    public LinkTable(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }

    /**
     * Indicates if a link with the given name is present in the table.
     *
     * @param linkName name of the link to check for
     * @return true if the given link is present, false otherwise
     */
    public boolean isLinkPresent(String linkName)
    {
        return isLinkPresent(linkName, null);
    }

    /**
     * Indicates if a link with the given name in the given category is present
     * in the table.
     *
     * @param linkName name of the link to check for
     * @param category category the link belongs to (null if not categorised)
     * @return true if the given link is present, false otherwise
     */
    public boolean isLinkPresent(String linkName, String category)
    {
        return browser.isElementIdPresent(getLinkId(linkName, category));
    }

    /**
     * Clicks on the link with the given name.  The link must be present.  This
     * method returns immediately on clicking the link (i.e. it does not wait
     * for any effect).
     *
     * @param linkName name of the link to click on
     */
    public void clickLink(String linkName)
    {
        clickLink(linkName, null);
    }

    /**
     * Clicks on the link with the given name in the given category.  The link
     * must be present.  This method returns immediately on clicking the link
     * (i.e. it does not wait for any effect).
     *
     * @param linkName name of the link to click on
     * @param category category the link belongs to (null if not categorised)
     */
    public void clickLink(String linkName, String category)
    {
        browser.click(By.id(getLinkId(linkName, category)));
    }

    /**
     * Returns the DOM id that will be used for a link with the given name.
     *
     * @param linkName name of the link to get the id for
     * @return the DOM is that will be applied to the given link
     */
    public String getLinkId(String linkName)
    {
        return getLinkId(linkName, null);
    }

    /**
     * Returns the DOM id that will be used for a link with the given name in
     * the given category.
     *
     * @param linkName name of the link to get the id for
     * @param category category the link belongs to (null if not categorised)
     * @return the DOM is that will be applied to the given link
     */
    public String getLinkId(String linkName, String category)
    {
        return id + "-" + (category == null ? "" : WebUtils.toValidHtmlName(category) + "-") + WebUtils.toValidHtmlName(linkName);
    }

    /**
     * Indicates how many links are shown in the table.
     *
     * @return the number of links shown in the table
     */
    public long getLinkCount()
    {
        return (Long) browser.evaluateScript("return " + getComponentJS() + ".getDynamicCount();");
    }

    /**
     * Returns the label of the link at the given row in the table.
     *
     * @param index zero-based index of the row (starting from the first link -
     *        i.e. the table title is excluded)
     * @return the label of the link at the given index
     */
    public String getLinkLabel(int index)
    {
        return browser.getCellContents(getId(), index + 1, 0).trim();
    }

    /**
     * Returns the labels of all links in the table.
     *
     * @return the labels of all links shown in this table
     */
    public List<String> getLinkLabels()
    {
        List<String> result = new LinkedList<String>();
        long count = getLinkCount();
        for (int i = 0; i < count; i++)
        {
            result.add(getLinkLabel(i));
        }

        return result;
    }
}
