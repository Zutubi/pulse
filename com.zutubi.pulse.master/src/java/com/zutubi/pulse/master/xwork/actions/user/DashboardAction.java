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

package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.user.DashboardConfiguration;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 * Action to render a user's dashboard.  Note most of the content is loaded via
 * an AJAX request using {@link DashboardDataAction}.
 */
public class DashboardAction extends ActionSupport
{
    private User user;
    private DashboardConfiguration dashboardConfig;

    public User getUser()
    {
        return user;
    }

    public DashboardConfiguration getDashboardConfig()
    {
        return dashboardConfig;
    }

    @Override
    public String execute() throws Exception
    {
        String login = SecurityUtils.getLoggedInUsername();
        if (login == null)
        {
            return "guest";
        }

        user = userManager.getUser(login);
        if (user == null)
        {
            return ERROR;
        }

        dashboardConfig = user.getConfig().getPreferences().getDashboard();
        return SUCCESS;
    }
}
