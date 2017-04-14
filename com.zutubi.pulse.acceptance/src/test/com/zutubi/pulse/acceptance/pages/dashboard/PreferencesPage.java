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
import com.zutubi.pulse.acceptance.pages.admin.CompositePage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.type.record.PathUtils;

import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.USERS_SCOPE;

/**
 * The user's preferences page: configuration for the user under their dashboard.
 */
public class PreferencesPage extends CompositePage
{
    public PreferencesPage(SeleniumBrowser browser, Urls urls, String user)
    {
        super(browser, urls, PathUtils.getPath(USERS_SCOPE, user, "preferences"));
    }

    @Override
    public String getUrl()
    {
        return urls.preferences();
    }
}
