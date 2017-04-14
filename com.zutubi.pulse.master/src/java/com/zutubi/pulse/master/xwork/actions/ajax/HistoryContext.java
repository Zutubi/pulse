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

package com.zutubi.pulse.master.xwork.actions.ajax;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * Manages storage and retrieval of history page related preferences.
 */
public class HistoryContext
{
    public static final String SESSION_KEY_BUILDS_PER_PAGE = "pulse.historyBuildsPerPage";

    public static int getBuildsPerPage(User user)
    {
        int buildsPerPage = UserPreferencesConfiguration.DEFAULT_HISTORY_BUILDS_PER_PAGE;
        if (user == null)
        {
            Integer sessionValue = (Integer) ActionContext.getContext().getSession().get(HistoryContext.SESSION_KEY_BUILDS_PER_PAGE);
            if (sessionValue != null)
            {
                buildsPerPage = sessionValue;
            }
        }
        else
        {
            buildsPerPage = user.getPreferences().getHistoryBuildsPerPage();
        }
    
        if (buildsPerPage <= 0)
        {
            buildsPerPage = UserPreferencesConfiguration.DEFAULT_HISTORY_BUILDS_PER_PAGE;
        }
    
        return buildsPerPage;
    }
    
    @SuppressWarnings({"unchecked"})
    public static void setBuildsPerPage(User user, int buildsPerPage, ConfigurationProvider configurationProvider)
    {
        if (user == null)
        {
            // Store in the session only.
            ActionContext.getContext().getSession().put(HistoryContext.SESSION_KEY_BUILDS_PER_PAGE, buildsPerPage);
        }
        else
        {
            // Save forever.
            UserPreferencesConfiguration preferences = configurationProvider.deepClone(user.getPreferences());
            preferences.setHistoryBuildsPerPage(buildsPerPage);
            configurationProvider.save(preferences);
        }
    }
}
