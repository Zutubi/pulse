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
import org.openqa.selenium.By;

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
        browser.click(By.id(CONFIRM_LINK));
    }

    public void clickCancel()
    {
        browser.click(By.id(CANCEL_LINK));
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