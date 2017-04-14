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

import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.ajax.SimpleResult;
import com.zutubi.pulse.master.xwork.actions.ajax.TrivialSuccessfulResult;
import com.zutubi.pulse.master.xwork.actions.project.ProjectDependenciesDataAction;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * Action to set the transitive mode for a user's viewing of dependency graphs.
 */
public class SetDependenciesTransitiveModeAction extends ActionSupport
{
    private String mode;
    private SimpleResult result;
    private ConfigurationProvider configurationProvider;

    public String getMode()
    {
        return mode;
    }

    public void setMode(String mode)
    {
        this.mode = mode;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    @Override @SuppressWarnings({"unchecked"})
    public String execute() throws Exception
    {
        ProjectDependencyGraphBuilder.TransitiveMode transitiveMode = ProjectDependencyGraphBuilder.TransitiveMode.valueOf(mode);

        User user = getLoggedInUser();
        if (user == null)
        {
            // For anonymous users we store this preference in the session.
            ActionContext.getContext().getSession().put(ProjectDependenciesDataAction.ANONYMOUS_MODE_KEY, transitiveMode);
        }
        else
        {
            UserPreferencesConfiguration preferences = configurationProvider.deepClone(user.getPreferences());
            preferences.setDependencyTransitiveMode(transitiveMode);
            configurationProvider.save(preferences);
        }

        result = new TrivialSuccessfulResult();
        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
