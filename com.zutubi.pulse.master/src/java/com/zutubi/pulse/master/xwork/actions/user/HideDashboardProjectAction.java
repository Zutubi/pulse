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

import com.google.common.collect.Iterables;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.user.DashboardConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * Action allowing a user to hide a chosen project from their dashboard.
 */
public class HideDashboardProjectAction extends UserActionSupport
{
    private String projectName;
    private ConfigurationProvider configurationProvider;

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
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
        DashboardConfiguration dashboardConfig = user.getConfig().getPreferences().getDashboard();

        Project p = projectManager.getProject(projectName, false);
        if(p != null)
        {
            dashboardConfig = configurationProvider.deepClone(dashboardConfig);
            if(dashboardConfig.isShowAllProjects())
            {
                dashboardConfig.setShowAllProjects(false);
                Iterables.addAll(dashboardConfig.getShownProjects(), projectManager.getAllProjectConfigs(true));
            }

            dashboardConfig.getShownProjects().remove(p.getConfig());
            configurationProvider.save(dashboardConfig);
        }

        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
