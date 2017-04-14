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
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 * Action to view the user's personal build results.
 */
public class MyBuildsAction extends ActionSupport
{
    private User user;
    private String columns;

    public String getColumns()
    {
        if (columns == null)
        {
            columns = user.getPreferences().getMyBuildsColumns();
        }
        return columns;
    }

    /**
     * Note that this is required by the main decorator for toolbar rendering.
     * 
     * @return true to indicate the user is looking at personal builds
     */
    public boolean isPersonalBuild()
    {
        return true;
    }

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

        return SUCCESS;
    }
}
