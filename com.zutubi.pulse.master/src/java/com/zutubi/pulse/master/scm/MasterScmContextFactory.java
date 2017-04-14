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

package com.zutubi.pulse.master.scm;

import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmContextFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * Interface for the scm context factory.  The context factory is used to maintain the
 * scm context instances.  These instances hold persistent data between scm invocations.
 */
public interface MasterScmContextFactory extends ScmContextFactory
{
    ScmContext createContext(String implicitResource);
    ScmContext createContext(ProjectConfiguration projectConfiguration, Project.State projectState, String implicitResource) throws ScmException;
}
