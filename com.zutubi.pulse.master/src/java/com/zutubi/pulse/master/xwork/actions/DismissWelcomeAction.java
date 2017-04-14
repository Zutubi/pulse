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

package com.zutubi.pulse.master.xwork.actions;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * An action to change the users default page to their dashboard once they
 * tire of the welcome page.
 */
public class DismissWelcomeAction extends ActionSupport
{
    private ConfigurationProvider configurationProvider;

    public String execute() throws Exception
    {
        String login = SecurityUtils.getLoggedInUsername();
        User user = userManager.getUser(login);
        UserPreferencesConfiguration preferences = configurationProvider.deepClone(user.getPreferences());
        preferences.setDefaultAction(DefaultAction.DASHBOARD_ACTION);
        configurationProvider.save(preferences);
        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
