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
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * An abstract base for pages that are shown in the right pane of the
 * configuration UI.
 */
public abstract class ConfigurationPanePage extends SeleniumPage
{
    public ConfigurationPanePage(SeleniumBrowser browser, Urls urls, String id)
    {
        super(browser, urls, id);
    }

    public ConfigurationPanePage(SeleniumBrowser browser, Urls urls, String id, String title)
    {
        super(browser, urls, id, title);
    }

    public void waitFor()
    {
        super.waitFor();
        waitForActionToComplete();
    }

    protected void waitForActionToComplete()
    {
        browser.waitForVariable("actionInProgress", true);
    }
}
