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

package com.zutubi.pulse.master.project.events;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * Event used to log a freeform status message for a project.
 */
public class ProjectStatusEvent extends ProjectEvent
{
    private String message;

    /**
     * Creates a new freeform status event for a project.
     *
     * @param source               {@inheritDoc}
     * @param projectConfiguration {@inheritDoc}
     * @param message              the status message to be reported, should
     *                             consist of a single line of status
     */
    public ProjectStatusEvent(Object source, ProjectConfiguration projectConfiguration, String message)
    {
        super(source, projectConfiguration);
        this.message = message;
    }

    /**
     * @return the status message, a single line of project feedback
     */
    public String getMessage()
    {
        return message;
    }

    public String toString()
    {
        return "Project Status Event: " + getProjectConfiguration().getName() + ": " + message;
    }
}
