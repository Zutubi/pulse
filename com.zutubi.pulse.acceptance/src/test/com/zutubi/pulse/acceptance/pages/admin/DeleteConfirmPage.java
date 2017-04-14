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

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.type.record.PathUtils;
import org.openqa.selenium.By;

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
        browser.click(By.id(CONFIRM_LINK));
    }

    public void clickCancel()
    {
        browser.click(By.id(CANCEL_LINK));
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
