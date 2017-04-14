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
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;

/**
 * A page that represents the stage log page.
 */
public class StageLogPage extends AbstractLogPage
{
    protected String project;
    protected String buildNumber;
    protected String stageName;

    public StageLogPage(SeleniumBrowser browser, Urls urls, String projectName, long buildNumber, String stageName)
    {
        super(browser, urls, "stage-log-" + projectName + "-" + buildNumber + "-" + stageName);
        this.project = projectName;
        this.buildNumber = String.valueOf(buildNumber);
        this.stageName = stageName;
    }

    public String getUrl()
    {
        return urls.stageLog(WebUtils.uriComponentEncode(project), buildNumber, stageName);
    }
}
