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

import com.zutubi.pulse.master.xwork.actions.ajax.HistoryContext;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * Action to save a user's choice of builds per page on history views.
 */
public class CustomiseHistoryBuildsAction extends UserActionSupport
{
    private int buildsPerPage;
    private ConfigurationProvider configurationProvider;

    public void setBuildsPerPage(int buildsPerPage)
    {
        this.buildsPerPage = buildsPerPage;
    }

    @SuppressWarnings({"unchecked"})
    public String execute() throws Exception
    {
        if (buildsPerPage <= 0)
        {
            return ERROR;
        }

        HistoryContext.setBuildsPerPage(getUser(), buildsPerPage, configurationProvider);
        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
