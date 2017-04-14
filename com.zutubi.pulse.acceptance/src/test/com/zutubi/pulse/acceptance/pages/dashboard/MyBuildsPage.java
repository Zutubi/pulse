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
import com.zutubi.pulse.acceptance.components.pulse.project.BuildSummaryTable;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildInfo;
import com.zutubi.pulse.master.webwork.Urls;

import java.util.List;

/**
 * The my builds page shows a user's personal builds.
 */
public class MyBuildsPage extends SeleniumPage
{
    private BuildSummaryTable buildsTable;
    
    public MyBuildsPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "my-builds", "my");
        buildsTable = new BuildSummaryTable(browser, "my-builds-builds");
    }

    @Override
    public void waitFor()
    {
        super.waitFor();
        browser.waitForVariable("panel.initialised");
    }

    public String getUrl()
    {
        return urls.myBuilds();
    }

    public List<BuildInfo> getBuilds()
    {
        return buildsTable.getBuilds();
    }
}
