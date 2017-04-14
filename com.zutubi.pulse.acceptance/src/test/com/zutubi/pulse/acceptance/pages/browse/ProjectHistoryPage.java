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
import com.zutubi.pulse.acceptance.pages.AbstractHistoryPage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;

/**
 * The project history page shows completed builds for a project.
 */
public class ProjectHistoryPage extends AbstractHistoryPage
{
    private String projectName;

    public ProjectHistoryPage(SeleniumBrowser browser, Urls urls, String projectName)
    {
        super(browser, urls, "project-history-" + projectName, "project-history");
        this.projectName = projectName;
    }

    @Override
    public String getUrl()
    {
        return urls.projectHistory(WebUtils.uriComponentEncode(projectName));
    }
}
