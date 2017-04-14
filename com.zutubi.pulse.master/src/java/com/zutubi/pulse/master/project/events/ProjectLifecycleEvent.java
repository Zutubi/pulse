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
 * Events regarding project lifecycle: initialisation and cleanup.
 */
public abstract class ProjectLifecycleEvent extends ProjectEvent
{
    /**
     * Create an event indicating a lifecycle change for a project.
     *
     * @param source               {@inheritDoc}
     * @param projectConfiguration {@inheritDoc}
     */
    public ProjectLifecycleEvent(Object source, ProjectConfiguration projectConfiguration)
    {
        super(source, projectConfiguration);
    }
}
