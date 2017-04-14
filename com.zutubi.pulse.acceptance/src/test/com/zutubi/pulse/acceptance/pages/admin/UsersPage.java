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
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.USERS_SCOPE;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * Simple specialisation of a list page for the admin/users tab.
 */
public class UsersPage extends ListPage
{
    private static final String STATE_ACTIVE_COUNT = "activeCount";

    public UsersPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, USERS_SCOPE);
    }

    public boolean isActiveCountPresent()
    {
        return isStateFieldPresent(STATE_ACTIVE_COUNT);
    }

    public String getActiveCount()
    {
        return getStateField(STATE_ACTIVE_COUNT);
    }
}
