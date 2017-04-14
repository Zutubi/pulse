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

import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.EnumUtils;

/**
 * An action to change the state of a project (e.g. to pause it);
 */
public class ProjectStateAction extends ProjectActionBase
{
    private String transition;

    public void setTransition(String transition)
    {
        this.transition = transition;
    }

    public String execute() throws Exception
    {
        Project project = getRequiredProject();
        Project.Transition transition = EnumUtils.fromPrettyString(Project.Transition.class, this.transition);
        if (isTransitionAllowed(transition))
        {
            projectManager.makeStateTransition(project.getId(), transition);
        }
        
        return SUCCESS;
    }

    private boolean isTransitionAllowed(Project.Transition transition)
    {
        switch (transition)
        {
            case INITIALISE:
            case PAUSE:
            case RESUME:
                return true;
            default:
                return false;
        }
    }
}
