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

package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.pulse.master.xwork.actions.ajax.HistoryContext;

/**
 * Action for viewing the history of builds that involved an agent
 */
public class AgentHistoryAction extends AgentActionBase
{
    private int startPage = 0;
    private String stateFilter = "";
    private int buildsPerPage;
    private String columns = UserPreferencesConfiguration.defaultProjectColumns();

    public int getStartPage()
    {
        return startPage;
    }

    public void setStartPage(int startPage)
    {
        this.startPage = startPage;
    }

    public String getStateFilter()
    {
        return stateFilter;
    }

    public void setStateFilter(String stateFilter)
    {
        this.stateFilter = stateFilter;
    }

    public int getBuildsPerPage()
    {
        return buildsPerPage;
    }

    public String getColumns()
    {
        return columns;
    }

    public String execute() throws Exception
    {
        getRequiredAgent();

        User user = getLoggedInUser();
        if (user != null)
        {
            columns = user.getConfig().getPreferences().getAgentHistoryColumns();
        }

        buildsPerPage = HistoryContext.getBuildsPerPage(user);
        return SUCCESS;
    }
}
