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

/**
 * A page that represents a personal build's logs tab.  The build log is shown by default.
 */
public class PersonalBuildLogPage extends BuildLogPage
{
    public PersonalBuildLogPage(SeleniumBrowser browser, Urls urls, String projectName, long buildNumber)
    {
        super(browser, urls, projectName, buildNumber);
    }

    @Override
    public String getUrl()
    {
        return urls.myBuildLog(buildNumber);
    }
}
