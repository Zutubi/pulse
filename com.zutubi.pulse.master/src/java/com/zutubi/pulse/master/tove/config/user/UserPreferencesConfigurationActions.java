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

package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.tove.config.ConfigurationProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Actions available for user preferences.
 */
public class UserPreferencesConfigurationActions
{
    public static final String ACTION_CHANGE_PASSWORD = "changePassword";
    
    private ConfigurationProvider configurationProvider;
    private UserManager userManager;

    public List<String> getActions(UserPreferencesConfiguration userPreferences)
    {
        UserConfiguration user = configurationProvider.getAncestorOfType(userPreferences, UserConfiguration.class);
        if (user.isAuthenticatedViaLdap())
        {
            return Collections.emptyList();
        }
        else
        {
            return Arrays.asList(ACTION_CHANGE_PASSWORD);
        }
    }

    public void doChangePassword(UserPreferencesConfiguration userPreferences, ChangePasswordConfiguration password)
    {
        UserConfiguration user = configurationProvider.getAncestorOfType(userPreferences, UserConfiguration.class);
        userManager.setPassword(user, password.getNewPassword());
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}