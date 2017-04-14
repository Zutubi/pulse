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

package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;
import org.openqa.selenium.By;

/**
 * The pulse file tab for a build result.
 */
public class BuildFilePage extends SeleniumPage
{
    private static final String ID_DOWNLOAD_LINK = "download-file";
    private static final String ID_HIGHLIGHTED_FILE = "highlighted-file";

    private String projectName;
    private long buildId;

    public BuildFilePage(SeleniumBrowser browser, Urls urls, String projectName, long buildId)
    {
        super(browser, urls, projectName + "-build-" + Long.toString(buildId) + "-file", "build " + buildId);
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildFile(WebUtils.uriComponentEncode(projectName), Long.toString(buildId));
    }

    public boolean isHighlightedFilePresent()
    {
        return browser.isElementIdPresent(ID_HIGHLIGHTED_FILE);
    }

    public boolean isDownloadLinkPresent()
    {
        return browser.isElementIdPresent(ID_DOWNLOAD_LINK);
    }

    public void clickDownload()
    {
        browser.click(By.id(ID_DOWNLOAD_LINK));
    }
}
