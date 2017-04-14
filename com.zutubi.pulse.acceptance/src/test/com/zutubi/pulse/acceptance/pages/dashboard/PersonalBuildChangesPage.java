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

package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;
import org.openqa.selenium.By;

/**
 * The summary tab for a personal build result.
 */
public class PersonalBuildChangesPage extends SeleniumPage
{
    private long buildId;

    public PersonalBuildChangesPage(SeleniumBrowser browser, Urls urls, long buildId)
    {
        super(browser, urls, "personal-build-" + Long.toString(buildId) + "-changes", "build " + buildId);
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.myBuildChanges(Long.toString(buildId));
    }

    public String getCheckedOutRevision()
    {
        String text = browser.getText(By.id("checked-out-revision"));
        text = text.trim();
        String[] pieces = text.split(" ");
        return pieces[pieces.length - 1];
    }

    public String getChangedFile(int index)
    {
        return browser.getCellContents(getId(), index + 2, 0);
    }
}
