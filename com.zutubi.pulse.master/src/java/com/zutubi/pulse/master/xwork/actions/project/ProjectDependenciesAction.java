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

import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder;
import com.zutubi.pulse.master.model.User;

/**
 * Action for viewing project dependencies.
 */
public class ProjectDependenciesAction extends ProjectActionBase
{
    public static final String ANONYMOUS_MODE_KEY = "pulse.anonymousUserDependencyTransientMode";

    private String transitiveMode;

    public String getTransitiveMode()
    {
        return transitiveMode;
    }

    public String execute() throws Exception
    {
        getRequiredProject();
        transitiveMode = lookupTransitiveMode().name();
        return SUCCESS;
    }

    protected ProjectDependencyGraphBuilder.TransitiveMode lookupTransitiveMode()
    {
        User user = getLoggedInUser();
        ProjectDependencyGraphBuilder.TransitiveMode mode;
        if (user == null)
        {
            mode = (ProjectDependencyGraphBuilder.TransitiveMode) ActionContext.getContext().getSession().get(ANONYMOUS_MODE_KEY);
            if (mode == null)
            {
                mode = ProjectDependencyGraphBuilder.TransitiveMode.FULL;
            }
        }
        else
        {
            mode = user.getPreferences().getDependencyTransitiveMode();
        }

        return mode;
    }
}
