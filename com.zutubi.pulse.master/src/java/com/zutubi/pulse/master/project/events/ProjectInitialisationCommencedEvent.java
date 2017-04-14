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
 * Initialisation has just commenced for a project.
 */
public class ProjectInitialisationCommencedEvent extends ProjectLifecycleEvent
{
    /**
     * Create an event to indicate that initialisation has started for a
     * project.
     *
     * @param source               {@inheritDoc}
     * @param projectConfiguration {@inheritDoc}
     */
    public ProjectInitialisationCommencedEvent(Object source, ProjectConfiguration projectConfiguration)
    {
        super(source, projectConfiguration);
    }

    public String toString()
    {
        return "Project Initialisation Commenced: " + getProjectConfiguration().getName();
    }
}
