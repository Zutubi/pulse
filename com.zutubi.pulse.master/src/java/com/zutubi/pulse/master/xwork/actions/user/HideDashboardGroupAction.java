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

import com.google.common.base.Function;
import com.zutubi.pulse.master.model.ProjectGroup;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.user.DashboardConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;

import static com.google.common.collect.Collections2.transform;

/**
 * Action allowing a user to hide a chosen project group from their dashboard.
 */
public class HideDashboardGroupAction extends UserActionSupport
{
    private String groupName;
    private ConfigurationProvider configurationProvider;

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public String execute() throws Exception
    {
        String login = SecurityUtils.getLoggedInUsername();
        if (login == null)
        {
            return ERROR;
        }

        setUserLogin(login);

        User user = getUser();

        DashboardConfiguration configuration = configurationProvider.deepClone(user.getPreferences().getDashboard());
        if(configuration.isShowAllGroups())
        {
            configuration.setShowAllGroups(false);
            configuration.getShownGroups().addAll(transform(projectManager.getAllProjectGroups(), new Function<ProjectGroup, String>()
            {
                public String apply(ProjectGroup projectGroup)
                {
                    return projectGroup.getName();
                }
            }));
        }
        
        configuration.getShownGroups().remove(groupName);
        configurationProvider.save(configuration);
        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
