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

package com.zutubi.pulse.master.xwork.actions.project;

import com.google.common.base.Objects;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 * An action to save the value of the filter field on the dashboard/browse views.
 */
public class SaveProjectsFilterAction extends ActionSupport
{
    private String filter;
    private boolean dashboard;

    public void setFilter(String filter)
    {
        this.filter = filter;
    }

    public void setDashboard(boolean dashboard)
    {
        this.dashboard = dashboard;
    }

    @Override
    public String execute() throws Exception
    {
        Object principle = getPrinciple();
        if (principle != null)
        {
            User user = userManager.getUser((String) principle);
            if (user != null)
            {
                boolean changed = false;
                if (dashboard)
                {
                    if (!Objects.equal(user.getDashboardFilter(), filter))
                    {
                        user.setDashboardFilter(filter);
                        changed = true;
                    }
                }
                else if (!Objects.equal(user.getBrowseViewFilter(), filter))
                {
                    user.setBrowseViewFilter(filter);
                    changed = true;
                }

                if (changed)
                {
                    userManager.save(user);
                }
            }
        }

        return SUCCESS;
    }
}
